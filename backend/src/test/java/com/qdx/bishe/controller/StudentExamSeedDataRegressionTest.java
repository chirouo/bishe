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
@Sql(scripts = {"/sql/student-exam-seed-regression-setup.sql", "/data.sql", "/data.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = {"/sql/student-exam-seed-regression-teardown.sql", "/data.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class StudentExamSeedDataRegressionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void shouldNotDuplicateQuestionsOrOptionsAfterDataSqlRunsTwice() throws Exception {
        mockMvc.perform(get("/api/student/exams/1")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.questions.length()").value(2))
                .andExpect(jsonPath("$.data.questions[0].questionId").value(1))
                .andExpect(jsonPath("$.data.questions[0].options.length()").value(4))
                .andExpect(jsonPath("$.data.questions[1].questionId").value(2))
                .andExpect(jsonPath("$.data.questions[1].options.length()").value(0));
    }

    private String bearerToken() {
        return "Bearer " + jwtUtils.generateToken(2L, "STUDENT");
    }
}
