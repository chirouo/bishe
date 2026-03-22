DELETE FROM student_answer;
DELETE FROM exam_record;
DELETE FROM paper_question;
DELETE FROM paper;
DELETE FROM question_option;
DELETE FROM question;
DELETE FROM knowledge_point;
DELETE FROM course;
DELETE FROM sys_user WHERE id IN (11, 12, 13);

INSERT INTO sys_user (id, username, password, real_name, role, status)
VALUES
    (11, 'teacher_result_01', '123456', '测试教师', 'TEACHER', 1),
    (12, 'student_result_01', '123456', '学生甲', 'STUDENT', 1),
    (13, 'student_result_02', '123456', '学生乙', 'STUDENT', 1);

INSERT INTO course (id, course_name, description, status)
VALUES
    (101, '离散数学', '试卷成绩明细测试课程', 1);

INSERT INTO knowledge_point (id, course_id, point_name, parent_id, difficulty, description)
VALUES
    (201, 101, '命题逻辑', 0, 'EASY', '命题与联结词'),
    (202, 101, '集合与关系', 0, 'MEDIUM', '关系的性质');

INSERT INTO question (id, course_id, knowledge_point_id, question_type, stem, difficulty, answer, analysis, source, created_by)
VALUES
    (301, 101, 201, 'SINGLE_CHOICE', '题目一', 'EASY', 'A', '解析一', 'MANUAL', 11),
    (302, 101, 202, 'SHORT_ANSWER', '题目二', 'MEDIUM', '略', '解析二', 'MANUAL', 11);

INSERT INTO paper (id, course_id, title, description, total_score, status, created_by)
VALUES
    (401, 101, '离散数学阶段测试一', '有提交记录的试卷', 100.00, 'PUBLISHED', 11),
    (402, 101, '离散数学阶段测试二', '暂无提交记录的试卷', 50.00, 'PUBLISHED', 11);

INSERT INTO paper_question (id, paper_id, question_id, question_order, score)
VALUES
    (601, 401, 301, 1, 40.00),
    (602, 401, 302, 2, 60.00),
    (603, 402, 301, 1, 20.00),
    (604, 402, 302, 2, 30.00);

INSERT INTO exam_record (id, paper_id, student_id, total_score, auto_score, subjective_score, status, submitted_at)
VALUES
    (501, 401, 12, 88.00, 40.00, 48.00, 'SUBMITTED', '2026-03-20 09:00:00'),
    (502, 401, 13, 62.00, 20.00, 42.00, 'SUBMITTED', '2026-03-20 09:10:00'),
    (503, 401, 2, 0.00, 0.00, 0.00, 'PENDING', NULL);
