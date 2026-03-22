package com.qdx.bishe.controller;

import com.qdx.bishe.common.ApiResponse;
import com.qdx.bishe.dto.CourseDto;
import com.qdx.bishe.dto.KnowledgePointDto;
import com.qdx.bishe.dto.TeacherDashboardOverviewDto;
import com.qdx.bishe.service.TeachingResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherResourceController {

    private final TeachingResourceService teachingResourceService;

    @GetMapping("/dashboard/overview")
    public ApiResponse<TeacherDashboardOverviewDto> dashboardOverview() {
        return ApiResponse.success(teachingResourceService.getDashboardOverview());
    }

    @GetMapping("/courses")
    public ApiResponse<List<CourseDto>> listCourses() {
        return ApiResponse.success(teachingResourceService.listCourses());
    }

    @GetMapping("/knowledge-points")
    public ApiResponse<List<KnowledgePointDto>> listKnowledgePoints(
            @RequestParam(required = false) Long courseId) {
        return ApiResponse.success(teachingResourceService.listKnowledgePoints(courseId));
    }
}

