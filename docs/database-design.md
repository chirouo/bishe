# 数据库设计说明

## 1. 数据库概览

项目数据库名为 `bishe_platform`，核心表围绕“用户、课程、知识点、题目、试卷、考试记录、学生答案”展开。

## 2. 核心数据表

### 2.1 `sys_user`

用途：存储教师和学生账号信息。

关键字段：

- `id`：主键
- `username`：用户名
- `password`：密码
- `real_name`：真实姓名
- `role`：角色，区分教师和学生
- `status`：账号状态

### 2.2 `course`

用途：存储课程信息。

关键字段：

- `id`
- `course_name`
- `description`
- `status`

### 2.3 `knowledge_point`

用途：存储课程下的知识点。

关键字段：

- `id`
- `course_id`
- `point_name`
- `difficulty`
- `description`

关系：

- 一个课程对应多个知识点

### 2.4 `question`

用途：存储题库题目。

关键字段：

- `id`
- `course_id`
- `knowledge_point_id`
- `question_type`
- `stem`
- `difficulty`
- `answer`
- `analysis`
- `source`

说明：

- `source` 用于区分手动录入、AI 生成草稿、AI 正式生成等来源

### 2.5 `question_option`

用途：存储单选题选项。

关键字段：

- `id`
- `question_id`
- `option_label`
- `option_content`

关系：

- 一道单选题对应多个选项

### 2.6 `paper`

用途：存储试卷信息。

关键字段：

- `id`
- `course_id`
- `title`
- `description`
- `total_score`
- `status`
- `created_by`

说明：

- `status` 目前包括 `DRAFT` 与 `PUBLISHED`

### 2.7 `paper_question`

用途：存储试卷与题目的关联关系。

关键字段：

- `id`
- `paper_id`
- `question_id`
- `question_order`
- `score`

关系：

- 一张试卷包含多道题
- 一道题可以被多张试卷复用

### 2.8 `exam_record`

用途：存储学生参加考试的记录。

关键字段：

- `id`
- `paper_id`
- `student_id`
- `total_score`
- `auto_score`
- `subjective_score`
- `status`
- `submitted_at`

说明：

- `status` 主要包括 `PENDING` 与 `SUBMITTED`

### 2.9 `student_answer`

用途：存储学生逐题作答内容和判分结果。

关键字段：

- `id`
- `exam_record_id`
- `question_id`
- `answer_content`
- `is_correct`
- `score`
- `feedback`

说明：

- 客观题由系统自动填写 `is_correct` 和 `score`
- 主观题由 AI 评阅后填写 `score` 和 `feedback`

## 3. 表关系说明

- `course` 1 对多 `knowledge_point`
- `course` 1 对多 `question`
- `knowledge_point` 1 对多 `question`
- `question` 1 对多 `question_option`
- `paper` 多对多 `question`，中间表为 `paper_question`
- `paper` 1 对多 `exam_record`
- `exam_record` 1 对多 `student_answer`
- `sys_user` 1 对多 `exam_record`

## 4. 业务数据流

### 4.1 教师组卷

教师从 `question` 选择题目，生成 `paper`，并通过 `paper_question` 保存试卷结构。

### 4.2 学生答题

学生开始考试时创建或读取 `exam_record`，提交后在 `student_answer` 中保存每题答案。

### 4.3 判分与分析

系统根据 `student_answer` 和 `paper_question` 计算客观题、主观题和总分，再基于 `knowledge_point` 统计知识点掌握度。

## 5. 数据库脚本位置

- 初始化数据库脚本：[scripts/init-db.sql](../scripts/init-db.sql)
- 表结构脚本：[backend/src/main/resources/schema.sql](../backend/src/main/resources/schema.sql)
- 演示数据脚本：[backend/src/main/resources/data.sql](../backend/src/main/resources/data.sql)
