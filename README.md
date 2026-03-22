# 离散数学阶段性测试平台

一个面向本科毕业设计的前后端分离项目，主题是“融合大语言模型智能组卷与个性化评估的离散数学阶段性测试平台”。系统覆盖教师组卷、学生在线考试、自动判分、AI 辅助出题、AI 主观题评阅、学习分析和班级统计。

## 技术栈

- 后端：Spring Boot 2.7、MyBatis-Plus、MySQL 8、JWT
- 前端：Vue 3、Vite、Pinia、Element Plus、ECharts
- AI：DashScope OpenAI-compatible 接口，当前默认模型 `qwen-math-plus`
- 说明：当前环境是 Java 11，因此后端选择 Spring Boot 2.7.x 以确保兼容性

## 目录结构

- `backend/`：Spring Boot 后端工程
- `frontend/`：Vue 前端工程
- `docs/`：系统设计与开发说明
- `scripts/`：数据库初始化脚本

## 快速启动

### 1. 初始化数据库

```bash
mysql -h 127.0.0.1 -P 3306 -u root -p123456 < scripts/init-db.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

默认端口：`8080`

说明：仓库已内置 `backend/.mvn/maven.config`，会将 Maven 本地仓库固定到项目内，避免 WSL + Windows Maven 路径导致的依赖编译问题。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

默认端口：`5173`

## 演示账号

- 教师：`teacher01 / 123456`
- 学生：`student01 / 123456`

## 当前功能

- 教师端：
  - 教师首页概览
  - 知识点管理
  - 题库管理
  - AI 题目草稿生成
  - 智能组卷、草稿保存、草稿发布
  - 试卷成绩明细与学生答卷详情
  - 班级统计分析
- 学生端：
  - 我的考试
  - 在线作答
  - 自动判分与 AI 主观题评阅
  - 学习分析
- AI 能力：
  - AI 题目草稿生成
  - AI 组卷草稿生成
  - AI 主观题评阅
  - 模型切换

## 文档索引

- 架构概览：[docs/architecture.md](docs/architecture.md)
- 系统设计说明：[docs/system-design.md](docs/system-design.md)
- 数据库设计说明：[docs/database-design.md](docs/database-design.md)
- 浏览器级测试报告：[docs/browser-test-report.md](docs/browser-test-report.md)
- 论文写作素材：[docs/thesis-writing-guide.md](docs/thesis-writing-guide.md)
- 论文全文草稿：[docs/thesis-full-draft.md](docs/thesis-full-draft.md)
- 开题报告草稿：[docs/opening-report.md](docs/opening-report.md)
- 论文初稿目录：[docs/thesis-outline.md](docs/thesis-outline.md)
- 答辩 PPT 提纲：[docs/defense-ppt-outline.md](docs/defense-ppt-outline.md)
- 跨平台部署教程：[docs/deployment-guide.md](docs/deployment-guide.md)

## 测试命令

- 后端自动化测试：
  ```bash
  cd backend && mvn test
  ```
- 前端构建验证：
  ```bash
  cd frontend && npm run build
  ```
- 浏览器级全链路测试：
  ```bash
  conda run -n qdx_bishe python tests/browser/full_browser_smoke.py
  ```
