package com.qdx.bishe.service.impl;

import com.qdx.bishe.common.BusinessException;
import com.qdx.bishe.config.LlmProperties;
import com.qdx.bishe.dto.AiSubjectiveGradingResultDto;
import com.qdx.bishe.entity.Question;
import com.qdx.bishe.llm.LlmSubjectiveGrader;
import com.qdx.bishe.service.AiGradingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiGradingServiceImpl implements AiGradingService {

    private final LlmProperties llmProperties;
    private final List<LlmSubjectiveGrader> graders;

    @Override
    public AiSubjectiveGradingResultDto gradeShortAnswer(Question question, String studentAnswer, BigDecimal fullScore) {
        if (!"SHORT_ANSWER".equals(question.getQuestionType())) {
            throw new BusinessException("当前仅支持简答题智能评阅");
        }
        if (fullScore == null || fullScore.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("题目分值配置异常");
        }

        LlmSubjectiveGrader grader = graders.stream()
                .filter(item -> item.getProviderName().equalsIgnoreCase(llmProperties.getProvider()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到可用的大模型 provider：" + llmProperties.getProvider()));

        AiSubjectiveGradingResultDto result = grader.gradeShortAnswer(question, studentAnswer, fullScore);
        if (result == null) {
            throw new BusinessException("智能评阅未返回结果");
        }
        if (result.getScore() == null) {
            result.setScore(BigDecimal.ZERO);
        }
        if (result.getScore().compareTo(BigDecimal.ZERO) < 0) {
            result.setScore(BigDecimal.ZERO);
        }
        if (result.getScore().compareTo(fullScore) > 0) {
            result.setScore(fullScore);
        }
        result.setScore(result.getScore().setScale(2, RoundingMode.HALF_UP));
        if (result.getFeedback() == null || result.getFeedback().trim().isEmpty()) {
            result.setFeedback("智能评阅已完成，但未返回详细评语。");
        }
        if (result.getProvider() == null || result.getProvider().trim().isEmpty()) {
            result.setProvider(llmProperties.getProvider());
        }
        if (result.getSource() == null || result.getSource().trim().isEmpty()) {
            result.setSource("AI_GENERATED");
        }
        return result;
    }
}
