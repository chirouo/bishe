package com.qdx.bishe.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiQuestionDraftDto {

    private String provider;
    private String source;
    private Long courseId;
    private Long knowledgePointId;
    private String questionType;
    private String difficulty;
    private String stem;
    private String answer;
    private String analysis;
    private List<QuestionOptionDto> options;
}
