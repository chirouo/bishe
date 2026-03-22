INSERT IGNORE INTO sys_user (id, username, password, real_name, role, status)
VALUES
    (1, 'teacher01', '123456', '张老师', 'TEACHER', 1),
    (2, 'student01', '123456', '学生一号', 'STUDENT', 1);

INSERT IGNORE INTO course (id, course_name, description, status)
VALUES
    (1, '离散数学', '本科毕业设计演示用课程', 1);

INSERT IGNORE INTO knowledge_point (id, course_id, point_name, parent_id, difficulty, description)
VALUES
    (1, 1, '命题逻辑', 0, 'EASY', '命题、联结词、真值表'),
    (2, 1, '集合与关系', 0, 'MEDIUM', '集合运算、关系性质'),
    (3, 1, '图论基础', 0, 'MEDIUM', '图、路径、连通性');

INSERT IGNORE INTO question (id, course_id, knowledge_point_id, question_type, stem, difficulty, answer, analysis, source, created_by)
VALUES
    (1, 1, 1, 'SINGLE_CHOICE', '命题 p→q 的逆命题是下列哪一项？', 'EASY', 'C', '逆命题为 q→p。', 'MANUAL', 1),
    (2, 1, 2, 'SHORT_ANSWER', '简述自反关系与对称关系的定义。', 'MEDIUM', '略', '用于演示主观题评阅入口。', 'MANUAL', 1);

DELETE qo_keep
FROM question_option qo_keep
INNER JOIN question_option qo_drop
    ON qo_keep.question_id = qo_drop.question_id
    AND qo_keep.option_label = qo_drop.option_label
    AND qo_keep.id > qo_drop.id;

INSERT INTO question_option (question_id, option_label, option_content)
SELECT seed.question_id, seed.option_label, seed.option_content
FROM (
    SELECT 1 AS question_id, 'A' AS option_label, '¬p→¬q' AS option_content
    UNION ALL
    SELECT 1, 'B', '¬q→¬p'
    UNION ALL
    SELECT 1, 'C', 'q→p'
    UNION ALL
    SELECT 1, 'D', 'p∧q'
) AS seed
WHERE NOT EXISTS (
    SELECT 1
    FROM question_option existing
    WHERE existing.question_id = seed.question_id
      AND existing.option_label = seed.option_label
);

INSERT IGNORE INTO paper (id, course_id, title, description, total_score, status, created_by)
VALUES
    (1, 1, '离散数学阶段测试一', '系统初始化示例试卷', 100.00, 'PUBLISHED', 1);

DELETE pq_keep
FROM paper_question pq_keep
INNER JOIN paper_question pq_drop
    ON pq_keep.paper_id = pq_drop.paper_id
    AND pq_keep.question_id = pq_drop.question_id
    AND pq_keep.id > pq_drop.id;

INSERT INTO paper_question (paper_id, question_id, question_order, score)
SELECT seed.paper_id, seed.question_id, seed.question_order, seed.score
FROM (
    SELECT 1 AS paper_id, 1 AS question_id, 1 AS question_order, 20.00 AS score
    UNION ALL
    SELECT 1, 2, 2, 30.00
) AS seed
WHERE NOT EXISTS (
    SELECT 1
    FROM paper_question existing
    WHERE existing.paper_id = seed.paper_id
      AND existing.question_id = seed.question_id
);
