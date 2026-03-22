package com.qdx.bishe.controller;

import com.qdx.bishe.config.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/student-result-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/student-result-teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class StudentResultControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void shouldGetStudentResultAnalysis() throws Exception {
        mockMvc.perform(get("/api/student/results/analysis")
                        .header("Authorization", studentToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.completedExamCount").value(2))
                .andExpect(jsonPath("$.data.averageScoreRate").value(73.67))
                .andExpect(jsonPath("$.data.weakKnowledgePointCount").value(1))
                .andExpect(jsonPath("$.data.knowledgePoints.length()").value(3))
                .andExpect(jsonPath("$.data.knowledgePoints[0].pointName").value("集合与关系"))
                .andExpect(jsonPath("$.data.knowledgePoints[0].masteryRate").value(40.00))
                .andExpect(jsonPath("$.data.trends.length()").value(2))
                .andExpect(jsonPath("$.data.trends[0].paperTitle").value("离散数学阶段测试一"))
                .andExpect(jsonPath("$.data.suggestion").value(containsString("集合与关系")));
    }

    @Test
    void shouldRejectStudentResultAnalysisWithoutToken() throws Exception {
        mockMvc.perform(get("/api/student/results/analysis"))
                .andExpect(status().isUnauthorized());
    }

    private String studentToken() {
        return "Bearer " + jwtUtils.generateToken(2L, "STUDENT");
    }
}
