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
    (1, 'teacher-settings', '123456', '模型切换教师', 'TEACHER', 1);
