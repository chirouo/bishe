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
    (101, '离散数学测试课程A', '用于 AI 组卷测试', 1),
    (102, '离散数学测试课程B', '用于课程隔离校验', 1);

INSERT INTO knowledge_point (id, course_id, point_name, parent_id, difficulty, description)
VALUES
    (201, 101, '命题逻辑', 0, 'EASY', '命题与真值表'),
    (202, 101, '集合与关系', 0, 'MEDIUM', '集合与关系'),
    (203, 101, '图论基础', 0, 'MEDIUM', '图论基础'),
    (204, 102, '谓词逻辑', 0, 'MEDIUM', '用于其他课程');

INSERT INTO question (id, course_id, knowledge_point_id, question_type, stem, difficulty, answer, analysis, source, created_by)
VALUES
    (301, 101, 201, 'SINGLE_CHOICE', '命题逻辑题一', 'EASY', 'A', '解析一', 'MANUAL', 1),
    (302, 101, 202, 'SINGLE_CHOICE', '集合与关系题一', 'MEDIUM', 'B', '解析二', 'MANUAL', 1),
    (303, 101, 201, 'SHORT_ANSWER', '命题逻辑题二', 'MEDIUM', '略', '解析三', 'MANUAL', 1),
    (304, 101, 203, 'SINGLE_CHOICE', '图论基础题一', 'MEDIUM', 'C', '解析四', 'MANUAL', 1),
    (305, 102, 204, 'SINGLE_CHOICE', '其他课程题目', 'EASY', 'A', '解析五', 'MANUAL', 1);

INSERT INTO question_option (question_id, option_label, option_content)
VALUES
    (301, 'A', 'A1'),
    (301, 'B', 'B1'),
    (302, 'A', 'A2'),
    (302, 'B', 'B2'),
    (304, 'A', 'A4'),
    (304, 'C', 'C4'),
    (305, 'A', 'A5'),
    (305, 'B', 'B5');

INSERT INTO paper (id, course_id, title, description, total_score, status, created_by)
VALUES
    (401, 101, '历史测试卷', '用于生成班级掌握度', 35.00, 'PUBLISHED', 1);

INSERT INTO paper_question (id, paper_id, question_id, question_order, score)
VALUES
    (601, 401, 301, 1, 10.00),
    (602, 401, 302, 2, 15.00),
    (603, 401, 304, 3, 10.00);

INSERT INTO exam_record (id, paper_id, student_id, total_score, auto_score, subjective_score, status, submitted_at)
VALUES
    (501, 401, 2, 21.00, 21.00, 0.00, 'SUBMITTED', '2026-03-20 10:00:00'),
    (502, 401, 3, 22.00, 22.00, 0.00, 'SUBMITTED', '2026-03-20 10:30:00');

INSERT INTO student_answer (id, exam_record_id, question_id, answer_content, is_correct, score, feedback)
VALUES
    (701, 501, 301, 'B', 0, 2.00, '命题逻辑得分偏低'),
    (702, 501, 302, 'B', 1, 15.00, '回答正确'),
    (703, 501, 304, 'C', 1, 8.00, '图论基础整体较稳'),
    (704, 502, 301, 'C', 0, 3.00, '命题逻辑仍需巩固'),
    (705, 502, 302, 'B', 1, 13.00, '基本正确'),
    (706, 502, 304, 'C', 1, 9.00, '图论掌握较稳');
