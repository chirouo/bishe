package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudentExamDetailDto {

    private Long paperId;
    private String title;
    private String courseName;
    private BigDecimal paperTotalScore;
    private String examStatus;
    private BigDecimal studentScore;
    private BigDecimal autoScore;
    private BigDecimal subjectiveScore;
    private LocalDateTime submittedAt;
    private List<StudentExamQuestionDto> questions;
}
