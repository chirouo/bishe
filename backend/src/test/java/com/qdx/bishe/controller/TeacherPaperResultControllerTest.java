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
@Sql(scripts = "/sql/teacher-paper-result-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/teacher-paper-result-teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TeacherPaperResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void shouldReturnPaperResultDetail() throws Exception {
        mockMvc.perform(get("/api/teacher/papers/401/results")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paperId").value(401))
                .andExpect(jsonPath("$.data.title").value("离散数学阶段测试一"))
                .andExpect(jsonPath("$.data.submittedCount").value(2))
                .andExpect(jsonPath("$.data.averageScoreRate").value(75.00))
                .andExpect(jsonPath("$.data.records.length()").value(2))
                .andExpect(jsonPath("$.data.records[0].studentName").value("学生甲"))
                .andExpect(jsonPath("$.data.records[0].totalScore").value(88.00))
                .andExpect(jsonPath("$.data.records[0].scoreRate").value(88.00))
                .andExpect(jsonPath("$.data.records[1].studentName").value("学生乙"));
    }

    @Test
    void shouldReturnEmptyPaperResultRecordsWhenNoSubmission() throws Exception {
        mockMvc.perform(get("/api/teacher/papers/402/results")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.paperId").value(402))
                .andExpect(jsonPath("$.data.submittedCount").value(0))
                .andExpect(jsonPath("$.data.averageScoreRate").value(0.00))
                .andExpect(jsonPath("$.data.records.length()").value(0));
    }

    @Test
    void shouldRejectPaperResultRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/teacher/papers/401/results"))
                .andExpect(status().isUnauthorized());
    }

    private String bearerToken() {
        return "Bearer " + jwtUtils.generateToken(11L, "TEACHER");
    }
}
