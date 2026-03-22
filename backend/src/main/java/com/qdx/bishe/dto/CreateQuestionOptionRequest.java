package com.qdx.bishe.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CreateQuestionOptionRequest {

    @NotBlank(message = "选项标识不能为空")
    private String label;

    @NotBlank(message = "选项内容不能为空")
    private String content;
}

