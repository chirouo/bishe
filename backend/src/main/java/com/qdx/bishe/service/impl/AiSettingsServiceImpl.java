package com.qdx.bishe.service.impl;

import com.qdx.bishe.common.BusinessException;
import com.qdx.bishe.config.LlmProperties;
import com.qdx.bishe.dto.AiModelSettingsDto;
import com.qdx.bishe.service.AiSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiSettingsServiceImpl implements AiSettingsService {

    private final LlmProperties llmProperties;

    @Override
    public AiModelSettingsDto getSettings() {
        return buildSettings();
    }

    @Override
    public synchronized AiModelSettingsDto switchModel(String model) {
        String normalizedModel = normalize(model);
        List<String> availableModels = resolveAvailableModels();
        if (availableModels.isEmpty()) {
            throw new BusinessException("当前未配置可切换的模型列表");
        }
        if (!availableModels.contains(normalizedModel)) {
            throw new BusinessException("不支持的模型：" + normalizedModel);
        }
        llmProperties.setModel(normalizedModel);
        return buildSettings();
    }

    private AiModelSettingsDto buildSettings() {
        List<String> availableModels = resolveAvailableModels();
        if (availableModels.isEmpty() && hasText(llmProperties.getModel())) {
            availableModels.add(llmProperties.getModel().trim());
        }

        AiModelSettingsDto dto = new AiModelSettingsDto();
        dto.setProvider(llmProperties.getProvider());
        dto.setCurrentModel(normalize(llmProperties.getModel()));
        dto.setAvailableModels(availableModels);
        return dto;
    }

    private List<String> resolveAvailableModels() {
        List<String> availableModels = new ArrayList<>();
        for (String model : llmProperties.getAvailableModels()) {
            String normalizedModel = normalize(model);
            if (hasText(normalizedModel) && !availableModels.contains(normalizedModel)) {
                availableModels.add(normalizedModel);
            }
        }
        return availableModels;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
