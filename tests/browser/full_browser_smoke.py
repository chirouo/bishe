from __future__ import annotations

import re
import sys
import time
from pathlib import Path

from playwright.sync_api import TimeoutError as PlaywrightTimeoutError
from playwright.sync_api import expect, sync_playwright


BASE_URL = "http://localhost:5173"
ARTIFACT_DIR = Path("/home/gx/code/qdx/bishe/tests/browser/artifacts")
ARTIFACT_DIR.mkdir(parents=True, exist_ok=True)


def log(step: str) -> None:
    print(f"[browser-test] {step}", flush=True)


def wait_for_network_idle(page) -> None:
    page.wait_for_load_state("networkidle")


def expect_title(page, title: str, timeout: int = 15000) -> None:
    expect(page.locator("h2").filter(has_text=title).first).to_be_visible(timeout=timeout)


def wait_success_message(page, keyword: str, timeout: int = 15000) -> None:
    locator = page.locator(".el-message .el-message__content").filter(has_text=keyword).last
    expect(locator).to_be_visible(timeout=timeout)


def click_menu(page, label: str, heading: str | None = None) -> None:
    page.get_by_role("menuitem", name=label).click()
    wait_for_network_idle(page)
    if heading:
        expect(page.get_by_role("heading", name=heading)).to_be_visible(timeout=15000)


def fill_login(page, username: str, password: str) -> None:
    page.goto(f"{BASE_URL}/login", wait_until="domcontentloaded")
    wait_for_network_idle(page)
    page.get_by_placeholder("请输入用户名").fill(username)
    page.get_by_placeholder("请输入密码").fill(password)
    page.get_by_role("button", name="登录").click()


def logout(page) -> None:
    page.get_by_role("button", name="退出登录").click()
    expect(page.get_by_role("heading", name="系统登录")).to_be_visible(timeout=15000)


def choose_select_option(page, scope, label_text: str, option_text: str) -> None:
    form_item = scope.locator(".el-form-item").filter(has_text=label_text).first
    expect(form_item).to_be_visible(timeout=15000)
    form_item.locator(".el-select__wrapper").first.click()
    dropdown = page.locator(".el-select-dropdown:visible").last
    expect(dropdown).to_be_visible(timeout=15000)
    dropdown.locator(".el-select-dropdown__item").filter(has_text=option_text).first.click()


def fill_form_input(scope, label_text: str, value: str) -> None:
    form_item = scope.locator(".el-form-item").filter(has_text=label_text).first
    expect(form_item).to_be_visible(timeout=15000)
    field = form_item.locator("textarea, input").first
    field.fill(value)


def get_form_input(scope, label_text: str) -> str:
    form_item = scope.locator(".el-form-item").filter(has_text=label_text).first
    field = form_item.locator("textarea, input").first
    return field.input_value()


def set_question_options(dialog, mapping: dict[str, str]) -> None:
    for label, content in mapping.items():
        dialog.get_by_placeholder(f"请输入选项 {label}").fill(content)


def submit_manual_question(page, unique_stem: str) -> None:
    log("teacher: create manual single-choice question")
    page.get_by_role("button", name="新增单选题").click()
    dialog = page.locator(".el-dialog:visible").last
    expect(dialog).to_be_visible(timeout=15000)

    choose_select_option(page, dialog, "知识点", "命题逻辑")
    fill_form_input(dialog, "题干", unique_stem)
    set_question_options(
        dialog,
        {
            "A": "浏览器级测试选项 A",
            "B": "浏览器级测试选项 B",
            "C": "浏览器级测试选项 C",
            "D": "浏览器级测试选项 D",
        },
    )
    choose_select_option(page, dialog, "正确答案", "B")
    fill_form_input(dialog, "题目解析", "浏览器级测试手动新增题目解析。")
    dialog.get_by_role("button", name="保存题目").click()
    wait_success_message(page, "题目新增成功", timeout=15000)
    expect(page.locator(".el-table").get_by_text(unique_stem)).to_be_visible(timeout=15000)


def exercise_ai_question_draft(page) -> None:
    log("teacher: generate AI question draft and switch models")
    page.get_by_role("button", name="新增单选题").click()
    dialog = page.locator(".el-dialog:visible").last
    expect(dialog).to_be_visible(timeout=15000)

    choose_select_option(page, dialog, "知识点", "集合与关系")
    choose_select_option(page, dialog, "AI模型", "qwen-math-turbo")
    wait_success_message(page, "已切换到模型：qwen-math-turbo", timeout=15000)
    choose_select_option(page, dialog, "AI模型", "qwen-math-plus")
    wait_success_message(page, "已切换到模型：qwen-math-plus", timeout=15000)
    fill_form_input(dialog, "AI补充要求", "贴近阶段测试，强调定义辨析。")
    dialog.get_by_role("button", name="AI生成草稿").click()
    wait_success_message(page, "AI 草稿已生成", timeout=70000)

    stem_value = get_form_input(dialog, "题干")
    analysis_value = get_form_input(dialog, "题目解析")
    if not stem_value.strip() or not analysis_value.strip():
        raise AssertionError("AI question draft did not populate stem or analysis")

    dialog.get_by_role("button", name="取消").click()
    expect(dialog).not_to_be_visible(timeout=15000)


