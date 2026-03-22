package com.qdx.bishe.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AiPaperDraftQuestionDto {

    private Long questionId;
    private Long knowledgePointId;
    private String knowledgePointName;
    private String questionType;
    private String difficulty;
    private String stem;
    private BigDecimal recommendedScore;
    private String recommendationReason;
}
