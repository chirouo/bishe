package com.qdx.bishe.llm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdx.bishe.common.BusinessException;
import com.qdx.bishe.config.LlmProperties;
import com.qdx.bishe.dto.AiPaperDraftDto;
import com.qdx.bishe.dto.GenerateAiPaperDraftRequest;
import com.qdx.bishe.dto.QuestionListItemDto;
import com.qdx.bishe.dto.TeacherKnowledgeMasteryDto;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OpenAiCompatiblePaperDraftPlanner implements LlmPaperDraftPlanner {

    private final LlmProperties llmProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplateBuilder restTemplateBuilder;

    @Override
    public String getProviderName() {
        return "openai-compatible";
    }

    @Override
    public AiPaperDraftDto generatePaperDraft(GenerateAiPaperDraftRequest request,
                                              String courseName,
                                              List<QuestionListItemDto> candidates,
                                              List<TeacherKnowledgeMasteryDto> knowledgeMastery) {
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
                    message("system", "你是离散数学组卷助手。必须返回合法 json，不要输出 markdown、latex、反斜杠转义或额外说明。"),
                    message("user", buildPrompt(request, courseName, candidates, knowledgeMastery))
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
            return objectMapper.readValue(content, AiPaperDraftDto.class);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException("AI 组卷调用失败：" + ex.getMessage() + "。若持续出现结构化输出异常，建议切换为当前可选的其他模型，例如 qwen-math-plus。");
        }
    }

    private Map<String, String> message(String role, String content) {
        Map<String, String> message = new HashMap<>();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String buildPrompt(GenerateAiPaperDraftRequest request,
                               String courseName,
                               List<QuestionListItemDto> candidates,
                               List<TeacherKnowledgeMasteryDto> knowledgeMastery) {
        String candidateSummary = candidates.stream()
                .map(item -> "questionId=" + item.getId()
                        + ", knowledgePointId=" + item.getKnowledgePointId()
                        + ", knowledgePointName=" + item.getKnowledgePointName()
                        + ", questionType=" + item.getQuestionType()
                        + ", difficulty=" + item.getDifficulty()
                        + ", stem=" + item.getStem())
                .collect(Collectors.joining("\n"));

        String masterySummary = knowledgeMastery == null || knowledgeMastery.isEmpty()
                ? "暂无班级作答数据"
                : knowledgeMastery.stream()
                .map(item -> "knowledgePointId=" + item.getKnowledgePointId()
                        + ", pointName=" + item.getPointName()
                        + ", masteryRate=" + item.getMasteryRate())
                .collect(Collectors.joining("\n"));

        String scorePerQuestion = request.getScorePerQuestion() == null ? "10" : request.getScorePerQuestion().toPlainString();

        return "请基于现有题库，为课程“" + courseName + "”生成一份离散数学阶段测试组卷草稿。\n"
                + "目标题量：" + request.getQuestionCount() + "\n"
                + "建议每题分值：" + scorePerQuestion + "\n"
                + "补充要求：" + valueOrEmpty(request.getRequirements()) + "\n"
                + "班级知识点掌握度如下，掌握度越低越需要优先覆盖：\n"
                + masterySummary + "\n"
                + "可用题库如下，只能从这些题中选择，不能编造新题：\n"
                + candidateSummary + "\n"
                + "请只返回 json，策略说明与推荐理由使用普通中文。"
                + "请严格返回如下 JSON："
                + "{\"provider\":\"openai-compatible\",\"source\":\"AI_GENERATED\",\"title\":\"...\",\"description\":\"...\",\"strategySummary\":\"...\",\"questions\":[{\"questionId\":0,\"knowledgePointId\":0,\"knowledgePointName\":\"...\",\"questionType\":\"SINGLE_CHOICE\",\"difficulty\":\"EASY\",\"stem\":\"...\",\"recommendedScore\":10,\"recommendationReason\":\"...\"}]}";
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
