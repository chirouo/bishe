package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentExamListItemDto {

    private Long paperId;
    private String title;
    private String courseName;
    private BigDecimal paperTotalScore;
    private String examStatus;
    private BigDecimal studentScore;
    private LocalDateTime submittedAt;
}