def create_ai_paper(page, unique_title: str) -> None:
    log("teacher: create draft paper from AI draft and publish it")
    click_menu(page, "智能组卷", "智能组卷")
    ai_config_item = page.locator(".el-form-item").filter(has_text="AI组卷参数").first
    expect(ai_config_item).to_be_visible(timeout=15000)
    expect(ai_config_item.locator(".ai-field-label").filter(has_text="题目数量").first).to_be_visible(timeout=15000)
    expect(ai_config_item.locator(".ai-field-label").filter(has_text="每题分值").first).to_be_visible(timeout=15000)
    expect(ai_config_item.locator(".ai-field-label").filter(has_text="使用模型").first).to_be_visible(timeout=15000)
    ai_config_item.locator("textarea").fill("优先覆盖薄弱知识点，适合阶段测试。")
    page.get_by_role("button", name="AI生成组卷草稿").click()
    wait_success_message(page, "AI 组卷草稿已生成", timeout=70000)

    title_input = page.locator(".el-form-item").filter(has_text="试卷标题").locator("input").first
    title_input.fill(unique_title)
    description_input = page.locator(".el-form-item").filter(has_text="试卷说明").locator("textarea").first
    description_input.fill("浏览器级测试创建的试卷。")

    strategy_alert = page.locator(".el-alert").first
    expect(strategy_alert).to_be_visible(timeout=15000)

    page.get_by_role("button", name="保存试卷").click()
    wait_success_message(page, "试卷创建成功", timeout=15000)

    paper_row = page.locator(".el-table__row").filter(has_text=unique_title).first
    expect(paper_row).to_be_visible(timeout=15000)
    expect(paper_row.get_by_text("草稿")).to_be_visible(timeout=15000)
    paper_row.get_by_role("button", name="发布试卷").click()

    confirm = page.locator(".el-message-box:visible")
    expect(confirm).to_be_visible(timeout=15000)
    confirm.get_by_role("button", name=re.compile(r"^(OK|确定)$")).click()

    wait_success_message(page, "试卷已发布", timeout=15000)
    expect(paper_row.get_by_text("已发布")).to_be_visible(timeout=15000)
    expect(paper_row.get_by_role("button", name="查看成绩")).to_be_visible(timeout=15000)


def answer_current_exam(page) -> None:
    log("student: answer and submit paper")
    dom_snapshot = page.evaluate(
        """() => ({
            hasQuestionText: document.body.innerText.includes('第 1 题'),
            textareaCount: document.querySelectorAll('textarea').length,
            radioInputCount: document.querySelectorAll('input[type="radio"]').length,
            buttonTexts: Array.from(document.querySelectorAll('button')).map((item) => item.innerText.trim()).filter(Boolean)
        })"""
    )
    print("[browser-test] exam dom snapshot:", dom_snapshot, flush=True)
    if not dom_snapshot["hasQuestionText"]:
        raise AssertionError("No questions rendered on exam detail page")

    page.evaluate(
        """() => {
            const textareas = Array.from(document.querySelectorAll('textarea'));
            textareas.forEach((item) => {
                item.value = '这是浏览器级测试提交的简答题答案，用于验证主观题智能评阅。';
                item.dispatchEvent(new Event('input', { bubbles: true }));
                item.dispatchEvent(new Event('change', { bubbles: true }));
            });

            const firstRadio = document.querySelector('input[type="radio"]');
            if (firstRadio) {
                firstRadio.click();
                firstRadio.dispatchEvent(new Event('change', { bubbles: true }));
            }
        }"""
    )

    page.get_by_role("button", name="提交试卷").click()
    confirm = page.locator(".el-message-box:visible")
    expect(confirm).to_be_visible(timeout=15000)
    confirm.get_by_role("button", name=re.compile(r"^(OK|确定)$")).click()
    wait_success_message(page, "提交成功", timeout=70000)
    expect(page.get_by_text("系统反馈").first).to_be_visible(timeout=15000)


def assert_student_result_contains(page, paper_title: str) -> None:
    click_menu(page, "学习分析", "学习分析")
    expect(page.locator(".el-table").get_by_text(paper_title)).to_be_visible(timeout=15000)


