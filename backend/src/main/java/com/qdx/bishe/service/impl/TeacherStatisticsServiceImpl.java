package com.qdx.bishe.service.impl;

import com.qdx.bishe.dto.TeacherKnowledgeMasteryDto;
import com.qdx.bishe.dto.TeacherPaperTrendDto;
import com.qdx.bishe.dto.TeacherStatisticsAnalysisDto;
import com.qdx.bishe.dto.TeacherStatisticsSummaryDto;
import com.qdx.bishe.mapper.TeacherStatisticsMapper;
import com.qdx.bishe.service.TeacherStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherStatisticsServiceImpl implements TeacherStatisticsService {

    private static final BigDecimal WEAK_POINT_THRESHOLD = new BigDecimal("60");

    private final TeacherStatisticsMapper teacherStatisticsMapper;

    @Override
    public TeacherStatisticsAnalysisDto getTeacherStatisticsAnalysis() {
        TeacherStatisticsSummaryDto summary = teacherStatisticsMapper.selectSummary();
        List<TeacherPaperTrendDto> papers = teacherStatisticsMapper.selectPaperTrends();
        List<TeacherKnowledgeMasteryDto> knowledgePoints = teacherStatisticsMapper.selectKnowledgeMastery();

        if (summary == null) {
            summary = new TeacherStatisticsSummaryDto();
            summary.setSubmittedExamCount(0);
            summary.setStudentCount(0);
            summary.setAverageScoreRate(BigDecimal.ZERO);
        }

        BigDecimal averageScoreRate = summary.getAverageScoreRate() == null
                ? BigDecimal.ZERO
                : summary.getAverageScoreRate().setScale(2, RoundingMode.HALF_UP);

        if (papers != null) {
            for (TeacherPaperTrendDto item : papers) {
                if (item.getAverageScoreRate() != null) {
                    item.setAverageScoreRate(item.getAverageScoreRate().setScale(2, RoundingMode.HALF_UP));
                }
            }
        }

        int weakKnowledgePointCount = 0;
        if (knowledgePoints != null) {
            for (TeacherKnowledgeMasteryDto item : knowledgePoints) {
                if (item.getMasteryRate() != null) {
                    item.setMasteryRate(item.getMasteryRate().setScale(2, RoundingMode.HALF_UP));
                    if (item.getMasteryRate().compareTo(WEAK_POINT_THRESHOLD) < 0) {
                        weakKnowledgePointCount++;
                    }
                }
            }
        }

        return TeacherStatisticsAnalysisDto.builder()
                .submittedExamCount(summary.getSubmittedExamCount() == null ? 0 : summary.getSubmittedExamCount())
                .studentCount(summary.getStudentCount() == null ? 0 : summary.getStudentCount())
                .averageScoreRate(averageScoreRate)
                .weakKnowledgePointCount(weakKnowledgePointCount)
                .suggestion(buildSuggestion(knowledgePoints))
                .papers(papers == null ? Collections.emptyList() : papers)
                .knowledgePoints(knowledgePoints == null ? Collections.emptyList() : knowledgePoints)
                .build();
    }

    private String buildSuggestion(List<TeacherKnowledgeMasteryDto> knowledgePoints) {
        if (knowledgePoints == null || knowledgePoints.isEmpty()) {
            return "当前还没有已提交考试数据，先发布并完成一场测试后再查看班级统计。";
        }

        TeacherKnowledgeMasteryDto weakestPoint = knowledgePoints.get(0);
        if (weakestPoint.getMasteryRate() != null && weakestPoint.getMasteryRate().compareTo(WEAK_POINT_THRESHOLD) < 0) {
            return weakestPoint.getPointName() + "错误率偏高，建议在下一次测试中增加基础巩固题。";
        }

        return "当前班级整体掌握情况较稳定，可以逐步增加中等难度题比例。";
    }
}
