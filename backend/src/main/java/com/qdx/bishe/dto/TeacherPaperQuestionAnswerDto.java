package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TeacherPaperQuestionAnswerDto {

    private Long questionId;
    private Integer questionOrder;
    private String questionType;
    private String stem;
    private BigDecimal fullScore;
    private String correctAnswer;
    private String studentAnswer;
    private Integer isCorrect;
    private BigDecimal gainedScore;
    private String feedback;
    private List<QuestionOptionDto> options;
}
