package com.qdx.bishe.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UpdateAiModelRequest {

    @NotBlank(message = "模型名称不能为空")
    private String model;
}
