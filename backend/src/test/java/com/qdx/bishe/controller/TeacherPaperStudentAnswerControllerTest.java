package com.qdx.bishe.controller;

import com.qdx.bishe.config.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/teacher-paper-answer-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/teacher-paper-answer-teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TeacherPaperStudentAnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void shouldReturnStudentAnswerDetail() throws Exception {
        mockMvc.perform(get("/api/teacher/papers/401/results/22")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paperId").value(401))
                .andExpect(jsonPath("$.data.studentName").value("学生丙"))
                .andExpect(jsonPath("$.data.studentTotalScore").value(82.00))
                .andExpect(jsonPath("$.data.questions.length()").value(2))
                .andExpect(jsonPath("$.data.questions[0].questionType").value("SINGLE_CHOICE"))
                .andExpect(jsonPath("$.data.questions[0].options.length()").value(4))
                .andExpect(jsonPath("$.data.questions[0].studentAnswer").value("C"))
                .andExpect(jsonPath("$.data.questions[0].gainedScore").value(40.00))
                .andExpect(jsonPath("$.data.questions[1].correctAnswer").value("略"))
                .andExpect(jsonPath("$.data.questions[1].feedback").value("定义完整，举例略少"));
    }

    @Test
    void shouldRejectWhenStudentAnswerDetailNotFound() throws Exception {
        mockMvc.perform(get("/api/teacher/papers/401/results/23")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("未找到该学生的已提交答卷"));
    }

    @Test
    void shouldRejectStudentAnswerRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/teacher/papers/401/results/22"))
                .andExpect(status().isUnauthorized());
    }

    private String bearerToken() {
        return "Bearer " + jwtUtils.generateToken(21L, "TEACHER");
    }
}
