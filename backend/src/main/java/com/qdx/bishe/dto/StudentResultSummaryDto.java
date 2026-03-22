package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudentResultSummaryDto {

    private Integer completedExamCount;
    private BigDecimal averageScoreRate;
}
