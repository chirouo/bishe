package com.qdx.bishe.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreatePaperRequest {

    @NotNull(message = "课程不能为空")
    private Long courseId;

    @NotBlank(message = "试卷标题不能为空")
    private String title;

    private String description;

    @NotBlank(message = "试卷状态不能为空")
    private String status;

    @Valid
    @NotEmpty(message = "试卷至少需要选择一道题")
    private List<CreatePaperQuestionRequest> questions;
}

