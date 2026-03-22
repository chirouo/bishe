package com.qdx.bishe.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class GenerateAiQuestionDraftRequest {

    @NotNull(message = "课程不能为空")
    private Long courseId;

    @NotNull(message = "知识点不能为空")
    private Long knowledgePointId;

    @NotBlank(message = "题型不能为空")
    private String questionType;

    @NotBlank(message = "难度不能为空")
    private String difficulty;

    private String requirements;
}
