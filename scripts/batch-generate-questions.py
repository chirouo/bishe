#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import time
from collections import Counter, defaultdict
from pathlib import Path

import requests


CONFIG_PATH = Path("/home/gx/code/qdx/bishe/backend/src/main/resources/application.yml")
QUESTION_TYPES = ("SINGLE_CHOICE", "SHORT_ANSWER", "TRUE_FALSE")
TYPE_LABELS = {
    "SINGLE_CHOICE": "单选题",
    "SHORT_ANSWER": "简答题",
    "TRUE_FALSE": "判断题",
}
TOPIC_POOLS = {
    "命题逻辑": [
        "命题真值判断与等价变形",
        "逆命题否命题逆否命题辨析",
        "复合命题与逻辑等价",
        "充分条件必要条件",
        "真值表与永真式",
        "逻辑联结词综合辨析",
        "命题推理与逻辑蕴含",
    ],
    "集合与关系": [
        "关系性质辨析",
        "集合运算与子集判断",
        "等价关系与偏序关系",
        "关系的复合与矩阵表示基础",
        "幂集与笛卡尔积基础",
        "自反对称传递综合判断",
        "函数关系与关系性质综合",
    ],
    "图论基础": [
        "图的基本概念与路径连通",
        "顶点边与度数基础",
        "树与连通图基本性质",
        "简单图性质与判定",
        "回路路径与连通分量",
        "二分图与度数相关基础",
        "图的邻接关系与连通判断",
    ],
}
DIFFICULTY_CYCLES = {
    "SINGLE_CHOICE": ["EASY", "MEDIUM", "HARD", "MEDIUM", "EASY"],
    "SHORT_ANSWER": ["MEDIUM", "HARD", "MEDIUM", "EASY"],
    "TRUE_FALSE": ["EASY", "MEDIUM", "EASY", "HARD"],
}


class ApiClient:
    def __init__(self, base_url: str, username: str, password: str) -> None:
        self.base_url = base_url.rstrip("/")
        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})
        login = self.request(
            "POST",
            "/auth/login",
            data=json.dumps({"username": username, "password": password}),
            timeout=30,
        )
        self.session.headers["Authorization"] = f"Bearer {login['token']}"

    def request(self, method: str, path: str, timeout: int = 90, **kwargs):
        response = self.session.request(method, self.base_url + path, timeout=timeout, **kwargs)
        response.raise_for_status()
        payload = response.json()
        if payload.get("code") != 200:
            raise RuntimeError(payload.get("message") or f"Request failed: {path}")
        return payload["data"]


class LlmClient:
    def __init__(self, config_path: Path) -> None:
        config = load_llm_config(config_path)
        self.base_url = config["base-url"].rstrip("/")
        self.api_key = config["api-key"]
        self.model = config["model"]
        self.temperature = float(config.get("temperature", "0.2"))
        self.enable_thinking = parse_bool(config.get("enable-thinking", "false"))
        self.connect_timeout = 5
        self.read_timeout = 60
        self.session = requests.Session()

    def generate_question(
        self,
        *,
        question_type: str,
        course_name: str,
        knowledge_point_name: str,
        difficulty: str,
        requirements: str,
    ) -> dict:
        payload = {
            "model": self.model,
            "temperature": self.temperature,
            "enable_thinking": self.enable_thinking,
            "response_format": {"type": "json_object"},
            "messages": [
                {
                    "role": "system",
                    "content": (
                        "你是离散数学题库生成助手。必须返回合法 JSON，不能输出 markdown、latex、代码块或额外说明。"
                    ),
                },
                {
                    "role": "user",
                    "content": build_prompt(
                        question_type=question_type,
                        course_name=course_name,
                        knowledge_point_name=knowledge_point_name,
                        difficulty=difficulty,
                        requirements=requirements,
                    ),
                },
            ],
        }
        response = self.session.post(
            f"{self.base_url}/chat/completions",
            headers={
                "Content-Type": "application/json",
                "Authorization": f"Bearer {self.api_key}",
            },
            json=payload,
            timeout=(self.connect_timeout, self.read_timeout),
        )
        response.raise_for_status()
        data = response.json()
        try:
            content = data["choices"][0]["message"]["content"]
        except Exception as exc:  # noqa: BLE001
            raise RuntimeError(f"invalid llm response: {data}") from exc
        return json.loads(sanitize_json_content(content))


