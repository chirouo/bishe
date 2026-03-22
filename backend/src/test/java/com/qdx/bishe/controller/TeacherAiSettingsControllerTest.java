package com.qdx.bishe.controller;

import com.qdx.bishe.config.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "app.llm.provider=openai-compatible",
        "app.llm.model=qwen-math-turbo",
        "app.llm.available-models[0]=qwen-math-plus",
        "app.llm.available-models[1]=qwen-math-turbo"
})
@Sql(scripts = "/sql/teacher-ai-settings-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/teacher-ai-settings-teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TeacherAiSettingsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void shouldReturnCurrentAiModelSettings() throws Exception {
        mockMvc.perform(get("/api/teacher/ai/settings")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.provider").value("openai-compatible"))
                .andExpect(jsonPath("$.data.currentModel").value("qwen-math-turbo"))
                .andExpect(jsonPath("$.data.availableModels.length()").value(2))
                .andExpect(jsonPath("$.data.availableModels[0]").value("qwen-math-plus"));
    }

    @Test
    void shouldSwitchAiModelToQwenMathPlus() throws Exception {
        mockMvc.perform(put("/api/teacher/ai/settings/model")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"model\":\"qwen-math-plus\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentModel").value("qwen-math-plus"));

        mockMvc.perform(get("/api/teacher/ai/settings")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.currentModel").value("qwen-math-plus"));
    }

    @Test
    void shouldRejectUnsupportedAiModel() throws Exception {
        mockMvc.perform(put("/api/teacher/ai/settings/model")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"model\":\"unknown-model\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("不支持的模型：unknown-model"));
    }

    private String bearerToken() {
        return "Bearer " + jwtUtils.generateToken(1L, "TEACHER");
    }
}
