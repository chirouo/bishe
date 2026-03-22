package com.qdx.bishe.controller;

import com.qdx.bishe.common.ApiResponse;
import com.qdx.bishe.dto.CreateQuestionRequest;
import com.qdx.bishe.dto.QuestionListItemDto;
import com.qdx.bishe.service.QuestionBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/questions")
@RequiredArgsConstructor
public class TeacherQuestionController {

    private final QuestionBankService questionBankService;

    @GetMapping
    public ApiResponse<List<QuestionListItemDto>> listQuestions(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long knowledgePointId,
            @RequestParam(required = false) String questionType) {
        return ApiResponse.success(questionBankService.listQuestions(courseId, knowledgePointId, questionType));
    }

    @PostMapping
    public ApiResponse<Long> createQuestion(
            @Validated @RequestBody CreateQuestionRequest request,
            @RequestAttribute("currentUserId") Long currentUserId) {
        return ApiResponse.success(questionBankService.createQuestion(request, currentUserId));
    }
}
