package com.qdx.bishe.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class SubmitStudentAnswerRequest {

    @NotNull(message = "题目不能为空")
    private Long questionId;

    private String answerContent;
}
