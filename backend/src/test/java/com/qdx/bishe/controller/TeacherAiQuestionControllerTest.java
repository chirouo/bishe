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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/teacher-ai-question-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/teacher-ai-question-teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TeacherAiQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void shouldGenerateAiQuestionDraftWithMockProvider() throws Exception {
        String requestBody = "{"
                + "\"courseId\":101,"
                + "\"knowledgePointId\":201,"
                + "\"questionType\":\"SINGLE_CHOICE\","
                + "\"difficulty\":\"EASY\","
                + "\"requirements\":\"强调定义辨析\""
                + "}";

        mockMvc.perform(post("/api/teacher/ai/questions/draft")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider").value("mock"))
                .andExpect(jsonPath("$.data.source").value("AI_MOCK"))
                .andExpect(jsonPath("$.data.courseId").value(101))
                .andExpect(jsonPath("$.data.knowledgePointId").value(201))
                .andExpect(jsonPath("$.data.questionType").value("SINGLE_CHOICE"))
                .andExpect(jsonPath("$.data.options.length()").value(4))
                .andExpect(jsonPath("$.data.answer").value("B"));
    }

    @Test
    void shouldRejectAiDraftWhenKnowledgePointNotInCourse() throws Exception {
        String requestBody = "{"
                + "\"courseId\":101,"
                + "\"knowledgePointId\":202,"
                + "\"questionType\":\"SINGLE_CHOICE\","
                + "\"difficulty\":\"EASY\""
                + "}";

        mockMvc.perform(post("/api/teacher/ai/questions/draft")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("知识点不存在或不属于当前课程"));
    }

    @Test
    void shouldCreateAiMockQuestionSource() throws Exception {
        String requestBody = "{"
                + "\"courseId\":101,"
                + "\"knowledgePointId\":201,"
                + "\"questionType\":\"SINGLE_CHOICE\","
                + "\"stem\":\"Mock 生成题目\","
                + "\"difficulty\":\"EASY\","
                + "\"answer\":\"A\","
                + "\"analysis\":\"Mock 解析\","
                + "\"source\":\"AI_MOCK\","
                + "\"options\":["
                + "{\"label\":\"A\",\"content\":\"正确\"},"
                + "{\"label\":\"B\",\"content\":\"错误1\"},"
                + "{\"label\":\"C\",\"content\":\"错误2\"},"
                + "{\"label\":\"D\",\"content\":\"错误3\"}"
                + "]"
                + "}";

        mockMvc.perform(post("/api/teacher/questions")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNumber());

        mockMvc.perform(get("/api/teacher/questions")
                        .param("courseId", "101")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].stem").value("Mock 生成题目"))
                .andExpect(jsonPath("$.data[0].source").value("AI_MOCK"));
    }

    @Test
    void shouldRejectAiDraftWhenQuestionTypeUnsupported() throws Exception {
        mockMvc.perform(post("/api/teacher/ai/questions/draft")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":101,\"knowledgePointId\":201,\"questionType\":\"SHORT_ANSWER\",\"difficulty\":\"MEDIUM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("当前版本仅支持 AI 生成单选题草稿"));
    }

    @Test
    void shouldRejectAiDraftRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/api/teacher/ai/questions/draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":101,\"knowledgePointId\":201,\"questionType\":\"SINGLE_CHOICE\",\"difficulty\":\"EASY\"}"))
                .andExpect(status().isUnauthorized());
    }

    private String bearerToken() {
        return "Bearer " + jwtUtils.generateToken(1L, "TEACHER");
    }
}