def assert_teacher_result_contains(page, paper_title: str) -> None:
    click_menu(page, "智能组卷", "智能组卷")
    paper_row = page.locator(".el-table__row").filter(has_text=paper_title).first
    expect(paper_row).to_be_visible(timeout=15000)
    paper_row.get_by_role("button", name="查看成绩").click()
    wait_for_network_idle(page)
    expect(page.get_by_text("学生成绩明细")).to_be_visible(timeout=15000)
    expect(page.locator(".el-table").get_by_text("学生一号")).to_be_visible(timeout=15000)
    page.locator(".el-table__row").filter(has_text="学生一号").first.get_by_role("button", name="查看答卷").click()
    wait_for_network_idle(page)
    expect(page.get_by_text("教师/系统反馈").first).to_be_visible(timeout=15000)
    expect(page.get_by_text("标准答案").first).to_be_visible(timeout=15000)


def assert_teacher_statistics_contains(page, paper_title: str) -> None:
    click_menu(page, "班级统计", "班级统计")
    expect(page.locator(".el-table").get_by_text(paper_title)).to_be_visible(timeout=15000)


def main() -> int:
    stamp = time.strftime("%Y%m%d%H%M%S")
    unique_stem = f"浏览器测试单选题 {stamp}"
    unique_paper_title = f"浏览器测试试卷 {stamp}"
    console_errors: list[str] = []
    page_errors: list[str] = []

    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)
        context = browser.new_context(viewport={"width": 1440, "height": 1200})
        page = context.new_page()
        page.set_default_timeout(15000)

        page.on(
            "console",
            lambda message: console_errors.append(f"{message.type}: {message.text}")
            if message.type == "error"
            else None,
        )
        page.on("pageerror", lambda error: page_errors.append(str(error)))

        try:
            log("teacher: login")
            fill_login(page, "teacher01", "123456")
            page.wait_for_url(re.compile(".*/teacher/.*"), timeout=15000)
            expect_title(page, "教师首页")
            expect(page.get_by_text("课程数")).to_be_visible(timeout=15000)

            log("teacher: browse knowledge points")
            click_menu(page, "知识点管理", "知识点管理")
            expect(page.locator(".el-table").get_by_text("命题逻辑")).to_be_visible(timeout=15000)

            log("teacher: open question bank")
            click_menu(page, "题库管理", "题库管理")
            submit_manual_question(page, unique_stem)
            exercise_ai_question_draft(page)

            create_ai_paper(page, unique_paper_title)

            log("teacher: logout")
            logout(page)

            log("student: login")
            fill_login(page, "student01", "123456")
            page.wait_for_url(re.compile(".*/student/.*"), timeout=15000)
            expect_title(page, "学生首页")

            log("student: open exam list")
            click_menu(page, "我的考试", "我的考试")
            paper_row = page.locator(".el-table__row").filter(has_text=unique_paper_title).first
            expect(paper_row).to_be_visible(timeout=15000)
            paper_row.get_by_role("button").click()
            expect(page.get_by_role("button", name="提交试卷")).to_be_visible(timeout=15000)
            expect(page.get_by_text(unique_paper_title)).to_be_visible(timeout=15000)
            expect(page.get_by_text("单选题").first).to_be_visible(timeout=15000)

            answer_current_exam(page)
            assert_student_result_contains(page, unique_paper_title)

            log("student: logout")
            logout(page)

            log("teacher: login for result review")
            fill_login(page, "teacher01", "123456")
            page.wait_for_url(re.compile(".*/teacher/.*"), timeout=15000)
            expect_title(page, "教师首页")

            assert_teacher_result_contains(page, unique_paper_title)
            assert_teacher_statistics_contains(page, unique_paper_title)

        except Exception as exc:
            screenshot = ARTIFACT_DIR / f"browser-smoke-failed-{stamp}.png"
            page.screenshot(path=str(screenshot), full_page=True)
            log(f"failed, screenshot saved to {screenshot}")
            print(f"[browser-test] current url: {page.url}", flush=True)
            print(f"[browser-test] body text: {page.locator('body').inner_text()[:800]}", flush=True)
            if console_errors:
                print("[browser-test] console errors before failure:", flush=True)
                for item in console_errors:
                    print(item, flush=True)
            if page_errors:
                print("[browser-test] page errors before failure:", flush=True)
                for item in page_errors:
                    print(item, flush=True)
            raise exc
        finally:
            browser.close()

    unexpected_console_errors = [
        item
        for item in console_errors
        if not re.search(r"(favicon|source map|ResizeObserver loop limit exceeded)", item, re.I)
    ]
    if unexpected_console_errors or page_errors:
        log("console/page errors detected")
        for item in unexpected_console_errors:
            print(item, flush=True)
        for item in page_errors:
            print(item, flush=True)
        return 1

    log("all browser-level flows passed")
    return 0


if __name__ == "__main__":
    sys.exit(main())
