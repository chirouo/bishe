package com.qdx.bishe.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdx.bishe.common.BusinessException;
import com.qdx.bishe.config.LlmProperties;
import com.qdx.bishe.dto.AiSubjectiveGradingResultDto;
import com.qdx.bishe.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiCompatibleSubjectiveGrader implements LlmSubjectiveGrader {

    private final LlmProperties llmProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public String getProviderName() {
        return "openai-compatible";
    }

    @Override
    public AiSubjectiveGradingResultDto gradeShortAnswer(Question question, String studentAnswer, BigDecimal fullScore) {
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
                    message("system", "你是离散数学主观题评阅助手。必须返回合法 json，不要输出 markdown、latex、反斜杠转义或额外说明。"),
                    message("user", buildPrompt(question, studentAnswer, fullScore))
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
            return objectMapper.readValue(content, AiSubjectiveGradingResultDto.class);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("大模型评阅失败：" + ex.getMessage() + "。若持续出现结构化输出异常，建议切换为当前可选的其他模型，例如 qwen-math-plus。");
        }
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String buildPrompt(Question question, String studentAnswer, BigDecimal fullScore) {
        return "请评阅一道离散数学简答题。\n"
                + "题目：" + question.getStem() + "\n"
                + "参考答案：" + valueOrEmpty(question.getAnswer()) + "\n"
                + "题目解析：" + valueOrEmpty(question.getAnalysis()) + "\n"
                + "满分：" + fullScore + "\n"
                + "学生答案：" + valueOrEmpty(studentAnswer) + "\n"
                + "请只返回 json，评语使用普通中文，不要输出 markdown。"
                + "请严格返回如下 JSON："
                + "{\"provider\":\"openai-compatible\",\"source\":\"AI_GENERATED\",\"score\":0,\"feedback\":\"...\"}";
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

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private Map<String, String> jsonObjectFormat() {
        Map<String, String> format = new HashMap<>();
        format.put("type", "json_object");
        return format;
    }
}
