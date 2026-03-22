package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TeacherPaperTrendDto {

    private String paperTitle;
    private BigDecimal averageScoreRate;
    private Integer submittedCount;
}
