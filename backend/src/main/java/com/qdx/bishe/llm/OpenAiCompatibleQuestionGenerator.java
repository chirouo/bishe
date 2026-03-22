package com.qdx.bishe.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdx.bishe.common.BusinessException;
import com.qdx.bishe.config.LlmProperties;
import com.qdx.bishe.dto.AiQuestionDraftDto;
import com.qdx.bishe.dto.GenerateAiQuestionDraftRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiCompatibleQuestionGenerator implements LlmQuestionGenerator {

    private final LlmProperties llmProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public String getProviderName() {
        return "openai-compatible";
    }

    @Override
    public AiQuestionDraftDto generateQuestionDraft(GenerateAiQuestionDraftRequest request,
                                                    String courseName,
                                                    String knowledgePointName) {
        if (isBlank(llmProperties.getBaseUrl()) || isBlank(llmProperties.getApiKey()) || isBlank(llmProperties.getModel())) {
            throw new BusinessException("大模型配置不完整，请先设置 baseUrl、apiKey 和 model");
        }

        try {
            RestTemplate restTemplate = restTemplateBuilder
                    .setConnectTimeout(Duration.ofMillis(llmProperties.getConnectTimeoutMs()))
                    .setReadTimeout(Duration.ofMillis(llmProperties.getReadTimeoutMs()))
                    .build();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(llmProperties.getApiKey());

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", llmProperties.getModel());
            payload.put("temperature", llmProperties.getTemperature());
            payload.put("enable_thinking", llmProperties.getEnableThinking());
            payload.put("response_format", jsonObjectFormat());
            payload.put("messages", Arrays.asList(
                    message("system", "你是离散数学题库生成助手。必须返回合法 json，不要输出 markdown、latex、反斜杠转义或额外说明。"),
                    message("user", buildPrompt(request, courseName, knowledgePointName))
            ));

            JsonNode response = restTemplate.postForObject(
                    llmProperties.getBaseUrl().replaceAll("/$", "") + "/chat/completions",
                    new HttpEntity<>(payload, headers),
                    JsonNode.class
            );
            if (response == null) {
                throw new BusinessException("大模型未返回内容");
            }

            JsonNode contentNode = response.path("choices").path(0).path("message").path("content");
            String content = sanitizeJsonContent(contentNode.asText());
            return objectMapper.readValue(content, AiQuestionDraftDto.class);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("大模型调用失败：" + ex.getMessage() + "。若持续出现结构化输出异常，建议切换为当前可选的其他模型，例如 qwen-math-plus。");
        }
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String buildPrompt(GenerateAiQuestionDraftRequest request, String courseName, String knowledgePointName) {
        return "请为课程“" + courseName + "”生成一道离散数学单选题。\n"
                + "知识点：" + knowledgePointName + "\n"
                + "难度：" + request.getDifficulty() + "\n"
                + "补充要求：" + (request.getRequirements() == null ? "" : request.getRequirements()) + "\n"
                + "请只返回 json，题干和解析使用普通中文，不要输出 latex 或 markdown。"
                + "请严格使用如下 JSON 结构："
                + "{\"provider\":\"openai-compatible\",\"source\":\"AI_GENERATED\",\"courseId\":" + request.getCourseId()
                + ",\"knowledgePointId\":" + request.getKnowledgePointId()
                + ",\"questionType\":\"SINGLE_CHOICE\",\"difficulty\":\"" + request.getDifficulty()
                + "\",\"stem\":\"...\",\"answer\":\"A\",\"analysis\":\"...\",\"options\":[{\"label\":\"A\",\"content\":\"...\"},{\"label\":\"B\",\"content\":\"...\"},{\"label\":\"C\",\"content\":\"...\"},{\"label\":\"D\",\"content\":\"...\"}]}";
    }

    private String sanitizeJsonContent(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```json", "").replaceFirst("^```", "").replaceFirst("```$", "").trim();
        }
        return trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Map<String, String> jsonObjectFormat() {
        Map<String, String> format = new HashMap<>();
        format.put("type", "json_object");
        return format;
    }
}
