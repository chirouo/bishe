package com.qdx.bishe.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AiSubjectiveGradingResultDto {

    private String provider;
    private String source;
    private BigDecimal score;
    private String feedback;
}
