package com.qdx.bishe.controller;

import com.qdx.bishe.common.ApiResponse;
import com.qdx.bishe.dto.AiQuestionDraftDto;
import com.qdx.bishe.dto.GenerateAiQuestionDraftRequest;
import com.qdx.bishe.service.AiQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/ai/questions")
@RequiredArgsConstructor
public class TeacherAiQuestionController {

    private final AiQuestionService aiQuestionService;

    @PostMapping("/draft")
    public ApiResponse<AiQuestionDraftDto> generateQuestionDraft(@Validated @RequestBody GenerateAiQuestionDraftRequest request) {
        return ApiResponse.success(aiQuestionService.generateQuestionDraft(request));
    }
}