def load_llm_config(config_path: Path) -> dict[str, str]:
    if not config_path.exists():
        raise RuntimeError(f"config file not found: {config_path}")

    config: dict[str, str] = {}
    in_llm_block = False
    for raw_line in config_path.read_text(encoding="utf-8").splitlines():
        if raw_line.startswith("  llm:"):
            in_llm_block = True
            continue
        if in_llm_block:
            if raw_line.startswith("    ") and ":" in raw_line:
                key, value = raw_line.strip().split(":", 1)
                config[key.strip()] = value.strip()
                continue
            if raw_line.startswith("      - "):
                continue
            break

    required_keys = ("base-url", "api-key", "model")
    missing = [key for key in required_keys if not config.get(key)]
    if missing:
        raise RuntimeError(f"llm config missing required keys: {', '.join(missing)}")
    return config


def parse_bool(value: str) -> bool:
    return str(value).strip().lower() in {"true", "1", "yes", "on"}


def sanitize_json_content(content: str) -> str:
    trimmed = (content or "").strip()
    if trimmed.startswith("```"):
        trimmed = re.sub(r"^```json\s*", "", trimmed)
        trimmed = re.sub(r"^```\s*", "", trimmed)
        trimmed = re.sub(r"\s*```$", "", trimmed)
    return trimmed.strip()


def build_prompt(
    *,
    question_type: str,
    course_name: str,
    knowledge_point_name: str,
    difficulty: str,
    requirements: str,
) -> str:
    shared = (
        f"课程：{course_name}\n"
        f"知识点：{knowledge_point_name}\n"
        f"题型：{TYPE_LABELS[question_type]}\n"
        f"难度：{difficulty}\n"
        f"补充要求：{requirements}\n"
        "要求：题干自然，不要与常见教材例题完全重复，不要和已有题目重复。"
        "不要输出 markdown、latex、代码块或解释文字，只返回 JSON。\n"
    )
    if question_type == "SINGLE_CHOICE":
        return (
            shared
            + "请生成一道适合本科离散数学阶段测试的单选题，四个选项要有区分度，答案唯一明确。\n"
            + 'JSON 结构：{"questionType":"SINGLE_CHOICE","difficulty":"EASY","stem":"...","answer":"A","analysis":"...","options":[{"label":"A","content":"..."},{"label":"B","content":"..."},{"label":"C","content":"..."},{"label":"D","content":"..."}]}'
        )
    if question_type == "SHORT_ANSWER":
        return (
            shared
            + "请生成一道适合本科离散数学阶段测试的简答题，并给出简洁明确的参考答案与评分要点说明。\n"
            + 'JSON 结构：{"questionType":"SHORT_ANSWER","difficulty":"MEDIUM","stem":"...","answer":"...","analysis":"..."}'
        )
    return (
        shared
        + "请生成一道适合本科离散数学阶段测试的判断题，题干必须可以明确判断为正确或错误，不要含糊。\n"
        + 'JSON 结构：{"questionType":"TRUE_FALSE","difficulty":"EASY","stem":"...","answer":"TRUE","analysis":"..."}'
    )


def build_topic_hint(index: int, knowledge_point_name: str) -> str:
    topic_pool = TOPIC_POOLS.get(knowledge_point_name, [f"{knowledge_point_name}基础概念辨析", f"{knowledge_point_name}综合判断"])
    topic = topic_pool[index % len(topic_pool)]
    return f"优先围绕“{topic}”出题。"


def normalize_label(value: str) -> str:
    return (value or "").strip().upper()


def normalize_true_false_answer(value: str) -> str:
    normalized = normalize_label(value)
    if normalized in {"TRUE", "正确", "T", "YES"}:
        return "TRUE"
    if normalized in {"FALSE", "错误", "F", "NO"}:
        return "FALSE"
    raise RuntimeError(f"invalid true/false answer: {value}")


