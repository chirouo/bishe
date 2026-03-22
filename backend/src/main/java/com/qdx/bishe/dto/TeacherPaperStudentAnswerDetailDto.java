package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TeacherPaperStudentAnswerDetailDto {

    private Long paperId;
    private String title;
    private String courseName;
    private BigDecimal totalScore;
    private Long studentId;
    private String studentName;
    private String status;
    private LocalDateTime submittedAt;
    private BigDecimal studentTotalScore;
    private BigDecimal autoScore;
    private BigDecimal subjectiveScore;
    private List<TeacherPaperQuestionAnswerDto> questions;
}
