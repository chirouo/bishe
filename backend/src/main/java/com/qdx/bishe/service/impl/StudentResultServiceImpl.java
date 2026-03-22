package com.qdx.bishe.service.impl;

import com.qdx.bishe.dto.StudentKnowledgeMasteryDto;
import com.qdx.bishe.dto.StudentResultAnalysisDto;
import com.qdx.bishe.dto.StudentResultSummaryDto;
import com.qdx.bishe.dto.StudentScoreTrendDto;
import com.qdx.bishe.mapper.StudentResultMapper;
import com.qdx.bishe.service.StudentResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentResultServiceImpl implements StudentResultService {

    private static final BigDecimal WEAK_POINT_THRESHOLD = new BigDecimal("60");

    private final StudentResultMapper studentResultMapper;

    @Override
    public StudentResultAnalysisDto getStudentResultAnalysis(Long studentId) {
        StudentResultSummaryDto summary = studentResultMapper.selectStudentResultSummary(studentId);
        List<StudentScoreTrendDto> trends = studentResultMapper.selectStudentScoreTrends(studentId);
        List<StudentKnowledgeMasteryDto> knowledgePoints = studentResultMapper.selectStudentKnowledgeMastery(studentId);

        if (summary == null) {
            summary = new StudentResultSummaryDto();
            summary.setCompletedExamCount(0);
            summary.setAverageScoreRate(BigDecimal.ZERO);
        }

        BigDecimal averageScoreRate = summary.getAverageScoreRate() == null
                ? BigDecimal.ZERO
                : summary.getAverageScoreRate().setScale(2, RoundingMode.HALF_UP);
        int weakKnowledgePointCount = (int) knowledgePoints.stream()
                .filter(item -> item.getMasteryRate() != null && item.getMasteryRate().compareTo(WEAK_POINT_THRESHOLD) < 0)
                .count();

        for (StudentKnowledgeMasteryDto item : knowledgePoints) {
            if (item.getMasteryRate() != null) {
                item.setMasteryRate(item.getMasteryRate().setScale(2, RoundingMode.HALF_UP));
            }
        }

        return StudentResultAnalysisDto.builder()
                .completedExamCount(summary.getCompletedExamCount() == null ? 0 : summary.getCompletedExamCount())
                .averageScoreRate(averageScoreRate)
                .weakKnowledgePointCount(weakKnowledgePointCount)
                .suggestion(buildSuggestion(knowledgePoints))
                .trends(trends == null ? Collections.emptyList() : trends)
                .knowledgePoints(knowledgePoints == null ? Collections.emptyList() : knowledgePoints)
                .build();
    }

    private String buildSuggestion(List<StudentKnowledgeMasteryDto> knowledgePoints) {
        if (knowledgePoints == null || knowledgePoints.isEmpty()) {
            return "当前还没有已完成考试，先完成一份测试后再查看学习分析。";
        }

        StudentKnowledgeMasteryDto weakestPoint = knowledgePoints.get(0);
        if (weakestPoint.getMasteryRate() != null && weakestPoint.getMasteryRate().compareTo(WEAK_POINT_THRESHOLD) < 0) {
            return weakestPoint.getPointName() + "是当前最薄弱的知识点，建议先回顾定义、典型例题和错题。";
        }

        return "当前整体掌握情况较稳定，建议继续通过阶段性测试巩固知识点。";
    }
}
