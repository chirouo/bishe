package com.qdx.bishe.controller;

import com.qdx.bishe.common.ApiResponse;
import com.qdx.bishe.dto.StudentExamDetailDto;
import com.qdx.bishe.dto.StudentExamListItemDto;
import com.qdx.bishe.dto.StudentExamSubmitResultDto;
import com.qdx.bishe.dto.SubmitStudentExamRequest;
import com.qdx.bishe.service.StudentExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student/exams")
@RequiredArgsConstructor
public class StudentExamController {

    private final StudentExamService studentExamService;

    @GetMapping
    public ApiResponse<List<StudentExamListItemDto>> listStudentExams(
            @RequestAttribute("currentUserId") Long currentUserId) {
        return ApiResponse.success(studentExamService.listStudentExams(currentUserId));
    }

    @GetMapping("/{paperId}")
    public ApiResponse<StudentExamDetailDto> getStudentExamDetail(
            @PathVariable Long paperId,
            @RequestAttribute("currentUserId") Long currentUserId) {
        return ApiResponse.success(studentExamService.getStudentExamDetail(paperId, currentUserId));
    }

    @PostMapping("/{paperId}/submit")
    public ApiResponse<StudentExamSubmitResultDto> submitStudentExam(
            @PathVariable Long paperId,
            @Validated @RequestBody SubmitStudentExamRequest request,
            @RequestAttribute("currentUserId") Long currentUserId) {
        return ApiResponse.success(studentExamService.submitStudentExam(paperId, currentUserId, request));
    }
}
