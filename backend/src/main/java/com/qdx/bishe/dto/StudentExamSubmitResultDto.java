package com.qdx.bishe.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class StudentExamSubmitResultDto {

    private Long examRecordId;
    private String status;
    private BigDecimal autoScore;
    private BigDecimal subjectiveScore;
    private BigDecimal totalScore;
    private LocalDateTime submittedAt;
}
