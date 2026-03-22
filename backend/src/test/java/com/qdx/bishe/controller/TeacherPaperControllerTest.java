package com.qdx.bishe.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdx.bishe.config.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/teacher-paper-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/teacher-paper-teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TeacherPaperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldListExistingPapers() throws Exception {
        mockMvc.perform(get("/api/teacher/papers")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("离散数学测验一"))
                .andExpect(jsonPath("$.data[0].questionCount").value(2))
                .andExpect(jsonPath("$.data[0].totalScore").value(25.00));
    }

    @Test
    void shouldCreatePaperFromQuestionSelection() throws Exception {
        String requestBody = "{"
                + "\"courseId\":101,"
                + "\"title\":\"离散数学单元卷\","
                + "\"description\":\"手动组卷测试\","
                + "\"status\":\"DRAFT\","
                + "\"questions\":["
                + "{\"questionId\":301,\"score\":10},"
                + "{\"questionId\":302,\"score\":15}"
                + "]"
                + "}";

        mockMvc.perform(post("/api/teacher/papers")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNumber());

        mockMvc.perform(get("/api/teacher/papers")
                        .param("courseId", "101")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("离散数学单元卷"))
                .andExpect(jsonPath("$.data[0].questionCount").value(2))
                .andExpect(jsonPath("$.data[0].totalScore").value(25.00));
    }

    @Test
    void shouldRejectPaperWhenQuestionCourseMismatch() throws Exception {
        String requestBody = "{"
                + "\"courseId\":101,"
                + "\"title\":\"错误试卷\","
                + "\"status\":\"DRAFT\","
                + "\"questions\":["
                + "{\"questionId\":301,\"score\":10},"
                + "{\"questionId\":303,\"score\":10}"
                + "]"
                + "}";

        mockMvc.perform(post("/api/teacher/papers")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("所选题目必须全部属于当前课程"));
    }

    @Test
    void shouldPublishDraftPaper() throws Exception {
        String requestBody = "{"
                + "\"courseId\":101,"
                + "\"title\":\"待发布试卷\","
                + "\"description\":\"草稿试卷\","
                + "\"status\":\"DRAFT\","
                + "\"questions\":["
                + "{\"questionId\":301,\"score\":10},"
                + "{\"questionId\":302,\"score\":15}"
                + "]"
                + "}";

        mockMvc.perform(post("/api/teacher/papers")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNumber());

        String listResponse = mockMvc.perform(get("/api/teacher/papers")
                        .param("courseId", "101")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode listRoot = objectMapper.readTree(listResponse);
        long paperId = listRoot.path("data").get(0).path("id").asLong();

        mockMvc.perform(post("/api/teacher/papers/" + paperId + "/publish")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mockMvc.perform(get("/api/teacher/papers")
                        .param("courseId", "101")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("待发布试卷"))
                .andExpect(jsonPath("$.data[0].status").value("PUBLISHED"));
    }

    @Test
    void shouldRejectPublishWhenPaperDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/teacher/papers/999/publish")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("试卷不存在"));
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/teacher/papers"))
                .andExpect(status().isUnauthorized());
    }

    private String bearerToken() {
        return "Bearer " + jwtUtils.generateToken(1L, "TEACHER");
    }
}
