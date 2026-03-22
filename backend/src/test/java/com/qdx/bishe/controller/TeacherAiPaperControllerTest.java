package com.qdx.bishe.controller;

import com.qdx.bishe.config.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "/sql/teacher-ai-paper-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/teacher-ai-paper-teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TeacherAiPaperControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void shouldGenerateAiPaperDraftWithWeakKnowledgePointPriority() throws Exception {
        String requestBody = "{"
                + "\"courseId\":101,"
                + "\"questionCount\":2,"
                + "\"scorePerQuestion\":12,"
                + "\"requirements\":\"优先覆盖薄弱知识点\""
                + "}";

        mockMvc.perform(post("/api/teacher/papers/ai-draft")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider").value("mock"))
                .andExpect(jsonPath("$.data.source").value("AI_MOCK"))
                .andExpect(jsonPath("$.data.title").value("AI 组卷草稿 - 离散数学测试课程A（2题）"))
                .andExpect(jsonPath("$.data.strategySummary").value(org.hamcrest.Matchers.containsString("命题逻辑")))
                .andExpect(jsonPath("$.data.questions.length()").value(2))
                .andExpect(jsonPath("$.data.questions[0].knowledgePointName").value("命题逻辑"))
                .andExpect(jsonPath("$.data.questions[0].recommendedScore").value(12))
                .andExpect(jsonPath("$.data.questions[0].recommendationReason").value(org.hamcrest.Matchers.containsString("薄弱知识点")));
    }

    @Test
    void shouldRejectAiPaperDraftWhenQuestionCountTooLarge() throws Exception {
        mockMvc.perform(post("/api/teacher/papers/ai-draft")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":101,\"questionCount\":10,\"scorePerQuestion\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("当前课程题目数量不足，无法生成指定题量的草稿"));
    }

    @Test
    void shouldRejectAiPaperDraftWhenCourseIdMissing() throws Exception {
        mockMvc.perform(post("/api/teacher/papers/ai-draft")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionCount\":2,\"scorePerQuestion\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("课程不能为空"));
    }

    @Test
    void shouldRejectAiPaperDraftRequestWithoutToken() throws Exception {
        mockMvc.perform(post("/api/teacher/papers/ai-draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":101,\"questionCount\":2,\"scorePerQuestion\":10}"))
                .andExpect(status().isUnauthorized());
    }

    private String bearerToken() {
        return "Bearer " + jwtUtils.generateToken(1L, "TEACHER");
    }
}
