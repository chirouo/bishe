package com.qdx.bishe.controller;

import com.qdx.bishe.common.ApiResponse;
import com.qdx.bishe.dto.TeacherStatisticsAnalysisDto;
import com.qdx.bishe.service.TeacherStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/teacher/statistics")
@RequiredArgsConstructor
public class TeacherStatisticsController {

    private final TeacherStatisticsService teacherStatisticsService;

    @GetMapping("/analysis")
    public ApiResponse<TeacherStatisticsAnalysisDto> getTeacherStatisticsAnalysis() {
        return ApiResponse.success(teacherStatisticsService.getTeacherStatisticsAnalysis());
    }
}
