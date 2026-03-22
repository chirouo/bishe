package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeacherStatisticsSummaryDto {

    private Integer submittedExamCount;
    private Integer studentCount;
    private BigDecimal averageScoreRate;
}
