package com.qdx.bishe.llm;

import com.qdx.bishe.dto.AiSubjectiveGradingResultDto;
import com.qdx.bishe.entity.Question;

import java.math.BigDecimal;

public interface LlmSubjectiveGrader {

    String getProviderName();

    AiSubjectiveGradingResultDto gradeShortAnswer(Question question, String studentAnswer, BigDecimal fullScore);
}
