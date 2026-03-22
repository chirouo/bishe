package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TeacherPaperStudentResultDto {

    private Long studentId;
    private String studentName;
    private BigDecimal totalScore;
    private BigDecimal autoScore;
    private BigDecimal subjectiveScore;
    private BigDecimal scoreRate;
    private String status;
    private LocalDateTime submittedAt;
}
