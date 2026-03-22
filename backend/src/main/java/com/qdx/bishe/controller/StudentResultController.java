package com.qdx.bishe.controller;

import com.qdx.bishe.common.ApiResponse;
import com.qdx.bishe.dto.StudentResultAnalysisDto;
import com.qdx.bishe.service.StudentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/results")
@RequiredArgsConstructor
public class StudentResultController {

    private final StudentResultService studentResultService;

    @GetMapping("/analysis")
    public ApiResponse<StudentResultAnalysisDto> getStudentResultAnalysis(
            @RequestAttribute("currentUserId") Long currentUserId) {
        return ApiResponse.success(studentResultService.getStudentResultAnalysis(currentUserId));
    }
}
