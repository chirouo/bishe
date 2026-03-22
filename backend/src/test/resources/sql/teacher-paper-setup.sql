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
    (101, '离散数学测试课程A', '用于试卷测试的课程 A', 1),
    (102, '离散数学测试课程B', '用于课程错配校验', 1);

INSERT INTO knowledge_point (id, course_id, point_name, parent_id, difficulty, description)
VALUES
    (201, 101, '命题逻辑', 0, 'EASY', '命题与真值表'),
    (202, 101, '集合与关系', 0, 'MEDIUM', '集合运算'),
    (203, 102, '图论基础', 0, 'MEDIUM', '图论知识点');

INSERT INTO question (id, course_id, knowledge_point_id, question_type, stem, difficulty, answer, analysis, source, created_by)
VALUES
    (301, 101, 201, 'SINGLE_CHOICE', '题目一', 'EASY', 'A', '解析一', 'MANUAL', 1),
    (302, 101, 202, 'SINGLE_CHOICE', '题目二', 'MEDIUM', 'B', '解析二', 'MANUAL', 1),
    (303, 102, 203, 'SINGLE_CHOICE', '题目三', 'MEDIUM', 'C', '解析三', 'MANUAL', 1);

INSERT INTO question_option (question_id, option_label, option_content)
VALUES
    (301, 'A', '选项 A1'),
    (301, 'B', '选项 B1'),
    (302, 'A', '选项 A2'),
    (302, 'B', '选项 B2'),
    (303, 'A', '选项 A3'),
    (303, 'C', '选项 C3');

INSERT INTO paper (id, course_id, title, description, total_score, status, created_by)
VALUES
    (401, 101, '离散数学测验一', '已存在试卷', 25.00, 'PUBLISHED', 1);

INSERT INTO paper_question (paper_id, question_id, question_order, score)
VALUES
    (401, 301, 1, 10.00),
    (401, 302, 2, 15.00);

