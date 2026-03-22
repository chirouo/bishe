package com.qdx.bishe.controller;

import com.qdx.bishe.config.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/teacher-resource-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/teacher-resource-teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TeacherResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void shouldReturnDashboardOverview() throws Exception {
        mockMvc.perform(get("/api/teacher/dashboard/overview")
                        .header("Authorization", bearerToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courseCount").value(2))
                .andExpect(jsonPath("$.data.knowledgePointCount").value(3))
                .andExpect(jsonPath("$.data.questionCount").value(2))
                .andExpect(jsonPath("$.data.publishedPaperCount").value(1));
    }

    @Test
    void shouldListActiveCoursesOnly() throws Exception {
        mockMvc.perform(get("/api/teacher/courses")
                        .header("Authorization", bearerToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].courseName").value("离散数学测试课程A"))
                .andExpect(jsonPath("$.data[1].courseName").value("离散数学测试课程B"));
    }

    @Test
    void shouldFilterKnowledgePointsByCourse() throws Exception {
        mockMvc.perform(get("/api/teacher/knowledge-points")
                        .param("courseId", "101")
                        .header("Authorization", bearerToken())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].courseName").value("离散数学测试课程A"))
                .andExpect(jsonPath("$.data[0].pointName").value("命题逻辑"))
                .andExpect(jsonPath("$.data[1].pointName").value("谓词逻辑"));
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/teacher/courses"))
                .andExpect(status().isUnauthorized());
    }

    private String bearerToken() {
        return "Bearer " + jwtUtils.generateToken(1L, "TEACHER");
    }
}

