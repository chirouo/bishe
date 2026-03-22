package com.qdx.bishe.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class QuestionListItemDto {

    private Long id;
    private Long courseId;
    private String courseName;
    private Long knowledgePointId;
    private String knowledgePointName;
    private String questionType;
    private String stem;
    private String difficulty;
    private String answer;
    private String analysis;
    private String source;
    private List<QuestionOptionDto> options = new ArrayList<>();
}

