package com.qdx.bishe.service;

import com.qdx.bishe.dto.CourseDto;
import com.qdx.bishe.dto.KnowledgePointDto;
import com.qdx.bishe.dto.TeacherDashboardOverviewDto;

import java.util.List;

public interface TeachingResourceService {

    TeacherDashboardOverviewDto getDashboardOverview();

    List<CourseDto> listCourses();

    List<KnowledgePointDto> listKnowledgePoints(Long courseId);
}

