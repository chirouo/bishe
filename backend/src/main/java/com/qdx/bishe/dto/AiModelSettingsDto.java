package com.qdx.bishe.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiModelSettingsDto {

    private String provider;
    private String currentModel;
    private List<String> availableModels;
}
