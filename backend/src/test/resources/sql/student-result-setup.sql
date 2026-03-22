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
    (101, '离散数学', '学生学习分析测试课程', 1);

INSERT INTO knowledge_point (id, course_id, point_name, parent_id, difficulty, description)
VALUES
    (201, 101, '命题逻辑', 0, 'EASY', '命题与联结词'),
    (202, 101, '集合与关系', 0, 'MEDIUM', '关系的基本性质'),
    (203, 101, '图论基础', 0, 'MEDIUM', '图与路径');

INSERT INTO question (id, course_id, knowledge_point_id, question_type, stem, difficulty, answer, analysis, source, created_by)
VALUES
    (301, 101, 201, 'SINGLE_CHOICE', '题目一', 'EASY', 'A', '解析一', 'MANUAL', 1),
    (302, 101, 202, 'SHORT_ANSWER', '题目二', 'MEDIUM', '略', '解析二', 'MANUAL', 1),
    (303, 101, 201, 'SINGLE_CHOICE', '题目三', 'EASY', 'B', '解析三', 'MANUAL', 1),
    (304, 101, 203, 'SHORT_ANSWER', '题目四', 'MEDIUM', '略', '解析四', 'MANUAL', 1);

INSERT INTO paper (id, course_id, title, description, total_score, status, created_by)
VALUES
    (401, 101, '离散数学阶段测试一', '学习分析测试试卷一', 50.00, 'PUBLISHED', 1),
    (402, 101, '离散数学阶段测试二', '学习分析测试试卷二', 30.00, 'PUBLISHED', 1);

INSERT INTO paper_question (id, paper_id, question_id, question_order, score)
VALUES
    (601, 401, 301, 1, 20.00),
    (602, 401, 302, 2, 30.00),
    (603, 402, 303, 1, 20.00),
    (604, 402, 304, 2, 10.00);

INSERT INTO exam_record (id, paper_id, student_id, total_score, auto_score, subjective_score, status, submitted_at)
VALUES
    (501, 401, 2, 32.00, 20.00, 12.00, 'SUBMITTED', '2026-03-18 10:00:00'),
    (502, 402, 2, 25.00, 15.00, 10.00, 'SUBMITTED', '2026-03-20 10:00:00'),
    (503, 401, 3, 40.00, 20.00, 20.00, 'SUBMITTED', '2026-03-20 11:00:00');

INSERT INTO student_answer (id, exam_record_id, question_id, answer_content, is_correct, score, feedback)
VALUES
    (701, 501, 301, 'A', 1, 20.00, '回答正确'),
    (702, 501, 302, '示例主观题答案一', 0, 12.00, '主观题评分'),
    (703, 502, 303, 'B', 1, 15.00, '客观题部分得分'),
    (704, 502, 304, '示例主观题答案二', 0, 10.00, '主观题评分'),
    (705, 503, 301, 'A', 1, 20.00, '其他学生数据'),
    (706, 503, 302, '其他学生答案', 0, 20.00, '其他学生数据');
