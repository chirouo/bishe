package com.qdx.bishe.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateQuestionRequest {

    @NotNull(message = "课程不能为空")
    private Long courseId;

    @NotNull(message = "知识点不能为空")
    private Long knowledgePointId;

    @NotBlank(message = "题型不能为空")
    private String questionType;

    @NotBlank(message = "题干不能为空")
    private String stem;

    @NotBlank(message = "难度不能为空")
    private String difficulty;

    @NotBlank(message = "答案不能为空")
    private String answer;

    private String analysis;

    private String source;

    @Valid
    @NotEmpty(message = "单选题至少需要两个选项")
    private List<CreateQuestionOptionRequest> options;
}
