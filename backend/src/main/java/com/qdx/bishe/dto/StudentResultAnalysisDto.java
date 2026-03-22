package com.qdx.bishe.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class StudentResultAnalysisDto {

    private Integer completedExamCount;
    private BigDecimal averageScoreRate;
    private Integer weakKnowledgePointCount;
    private String suggestion;
    private List<StudentScoreTrendDto> trends;
    private List<StudentKnowledgeMasteryDto> knowledgePoints;
}
