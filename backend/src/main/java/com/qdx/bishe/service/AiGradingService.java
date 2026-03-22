package com.qdx.bishe.service;

import com.qdx.bishe.dto.AiSubjectiveGradingResultDto;
import com.qdx.bishe.entity.Question;

import java.math.BigDecimal;

public interface AiGradingService {

    AiSubjectiveGradingResultDto gradeShortAnswer(Question question, String studentAnswer, BigDecimal fullScore);
}
