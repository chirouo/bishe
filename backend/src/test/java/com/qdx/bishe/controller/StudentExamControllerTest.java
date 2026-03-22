package com.qdx.bishe.controller;

import com.qdx.bishe.config.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/student-exam-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/student-exam-teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class StudentExamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetSqlMode() {
        jdbcTemplate.execute("SET sql_mode=''");
    }

    @Test
    void shouldListPublishedExamsForStudent() throws Exception {
        mockMvc.perform(get("/api/student/exams")
                        .header("Authorization", studentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("离散数学阶段测试二"))
                .andExpect(jsonPath("$.data[0].examStatus").value("NOT_STARTED"))
                .andExpect(jsonPath("$.data[1].title").value("离散数学阶段测试一"))
                .andExpect(jsonPath("$.data[1].examStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$.data[1].studentScore").value(32.00));
    }

    @Test
    void shouldGetStudentExamDetailWithExistingAnswers() throws Exception {
        mockMvc.perform(get("/api/student/exams/401")
                        .header("Authorization", studentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paperId").value(401))
                .andExpect(jsonPath("$.data.examStatus").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.studentScore").value(32.00))
                .andExpect(jsonPath("$.data.autoScore").value(20.00))
                .andExpect(jsonPath("$.data.subjectiveScore").value(12.00))
                .andExpect(jsonPath("$.data.questions.length()").value(2))
                .andExpect(jsonPath("$.data.questions[0].questionType").value("SINGLE_CHOICE"))
                .andExpect(jsonPath("$.data.questions[0].options.length()").value(4))
                .andExpect(jsonPath("$.data.questions[0].answerContent").value("C"))
                .andExpect(jsonPath("$.data.questions[0].gainedScore").value(20.00))
                .andExpect(jsonPath("$.data.questions[0].feedback").value("回答正确"))
                .andExpect(jsonPath("$.data.questions[1].gainedScore").value(12.00))
                .andExpect(jsonPath("$.data.questions[1].feedback").value("答案基本正确"))
                .andExpect(jsonPath("$.data.questions[1].answerContent").value("自反关系要求任意元素与自身有序对属于关系，对称关系要求若(a,b)属于关系则(b,a)也属于关系。"));
    }

    @Test
    void shouldSubmitStudentExamAndAiGradeShortAnswer() throws Exception {
        mockMvc.perform(post("/api/student/exams/402/submit")
                        .header("Authorization", studentToken())
                        .contentType("application/json")
                        .content("{\"answers\":[{\"questionId\":303,\"answerContent\":\"A\"},{\"questionId\":304,\"answerContent\":\"空关系具有自反性和对称性。\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.autoScore").value(15.00))
                .andExpect(jsonPath("$.data.subjectiveScore").value(20.00))
                .andExpect(jsonPath("$.data.totalScore").value(35.00));

        Integer recordCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM exam_record WHERE paper_id = 402 AND student_id = 2",
                Integer.class
        );
        Integer answerCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM student_answer sa " +
                        "INNER JOIN exam_record er ON sa.exam_record_id = er.id " +
                        "WHERE er.paper_id = 402 AND er.student_id = 2",
                Integer.class
        );
        Double choiceScore = jdbcTemplate.queryForObject(
                "SELECT sa.score FROM student_answer sa " +
                        "INNER JOIN exam_record er ON sa.exam_record_id = er.id " +
                        "WHERE er.paper_id = 402 AND er.student_id = 2 AND sa.question_id = 303",
                Double.class
        );
        Double shortAnswerScore = jdbcTemplate.queryForObject(
                "SELECT sa.score FROM student_answer sa " +
                        "INNER JOIN exam_record er ON sa.exam_record_id = er.id " +
                        "WHERE er.paper_id = 402 AND er.student_id = 2 AND sa.question_id = 304",
                Double.class
        );
        String shortAnswerFeedback = jdbcTemplate.queryForObject(
                "SELECT sa.feedback FROM student_answer sa " +
                        "INNER JOIN exam_record er ON sa.exam_record_id = er.id " +
                        "WHERE er.paper_id = 402 AND er.student_id = 2 AND sa.question_id = 304",
                String.class
        );

        org.junit.jupiter.api.Assertions.assertEquals(1, recordCount);
        org.junit.jupiter.api.Assertions.assertEquals(2, answerCount);
        org.junit.jupiter.api.Assertions.assertEquals(15.00, choiceScore);
        org.junit.jupiter.api.Assertions.assertEquals(20.00, shortAnswerScore);
        org.junit.jupiter.api.Assertions.assertTrue(shortAnswerFeedback.startsWith("Mock 智能评阅"));
    }

    @Test
    void shouldGiveZeroScoreForBlankShortAnswer() throws Exception {
        mockMvc.perform(post("/api/student/exams/402/submit")
                        .header("Authorization", studentToken())
                        .contentType("application/json")
                        .content("{\"answers\":[{\"questionId\":303,\"answerContent\":\"B\"},{\"questionId\":304,\"answerContent\":\"\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.autoScore").value(0.00))
                .andExpect(jsonPath("$.data.subjectiveScore").value(0.00))
                .andExpect(jsonPath("$.data.totalScore").value(0.00));

        Double shortAnswerScore = jdbcTemplate.queryForObject(
                "SELECT sa.score FROM student_answer sa " +
                        "INNER JOIN exam_record er ON sa.exam_record_id = er.id " +
                        "WHERE er.paper_id = 402 AND er.student_id = 2 AND sa.question_id = 304",
                Double.class
        );
        String shortAnswerFeedback = jdbcTemplate.queryForObject(
                "SELECT sa.feedback FROM student_answer sa " +
                        "INNER JOIN exam_record er ON sa.exam_record_id = er.id " +
                        "WHERE er.paper_id = 402 AND er.student_id = 2 AND sa.question_id = 304",
                String.class
        );

        org.junit.jupiter.api.Assertions.assertEquals(0.00, shortAnswerScore);
        org.junit.jupiter.api.Assertions.assertTrue(shortAnswerFeedback.contains("未作答"));
    }

    @Test
    void shouldRejectRepeatedSubmitForSubmittedExam() throws Exception {
        mockMvc.perform(post("/api/student/exams/401/submit")
                        .header("Authorization", studentToken())
                        .contentType("application/json")
                        .content("{\"answers\":[{\"questionId\":301,\"answerContent\":\"C\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("试卷已提交，不能重复提交"));
    }

    @Test
    void shouldRejectStudentExamRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/student/exams"))
                .andExpect(status().isUnauthorized());
    }

    private String studentToken() {
        return "Bearer " + jwtUtils.generateToken(2L, "STUDENT");
    }
}
