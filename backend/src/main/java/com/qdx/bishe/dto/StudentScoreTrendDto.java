package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentScoreTrendDto {

    private String paperTitle;
    private BigDecimal score;
    private BigDecimal paperTotalScore;
    private LocalDateTime submittedAt;
}
