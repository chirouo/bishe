package com.qdx.bishe.controller;

import com.qdx.bishe.common.ApiResponse;
import com.qdx.bishe.dto.AiModelSettingsDto;
import com.qdx.bishe.dto.UpdateAiModelRequest;
import com.qdx.bishe.service.AiSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/ai/settings")
@RequiredArgsConstructor
public class TeacherAiSettingsController {

    private final AiSettingsService aiSettingsService;

    @GetMapping
    public ApiResponse<AiModelSettingsDto> getSettings() {
        return ApiResponse.success(aiSettingsService.getSettings());
    }

    @PutMapping("/model")
    public ApiResponse<AiModelSettingsDto> switchModel(@Validated @RequestBody UpdateAiModelRequest request) {
        return ApiResponse.success(aiSettingsService.switchModel(request.getModel()));
    }
}