def normalize_single_choice_draft(raw: dict, difficulty: str) -> dict:
    options = raw.get("options") or []
    if len(options) < 4:
        raise RuntimeError("AI returned fewer than 4 options")

    normalized_options = []
    seen_labels = set()
    for item in options[:4]:
        label = normalize_label(item.get("label"))
        if label not in {"A", "B", "C", "D"}:
            raise RuntimeError(f"invalid option label: {label}")
        if label in seen_labels:
            raise RuntimeError("duplicate option labels")
        content = (item.get("content") or "").strip()
        if not content:
            raise RuntimeError("empty option content")
        seen_labels.add(label)
        normalized_options.append({"label": label, "content": content})

    answer = normalize_label(raw.get("answer"))
    if answer not in {item["label"] for item in normalized_options}:
        raise RuntimeError("answer not in options")

    stem = (raw.get("stem") or "").strip()
    if not stem:
        raise RuntimeError("empty stem")

    return {
        "questionType": "SINGLE_CHOICE",
        "difficulty": raw.get("difficulty") or difficulty,
        "stem": stem,
        "answer": answer,
        "analysis": (raw.get("analysis") or "").strip(),
        "options": normalized_options,
    }


def normalize_short_answer_draft(raw: dict, difficulty: str) -> dict:
    stem = (raw.get("stem") or "").strip()
    answer = (raw.get("answer") or "").strip()
    if not stem:
        raise RuntimeError("empty stem")
    if not answer:
        raise RuntimeError("empty short-answer reference answer")
    return {
        "questionType": "SHORT_ANSWER",
        "difficulty": raw.get("difficulty") or difficulty,
        "stem": stem,
        "answer": answer,
        "analysis": (raw.get("analysis") or "").strip(),
        "options": [],
    }


def normalize_true_false_draft(raw: dict, difficulty: str) -> dict:
    stem = (raw.get("stem") or "").strip()
    if not stem:
        raise RuntimeError("empty stem")
    return {
        "questionType": "TRUE_FALSE",
        "difficulty": raw.get("difficulty") or difficulty,
        "stem": stem,
        "answer": normalize_true_false_answer(raw.get("answer")),
        "analysis": (raw.get("analysis") or "").strip(),
        "options": [],
    }


def normalize_draft(question_type: str, raw: dict, difficulty: str) -> dict:
    if question_type == "SINGLE_CHOICE":
        return normalize_single_choice_draft(raw, difficulty)
    if question_type == "SHORT_ANSWER":
        return normalize_short_answer_draft(raw, difficulty)
    return normalize_true_false_draft(raw, difficulty)


def difficulty_for(question_type: str, index: int) -> str:
    cycle = DIFFICULTY_CYCLES[question_type]
    return cycle[index % len(cycle)]


