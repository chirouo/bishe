DELETE FROM student_answer;
DELETE FROM exam_record;
DELETE FROM paper_question;
DELETE FROM paper;
DELETE FROM question_option;
DELETE FROM question;
DELETE FROM knowledge_point;
DELETE FROM course;

INSERT INTO course (id, course_name, description, status)
VALUES
    (101, '离散数学测试课程A', '用于接口测试的 mock 课程 A', 1),
    (102, '离散数学测试课程B', '用于接口测试的 mock 课程 B', 1),
    (103, '离散数学停用课程', '不应出现在课程列表中', 0);

INSERT INTO knowledge_point (id, course_id, point_name, parent_id, difficulty, description)
VALUES
    (201, 101, '命题逻辑', 0, 'EASY', '命题、联结词、真值表'),
    (202, 101, '谓词逻辑', 0, 'MEDIUM', '量词与谓词公式'),
    (203, 102, '图论基础', 0, 'MEDIUM', '图、路径、连通性');

INSERT INTO question (id, course_id, knowledge_point_id, question_type, stem, difficulty, answer, analysis, source, created_by)
VALUES
    (301, 101, 201, 'SINGLE_CHOICE', '测试题目 1', 'EASY', 'A', '测试解析 1', 'MANUAL', 1),
    (302, 102, 203, 'SHORT_ANSWER', '测试题目 2', 'MEDIUM', '略', '测试解析 2', 'MANUAL', 1);

INSERT INTO paper (id, course_id, title, description, total_score, status, created_by)
VALUES
    (401, 101, '测试已发布试卷', '用于统计已发布试卷数量', 100.00, 'PUBLISHED', 1),
    (402, 102, '测试草稿试卷', '不应计入已发布试卷数量', 100.00, 'DRAFT', 1);

