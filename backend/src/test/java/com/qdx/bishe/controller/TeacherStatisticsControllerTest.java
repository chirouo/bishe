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
@Sql(scripts = "/sql/teacher-statistics-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/teacher-statistics-teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TeacherStatisticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void shouldReturnTeacherStatisticsAnalysis() throws Exception {
        mockMvc.perform(get("/api/teacher/statistics/analysis")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.submittedExamCount").value(3))
                .andExpect(jsonPath("$.data.studentCount").value(2))
                .andExpect(jsonPath("$.data.averageScoreRate").value(66.67))
                .andExpect(jsonPath("$.data.weakKnowledgePointCount").value(1))
                .andExpect(jsonPath("$.data.papers.length()").value(2))
                .andExpect(jsonPath("$.data.papers[0].paperTitle").value("离散数学阶段测试一"))
                .andExpect(jsonPath("$.data.papers[0].averageScoreRate").value(60.00))
                .andExpect(jsonPath("$.data.knowledgePoints.length()").value(3))
                .andExpect(jsonPath("$.data.knowledgePoints[0].pointName").value("集合与关系"))
                .andExpect(jsonPath("$.data.knowledgePoints[0].masteryRate").value(50.00))
                .andExpect(jsonPath("$.data.suggestion").value(containsString("集合与关系")));
    }

    @Test
    void shouldRejectTeacherStatisticsRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/teacher/statistics/analysis"))
                .andExpect(status().isUnauthorized());
    }

    private String bearerToken() {
        return "Bearer " + jwtUtils.generateToken(1L, "TEACHER");
    }
}
