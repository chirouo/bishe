package com.qdx.bishe.service.impl;

import com.qdx.bishe.common.BusinessException;
import com.qdx.bishe.config.LlmProperties;
import com.qdx.bishe.dto.AiPaperDraftDto;
import com.qdx.bishe.dto.GenerateAiPaperDraftRequest;
import com.qdx.bishe.dto.QuestionListItemDto;
import com.qdx.bishe.dto.TeacherKnowledgeMasteryDto;
import com.qdx.bishe.entity.Course;
import com.qdx.bishe.llm.LlmPaperDraftPlanner;
import com.qdx.bishe.mapper.CourseMapper;
import com.qdx.bishe.mapper.QuestionMapper;
import com.qdx.bishe.mapper.TeacherStatisticsMapper;
import com.qdx.bishe.service.AiPaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiPaperServiceImpl implements AiPaperService {

    private final CourseMapper courseMapper;
    private final QuestionMapper questionMapper;
    private final TeacherStatisticsMapper teacherStatisticsMapper;
    private final LlmProperties llmProperties;
    private final List<LlmPaperDraftPlanner> planners;

    @Override
    public AiPaperDraftDto generatePaperDraft(GenerateAiPaperDraftRequest request) {
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException("课程不存在或已停用");
        }

        List<QuestionListItemDto> candidates = questionMapper.selectQuestionList(request.getCourseId(), null, null);
        if (candidates.isEmpty()) {
            throw new BusinessException("当前课程题库为空，无法生成组卷草稿");
        }
        if (candidates.size() < request.getQuestionCount()) {
            throw new BusinessException("当前课程题目数量不足，无法生成指定题量的草稿");
        }

        LlmPaperDraftPlanner planner = planners.stream()
                .filter(item -> item.getProviderName().equalsIgnoreCase(llmProperties.getProvider()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到可用的大模型 provider：" + llmProperties.getProvider()));

        List<TeacherKnowledgeMasteryDto> knowledgeMastery = teacherStatisticsMapper.selectKnowledgeMasteryByCourse(request.getCourseId());
        return planner.generatePaperDraft(request, course.getCourseName(), candidates, knowledgeMastery);
    }
}
