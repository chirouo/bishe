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
@Sql(scripts = "/sql/teacher-question-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/sql/teacher-question-teardown.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class TeacherQuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtUtils jwtUtils;

    @Test
    void shouldListQuestionsWithOptions() throws Exception {
        mockMvc.perform(get("/api/teacher/questions")
                        .header("Authorization", bearerToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].courseName").value("离散数学测试课程A"))
                .andExpect(jsonPath("$.data[0].knowledgePointName").value("命题逻辑"))
                .andExpect(jsonPath("$.data[0].questionType").value("SINGLE_CHOICE"))
                .andExpect(jsonPath("$.data[0].options.length()").value(4));
    }

    @Test
    void shouldCreateSingleChoiceQuestion() throws Exception {
        String requestBody = "{"
                + "\"courseId\":101,"
                + "\"knowledgePointId\":201,"
                + "\"questionType\":\"SINGLE_CHOICE\","
                + "\"stem\":\"如果 p 为真且 q 为假，则 p∧q 的真值为？\","
                + "\"difficulty\":\"EASY\","
                + "\"answer\":\"B\","
                + "\"analysis\":\"合取命题要求两者都为真。\","
                + "\"options\":["
                + "{\"label\":\"A\",\"content\":\"真\"},"
                + "{\"label\":\"B\",\"content\":\"假\"},"
                + "{\"label\":\"C\",\"content\":\"无法判断\"},"
                + "{\"label\":\"D\",\"content\":\"恒真\"}"
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
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].stem").value("如果 p 为真且 q 为假，则 p∧q 的真值为？"));
    }

    @Test
    void shouldRejectQuestionWhenAnswerNotInOptions() throws Exception {
        String requestBody = "{"
                + "\"courseId\":101,"
                + "\"knowledgePointId\":201,"
                + "\"questionType\":\"SINGLE_CHOICE\","
                + "\"stem\":\"测试非法答案\","
                + "\"difficulty\":\"EASY\","
                + "\"answer\":\"D\","
                + "\"options\":["
                + "{\"label\":\"A\",\"content\":\"选项A\"},"
                + "{\"label\":\"B\",\"content\":\"选项B\"}"
                + "]"
                + "}";

        mockMvc.perform(post("/api/teacher/questions")
                        .header("Authorization", bearerToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("答案必须出现在选项标识中"));
    }

    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(get("/api/teacher/questions"))
                .andExpect(status().isUnauthorized());
    }

    private String bearerToken() {
        return "Bearer " + jwtUtils.generateToken(1L, "TEACHER");
    }
}

