package com.qdx.bishe.service;

import com.qdx.bishe.dto.AiModelSettingsDto;

public interface AiSettingsService {

    AiModelSettingsDto getSettings();

    AiModelSettingsDto switchModel(String model);
}
