DELETE FROM student_answer;
DELETE FROM exam_record;
DELETE FROM paper_question;
DELETE FROM paper;
DELETE FROM question_option;
DELETE FROM question;
DELETE FROM knowledge_point;
DELETE FROM course;
DELETE FROM sys_user;

INSERT INTO sys_user (id, username, password, real_name, role, status)
VALUES
    (1, 'teacher01', '123456', '王老师', 'TEACHER', 1),
    (2, 'student01', '123456', '张同学', 'STUDENT', 1),
    (3, 'student02', '123456', '李同学', 'STUDENT', 1);
