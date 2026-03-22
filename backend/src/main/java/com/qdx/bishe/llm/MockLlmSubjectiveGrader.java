package com.qdx.bishe.llm;

import com.qdx.bishe.dto.AiSubjectiveGradingResultDto;
import com.qdx.bishe.entity.Question;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MockLlmSubjectiveGrader implements LlmSubjectiveGrader {

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public AiSubjectiveGradingResultDto gradeShortAnswer(Question question, String studentAnswer, BigDecimal fullScore) {
        String normalizedAnswer = normalize(studentAnswer);
        if (normalizedAnswer.isEmpty()) {
            return AiSubjectiveGradingResultDto.builder()
                    .provider("mock")
                    .source("AI_MOCK")
                    .score(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .feedback("Mock 智能评阅：未作答，未覆盖关键术语，建议至少写出定义、性质和对应条件。")
                    .build();
        }

        double rate = 0.4D;
        if (normalizedAnswer.contains("自反")) {
            rate += 0.2D;
        }
        if (normalizedAnswer.contains("对称")) {
            rate += 0.2D;
        }
        if (normalizedAnswer.contains("任意元素") || normalizedAnswer.contains("所有元素")) {
            rate += 0.1D;
        }
        if (normalizedAnswer.contains("有序对")
                || normalizedAnswer.contains("(a,b)")
                || normalizedAnswer.contains("（a,b）")
                || normalizedAnswer.contains("(b,a)")
                || normalizedAnswer.contains("（b,a）")) {
            rate += 0.1D;
        }
        if (rate > 1D) {
            rate = 1D;
        }

        BigDecimal score = fullScore.multiply(BigDecimal.valueOf(rate))
                .setScale(2, RoundingMode.HALF_UP);

        String feedback;
        if (rate >= 0.85D) {
            feedback = "Mock 智能评阅：答案覆盖了主要关键词，定义表达较完整，可以作为阶段测试的有效作答。";
        } else if (rate >= 0.6D) {
            feedback = "Mock 智能评阅：答案提到了部分关键点，但定义或条件表述还不够完整。";
        } else {
            feedback = "Mock 智能评阅：答案与题意相关，但缺少关键术语或核心条件，建议补充标准定义。";
        }

        return AiSubjectiveGradingResultDto.builder()
                .provider("mock")
                .source("AI_MOCK")
                .score(score)
                .feedback(feedback)
                .build();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
