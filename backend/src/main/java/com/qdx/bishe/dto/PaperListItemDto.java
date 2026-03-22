package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaperListItemDto {

    private Long id;
    private Long courseId;
    private String courseName;
    private String title;
    private String description;
    private BigDecimal totalScore;
    private String status;
    private Long questionCount;
}

