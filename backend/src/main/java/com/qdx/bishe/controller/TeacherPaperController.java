package com.qdx.bishe.controller;

import com.qdx.bishe.common.ApiResponse;
import com.qdx.bishe.dto.AiPaperDraftDto;
import com.qdx.bishe.dto.CreatePaperRequest;
import com.qdx.bishe.dto.GenerateAiPaperDraftRequest;
import com.qdx.bishe.dto.PaperListItemDto;
import com.qdx.bishe.dto.TeacherPaperResultDetailDto;
import com.qdx.bishe.dto.TeacherPaperStudentAnswerDetailDto;
import com.qdx.bishe.service.AiPaperService;
import com.qdx.bishe.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher/papers")
@RequiredArgsConstructor
public class TeacherPaperController {

    private final PaperService paperService;
    private final AiPaperService aiPaperService;

    @GetMapping
    public ApiResponse<List<PaperListItemDto>> listPapers(@RequestParam(required = false) Long courseId) {
        return ApiResponse.success(paperService.listPapers(courseId));
    }

    @GetMapping("/{paperId}/results")
    public ApiResponse<TeacherPaperResultDetailDto> getPaperResultDetail(@PathVariable Long paperId) {
        return ApiResponse.success(paperService.getPaperResultDetail(paperId));
    }

    @GetMapping("/{paperId}/results/{studentId}")
    public ApiResponse<TeacherPaperStudentAnswerDetailDto> getPaperStudentAnswerDetail(@PathVariable Long paperId,
                                                                                        @PathVariable Long studentId) {
        return ApiResponse.success(paperService.getPaperStudentAnswerDetail(paperId, studentId));
    }

    @PostMapping("/ai-draft")
    public ApiResponse<AiPaperDraftDto> generateAiPaperDraft(@Validated @RequestBody GenerateAiPaperDraftRequest request) {
        return ApiResponse.success(aiPaperService.generatePaperDraft(request));
    }

    @PostMapping
    public ApiResponse<Long> createPaper(@Validated @RequestBody CreatePaperRequest request,
                                         @RequestAttribute("currentUserId") Long currentUserId) {
        return ApiResponse.success(paperService.createPaper(request, currentUserId));
    }

    @PostMapping("/{paperId}/publish")
    public ApiResponse<Void> publishPaper(@PathVariable Long paperId,
                                          @RequestAttribute("currentUserId") Long currentUserId) {
        paperService.publishPaper(paperId, currentUserId);
        return ApiResponse.success();
    }
}
