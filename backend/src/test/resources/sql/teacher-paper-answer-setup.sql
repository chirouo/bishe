DELETE FROM student_answer;
DELETE FROM exam_record;
DELETE FROM paper_question;
DELETE FROM paper;
DELETE FROM question_option;
DELETE FROM question;
DELETE FROM knowledge_point;
DELETE FROM course;
DELETE FROM sys_user WHERE id IN (21, 22, 23);

INSERT INTO sys_user (id, username, password, real_name, role, status)
VALUES
    (21, 'teacher_answer_01', '123456', '测试教师', 'TEACHER', 1),
    (22, 'student_answer_01', '123456', '学生丙', 'STUDENT', 1),
    (23, 'student_answer_02', '123456', '学生丁', 'STUDENT', 1);

INSERT INTO course (id, course_name, description, status)
VALUES
    (101, '离散数学', '学生答卷详情测试课程', 1);

INSERT INTO knowledge_point (id, course_id, point_name, parent_id, difficulty, description)
VALUES
    (201, 101, '命题逻辑', 0, 'EASY', '命题与联结词'),
    (202, 101, '集合与关系', 0, 'MEDIUM', '关系的性质');

INSERT INTO question (id, course_id, knowledge_point_id, question_type, stem, difficulty, answer, analysis, source, created_by)
VALUES
    (301, 101, 201, 'SINGLE_CHOICE', '命题 p→q 的逆命题是下列哪一项？', 'EASY', 'C', '逆命题为 q→p。', 'MANUAL', 21),
    (302, 101, 202, 'SHORT_ANSWER', '简述自反关系与对称关系的定义。', 'MEDIUM', '略', '用于测试主观题回显。', 'MANUAL', 21);

INSERT INTO question_option (question_id, option_label, option_content)
VALUES
    (301, 'A', '¬p→¬q'),
    (301, 'B', '¬q→¬p'),
    (301, 'C', 'q→p'),
    (301, 'D', 'p∧q');

INSERT INTO paper (id, course_id, title, description, total_score, status, created_by)
VALUES
    (401, 101, '离散数学阶段测试一', '学生答卷详情试卷', 100.00, 'PUBLISHED', 21);

INSERT INTO paper_question (id, paper_id, question_id, question_order, score)
VALUES
    (601, 401, 301, 1, 40.00),
    (602, 401, 302, 2, 60.00);

INSERT INTO exam_record (id, paper_id, student_id, total_score, auto_score, subjective_score, status, submitted_at)
VALUES
    (501, 401, 22, 82.00, 40.00, 42.00, 'SUBMITTED', '2026-03-20 15:00:00'),
    (502, 401, 23, 0.00, 0.00, 0.00, 'PENDING', NULL);

INSERT INTO student_answer (id, exam_record_id, question_id, answer_content, is_correct, score, feedback)
VALUES
    (701, 501, 301, 'C', 1, 40.00, '回答正确'),
    (702, 501, 302, '自反关系要求任意元素与自身有序对属于关系，对称关系要求若(a,b)属于关系则(b,a)也属于关系。', 0, 42.00, '定义完整，举例略少'),
    (703, 502, 301, '', 0, 0.00, '未提交');
