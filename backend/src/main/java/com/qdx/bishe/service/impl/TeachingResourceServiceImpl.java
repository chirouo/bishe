package com.qdx.bishe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qdx.bishe.dto.CourseDto;
import com.qdx.bishe.dto.KnowledgePointDto;
import com.qdx.bishe.dto.TeacherDashboardOverviewDto;
import com.qdx.bishe.entity.Course;
import com.qdx.bishe.mapper.CourseMapper;
import com.qdx.bishe.mapper.KnowledgePointMapper;
import com.qdx.bishe.mapper.TeacherDashboardMapper;
import com.qdx.bishe.service.TeachingResourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeachingResourceServiceImpl implements TeachingResourceService {

    private final CourseMapper courseMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final TeacherDashboardMapper teacherDashboardMapper;

    @Override
    public TeacherDashboardOverviewDto getDashboardOverview() {
        return teacherDashboardMapper.selectOverview();
    }

    @Override
    public List<CourseDto> listCourses() {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                        .eq(Course::getStatus, 1)
                        .orderByAsc(Course::getId))
                .stream()
                .map(course -> CourseDto.builder()
                        .id(course.getId())
                        .courseName(course.getCourseName())
                        .description(course.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<KnowledgePointDto> listKnowledgePoints(Long courseId) {
        return knowledgePointMapper.selectKnowledgePointDtos(courseId);
    }
}

