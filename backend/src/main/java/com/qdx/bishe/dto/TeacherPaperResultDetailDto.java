package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TeacherPaperResultDetailDto {

    private Long paperId;
    private String title;
    private String courseName;
    private String status;
    private BigDecimal totalScore;
    private Integer submittedCount;
    private BigDecimal averageScoreRate;
    private List<TeacherPaperStudentResultDto> records;
}
