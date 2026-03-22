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
    (1, '离散数学', '本科毕业设计演示用课程', 1);

INSERT INTO knowledge_point (id, course_id, point_name, parent_id, difficulty, description)
VALUES
    (1, 1, '命题逻辑', 0, 'EASY', '命题、联结词、真值表'),
    (2, 1, '集合与关系', 0, 'MEDIUM', '集合运算、关系性质'),
    (3, 1, '图论基础', 0, 'MEDIUM', '图、路径、连通性');

INSERT INTO question (id, course_id, knowledge_point_id, question_type, stem, difficulty, answer, analysis, source, created_by)
VALUES
    (1, 1, 1, 'SINGLE_CHOICE', '命题 p→q 的逆命题是下列哪一项？', 'EASY', 'C', '逆命题为 q→p。', 'MANUAL', 1),
    (2, 1, 2, 'SHORT_ANSWER', '简述自反关系与对称关系的定义。', 'MEDIUM', '略', '用于演示主观题评阅入口。', 'MANUAL', 1);

INSERT INTO question_option (question_id, option_label, option_content)
VALUES
    (1, 'A', '¬p→¬q'),
    (1, 'B', '¬q→¬p'),
    (1, 'C', 'q→p'),
    (1, 'D', 'p∧q');

INSERT INTO paper (id, course_id, title, description, total_score, status, created_by)
VALUES
    (1, 1, '离散数学阶段测试一', '系统初始化示例试卷', 100.00, 'PUBLISHED', 1);

INSERT INTO paper_question (paper_id, question_id, question_order, score)
VALUES
    (1, 1, 1, 20.00),
    (1, 2, 2, 30.00);

