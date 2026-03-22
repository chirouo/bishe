package com.qdx.bishe.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class TeacherStatisticsAnalysisDto {

    private Integer submittedExamCount;
    private Integer studentCount;
    private BigDecimal averageScoreRate;
    private Integer weakKnowledgePointCount;
    private String suggestion;
    private List<TeacherPaperTrendDto> papers;
    private List<TeacherKnowledgeMasteryDto> knowledgePoints;
}
