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
    (101, '离散数学', '学生考试列表测试课程', 1);

INSERT INTO knowledge_point (id, course_id, point_name, parent_id, difficulty, description)
VALUES
    (201, 101, '命题逻辑', 0, 'EASY', '命题、联结词与真值表'),
    (202, 101, '关系基础', 0, 'MEDIUM', '关系的基本性质');

INSERT INTO question (id, course_id, knowledge_point_id, question_type, stem, difficulty, answer, analysis, source, created_by)
VALUES
    (301, 101, 201, 'SINGLE_CHOICE', '命题 p→q 的逆命题是下列哪一项？', 'EASY', 'C', '逆命题为 q→p。', 'MANUAL', 1),
    (302, 101, 202, 'SHORT_ANSWER', '简述自反关系与对称关系的定义。', 'MEDIUM', '略', '用于测试主观题展示。', 'MANUAL', 1),
    (303, 101, 201, 'SINGLE_CHOICE', '若命题 p∧q 为真，则下列哪项一定成立？', 'EASY', 'A', 'p 与 q 都为真。', 'MANUAL', 1),
    (304, 101, 202, 'SHORT_ANSWER', '写出空关系的两个性质。', 'MEDIUM', '略', '用于测试提交主观题答案。', 'MANUAL', 1);

INSERT INTO question_option (question_id, option_label, option_content)
VALUES
    (301, 'A', '¬p→¬q'),
    (301, 'B', '¬q→¬p'),
    (301, 'C', 'q→p'),
    (301, 'D', 'p∧q'),
    (303, 'A', 'p 与 q 都为真'),
    (303, 'B', 'p 为假'),
    (303, 'C', 'q 为假'),
    (303, 'D', 'p→q 为假');

INSERT INTO paper (id, course_id, title, description, total_score, status, created_by)
VALUES
    (401, 101, '离散数学阶段测试一', '学生已提交的试卷', 50.00, 'PUBLISHED', 1),
    (402, 101, '离散数学阶段测试二', '学生尚未作答的试卷', 40.00, 'PUBLISHED', 1),
    (403, 101, '离散数学阶段测试三', '草稿试卷不应展示', 60.00, 'DRAFT', 1);

INSERT INTO paper_question (id, paper_id, question_id, question_order, score)
VALUES
    (601, 401, 301, 1, 20.00),
    (602, 401, 302, 2, 30.00),
    (603, 402, 303, 1, 15.00),
    (604, 402, 304, 2, 25.00);

INSERT INTO exam_record (id, paper_id, student_id, total_score, auto_score, subjective_score, status, submitted_at)
VALUES
    (501, 401, 2, 32.00, 20.00, 12.00, 'SUBMITTED', '2026-03-20 18:00:00'),
    (502, 401, 3, 28.00, 20.00, 8.00, 'SUBMITTED', '2026-03-20 18:20:00');

INSERT INTO student_answer (id, exam_record_id, question_id, answer_content, is_correct, score, feedback)
VALUES
    (701, 501, 301, 'C', 1, 20.00, '回答正确'),
    (702, 501, 302, '自反关系要求任意元素与自身有序对属于关系，对称关系要求若(a,b)属于关系则(b,a)也属于关系。', 0, 12.00, '答案基本正确'),
    (703, 502, 301, 'C', 1, 20.00, '回答正确'),
    (704, 502, 302, '示例答案', 0, 8.00, '示例反馈');
