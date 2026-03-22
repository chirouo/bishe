#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import time
from collections import Counter

import requests


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

DIFFICULTIES = ["EASY", "MEDIUM", "HARD", "MEDIUM", "EASY"]


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


def build_prompt(index: int, knowledge_point_name: str, topic: str) -> str:
    return (
        f"第{index}题，知识点为{knowledge_point_name}，方向：{topic}。"
        "请生成一题适合本科离散数学阶段测试的单选题。"
        "不要与已有题目重复，避免使用与常见教材示例完全相同的题干。"
        "题干简洁清楚，四个选项要有干扰性但不能含糊。"
        "不要使用过于模板化的表述。"
    )


def main() -> int:
    parser = argparse.ArgumentParser(description="Batch generate discrete math questions via real AI requests.")
    parser.add_argument("--base-url", default="http://127.0.0.1:8080/api")
    parser.add_argument("--username", default="teacher01")
    parser.add_argument("--password", default="123456")
    parser.add_argument("--course-id", type=int, default=1)
    parser.add_argument("--count", type=int, default=20)
    args = parser.parse_args()

    client = ApiClient(args.base_url, args.username, args.password)
    knowledge_points = client.request("GET", "/teacher/knowledge-points", params={"courseId": args.course_id})
    if not knowledge_points:
        raise RuntimeError("No knowledge points found for the target course")

    knowledge_points = sorted(knowledge_points, key=lambda item: item["id"])
    existing_questions = client.request("GET", "/teacher/questions", params={"courseId": args.course_id})
    existing_stems = {item["stem"].strip() for item in existing_questions if item.get("stem")}

    saved = []
    failed = []
    plan_index = 0
    max_attempts = max(args.count * 5, 20)

    while len(saved) < args.count and plan_index < max_attempts:
        kp = knowledge_points[plan_index % len(knowledge_points)]
        kp_name = kp["pointName"]
        topic_pool = TOPIC_POOLS.get(kp_name, [f"{kp_name}基础概念辨析", f"{kp_name}综合判断"])
        topic = topic_pool[(plan_index // len(knowledge_points)) % len(topic_pool)]
        difficulty = DIFFICULTIES[plan_index % len(DIFFICULTIES)]
        question_no = len(saved) + 1
        prompt = build_prompt(question_no, kp_name, topic)

        success = False
        for retry in range(1, 4):
            try:
                draft = client.request(
                    "POST",
                    "/teacher/ai/questions/draft",
                    data=json.dumps(
                        {
                            "courseId": args.course_id,
                            "knowledgePointId": kp["id"],
                            "questionType": "SINGLE_CHOICE",
                            "difficulty": difficulty,
                            "requirements": prompt,
                        }
                    ),
                )
                stem = (draft.get("stem") or "").strip()
                if not stem:
                    raise RuntimeError("AI returned an empty stem")
                if stem in existing_stems:
                    raise RuntimeError("AI returned a duplicate stem")
                options = [
                    {"label": item["label"], "content": item["content"]}
                    for item in (draft.get("options") or [])
                ]
                if len(options) < 4:
                    raise RuntimeError("AI returned fewer than 4 options")

                question_id = client.request(
                    "POST",
                    "/teacher/questions",
                    data=json.dumps(
                        {
                            "courseId": args.course_id,
                            "knowledgePointId": kp["id"],
                            "questionType": "SINGLE_CHOICE",
                            "stem": stem,
                            "difficulty": draft.get("difficulty") or difficulty,
                            "answer": draft["answer"],
                            "analysis": draft.get("analysis") or "",
                            "source": draft.get("source") or "AI_GENERATED",
                            "options": options,
                        }
                    ),
                )
                existing_stems.add(stem)
                saved.append(
                    {
                        "id": question_id,
                        "knowledgePointName": kp_name,
                        "difficulty": draft.get("difficulty") or difficulty,
                        "stem": stem,
                    }
                )
                print(
                    f"[{len(saved):02d}/{args.count}] saved question {question_id} | {kp_name} | {draft.get('difficulty') or difficulty} | {stem}",
                    flush=True,
                )
                success = True
                break
            except Exception as exc:  # noqa: BLE001
                print(
                    f"[plan {plan_index + 1:02d}] retry {retry} failed | {kp_name} | {difficulty} | {exc}",
                    flush=True,
                )
                time.sleep(1.5)

        if not success:
            failed.append({"planIndex": plan_index + 1, "knowledgePointName": kp_name, "difficulty": difficulty, "topic": topic})
        plan_index += 1

    total_questions = client.request("GET", "/teacher/questions", params={"courseId": args.course_id})
    summary = {
        "savedCount": len(saved),
        "failedCount": len(failed),
        "courseQuestionCount": len(total_questions),
        "knowledgePointDistribution": dict(Counter(item["knowledgePointName"] for item in saved)),
        "difficultyDistribution": dict(Counter(item["difficulty"] for item in saved)),
        "savedIds": [item["id"] for item in saved],
        "failedPlans": failed,
    }
    print("SUMMARY=" + json.dumps(summary, ensure_ascii=False), flush=True)
    return 0 if len(saved) == args.count else 1


if __name__ == "__main__":
    raise SystemExit(main())
