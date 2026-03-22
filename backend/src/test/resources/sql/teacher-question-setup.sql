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
    (101, '离散数学测试课程A', '用于题库测试的 mock 课程', 1);

INSERT INTO knowledge_point (id, course_id, point_name, parent_id, difficulty, description)
VALUES
    (201, 101, '命题逻辑', 0, 'EASY', '命题与真值表'),
    (202, 101, '集合与关系', 0, 'MEDIUM', '集合运算');

INSERT INTO question (id, course_id, knowledge_point_id, question_type, stem, difficulty, answer, analysis, source, created_by)
VALUES
    (301, 101, 201, 'SINGLE_CHOICE', '已存在的单选题', 'EASY', 'A', '测试解析', 'MANUAL', 1);

INSERT INTO question_option (question_id, option_label, option_content)
VALUES
    (301, 'A', '正确答案'),
    (301, 'B', '错误选项 1'),
    (301, 'C', '错误选项 2'),
    (301, 'D', '错误选项 3');

