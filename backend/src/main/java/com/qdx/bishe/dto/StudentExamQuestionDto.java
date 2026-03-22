package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StudentExamQuestionDto {

    private Long questionId;
    private Integer questionOrder;
    private String questionType;
    private String stem;
    private BigDecimal score;
    private String answerContent;
    private BigDecimal gainedScore;
    private String feedback;
    private List<QuestionOptionDto> options;
}