def main() -> int:
    parser = argparse.ArgumentParser(description="Batch generate discrete math questions via real AI requests.")
    parser.add_argument("--base-url", default="http://127.0.0.1:8080/api")
    parser.add_argument("--username", default="teacher01")
    parser.add_argument("--password", default="123456")
    parser.add_argument("--course-id", type=int, default=1)
    parser.add_argument("--target-per-type", type=int, default=20)
    parser.add_argument(
        "--question-types",
        default="SINGLE_CHOICE,SHORT_ANSWER,TRUE_FALSE",
        help="Comma-separated question types to target",
    )
    args = parser.parse_args()

    requested_types = [item.strip().upper() for item in args.question_types.split(",") if item.strip()]
    invalid_types = [item for item in requested_types if item not in QUESTION_TYPES]
    if invalid_types:
        raise SystemExit(f"Unsupported question types: {', '.join(invalid_types)}")

    client = ApiClient(args.base_url, args.username, args.password)
    llm_client = LlmClient(CONFIG_PATH)

    courses = client.request("GET", "/teacher/courses")
    course = next((item for item in courses if item["id"] == args.course_id), None)
    if not course:
        raise RuntimeError(f"Course not found: {args.course_id}")

    knowledge_points = client.request("GET", "/teacher/knowledge-points", params={"courseId": args.course_id})
    if not knowledge_points:
        raise RuntimeError("No knowledge points found for the target course")
    knowledge_points = sorted(knowledge_points, key=lambda item: item["id"])

    existing_questions = client.request("GET", "/teacher/questions", params={"courseId": args.course_id})
    existing_stems = {(item.get("stem") or "").strip() for item in existing_questions if item.get("stem")}
    existing_ai_counts = Counter(
        item["questionType"]
        for item in existing_questions
        if item.get("source") == "AI_GENERATED" and item.get("questionType") in QUESTION_TYPES
    )

    saved_by_type: dict[str, list[dict]] = defaultdict(list)
    failed_by_type: dict[str, list[dict]] = defaultdict(list)

    for question_type in requested_types:
        needed = max(0, args.target_per_type - existing_ai_counts.get(question_type, 0))
        if needed == 0:
            print(f"[skip] {TYPE_LABELS[question_type]} already has {args.target_per_type} AI-generated questions", flush=True)
            continue

        max_attempts = max(needed * 6, 30)
        plan_index = 0
        while len(saved_by_type[question_type]) < needed and plan_index < max_attempts:
            knowledge_point = knowledge_points[plan_index % len(knowledge_points)]
            difficulty = difficulty_for(question_type, plan_index)
            requirements = build_topic_hint(plan_index, knowledge_point["pointName"])

            success = False
            for retry in range(1, 4):
                try:
                    draft = llm_client.generate_question(
                        question_type=question_type,
                        course_name=course["courseName"],
                        knowledge_point_name=knowledge_point["pointName"],
                        difficulty=difficulty,
                        requirements=requirements,
                    )
                    normalized = normalize_draft(question_type, draft, difficulty)
                    stem = normalized["stem"]
                    if stem in existing_stems:
                        raise RuntimeError("AI returned a duplicate stem")

                    question_id = client.request(
                        "POST",
                        "/teacher/questions",
                        data=json.dumps(
                            {
                                "courseId": args.course_id,
                                "knowledgePointId": knowledge_point["id"],
                                "questionType": normalized["questionType"],
                                "stem": normalized["stem"],
                                "difficulty": normalized["difficulty"],
                                "answer": normalized["answer"],
                                "analysis": normalized["analysis"],
                                "source": "AI_GENERATED",
                                "options": normalized["options"],
                            }
                        ),
                    )
                    existing_stems.add(stem)
                    saved_entry = {
                        "id": question_id,
                        "questionType": question_type,
                        "knowledgePointName": knowledge_point["pointName"],
                        "difficulty": normalized["difficulty"],
                        "stem": stem,
                    }
                    saved_by_type[question_type].append(saved_entry)
                    print(
                        f"[{TYPE_LABELS[question_type]} {len(saved_by_type[question_type]):02d}/{needed}] "
                        f"saved question {question_id} | {knowledge_point['pointName']} | {normalized['difficulty']} | {stem}",
                        flush=True,
                    )
                    success = True
                    break
                except Exception as exc:  # noqa: BLE001
                    print(
                        f"[{TYPE_LABELS[question_type]} plan {plan_index + 1:02d}] retry {retry} failed | "
                        f"{knowledge_point['pointName']} | {difficulty} | {exc}",
                        flush=True,
                    )
                    time.sleep(1.5)

            if not success:
                failed_by_type[question_type].append(
                    {
                        "planIndex": plan_index + 1,
                        "knowledgePointName": knowledge_point["pointName"],
                        "difficulty": difficulty,
                    }
                )
            plan_index += 1

        if len(saved_by_type[question_type]) < needed:
            raise RuntimeError(
                f"{TYPE_LABELS[question_type]} only generated {len(saved_by_type[question_type])}/{needed} questions"
            )

    total_questions = client.request("GET", "/teacher/questions", params={"courseId": args.course_id})
    summary = {
        "targetPerType": args.target_per_type,
        "requestedTypes": requested_types,
        "savedCountByType": {key: len(value) for key, value in saved_by_type.items()},
        "failedCountByType": {key: len(value) for key, value in failed_by_type.items()},
        "courseQuestionCount": len(total_questions),
        "questionTypeDistribution": dict(Counter(item["questionType"] for item in total_questions)),
        "knowledgePointDistribution": dict(Counter(item["knowledgePointName"] for item in total_questions)),
        "savedIdsByType": {key: [item["id"] for item in value] for key, value in saved_by_type.items()},
    }
    print("SUMMARY=" + json.dumps(summary, ensure_ascii=False), flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
