package com.qdx.bishe.service.impl;

import com.qdx.bishe.common.BusinessException;
import com.qdx.bishe.config.LlmProperties;
import com.qdx.bishe.dto.AiQuestionDraftDto;
import com.qdx.bishe.dto.GenerateAiQuestionDraftRequest;
import com.qdx.bishe.dto.QuestionOptionDto;
import com.qdx.bishe.entity.Course;
import com.qdx.bishe.entity.KnowledgePoint;
import com.qdx.bishe.llm.LlmQuestionGenerator;
import com.qdx.bishe.mapper.CourseMapper;
import com.qdx.bishe.mapper.KnowledgePointMapper;
import com.qdx.bishe.service.AiQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AiQuestionServiceImpl implements AiQuestionService {

    private static final String SINGLE_CHOICE = "SINGLE_CHOICE";

    private final CourseMapper courseMapper;
    private final KnowledgePointMapper knowledgePointMapper;
    private final LlmProperties llmProperties;
    private final List<LlmQuestionGenerator> generators;

    @Override
    public AiQuestionDraftDto generateQuestionDraft(GenerateAiQuestionDraftRequest request) {
        if (!SINGLE_CHOICE.equals(request.getQuestionType())) {
            throw new BusinessException("当前版本仅支持 AI 生成单选题草稿");
        }

        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException("课程不存在或已停用");
        }

        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(request.getKnowledgePointId());
        if (knowledgePoint == null || !request.getCourseId().equals(knowledgePoint.getCourseId())) {
            throw new BusinessException("知识点不存在或不属于当前课程");
        }

        LlmQuestionGenerator generator = generators.stream()
                .filter(item -> item.getProviderName().equalsIgnoreCase(llmProperties.getProvider()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("未找到可用的大模型 provider：" + llmProperties.getProvider()));

        AiQuestionDraftDto draft = generator.generateQuestionDraft(request, course.getCourseName(), knowledgePoint.getPointName());
        normalizeDraft(draft, request);
        return draft;
    }

    private void normalizeDraft(AiQuestionDraftDto draft, GenerateAiQuestionDraftRequest request) {
        if (draft == null) {
            throw new BusinessException("AI 未返回题目草稿");
        }
        if (draft.getOptions() == null || draft.getOptions().size() < 2) {
            throw new BusinessException("AI 返回的选项数量不足");
        }

        draft.setCourseId(request.getCourseId());
        draft.setKnowledgePointId(request.getKnowledgePointId());
        draft.setQuestionType(SINGLE_CHOICE);
        draft.setDifficulty(request.getDifficulty());
        draft.setAnswer(normalizeLabel(draft.getAnswer()));
        draft.getOptions().forEach(option -> option.setLabel(normalizeLabel(option.getLabel())));

        boolean answerExists = draft.getOptions().stream()
                .map(QuestionOptionDto::getLabel)
                .anyMatch(label -> label.equals(draft.getAnswer()));
        if (!answerExists) {
            throw new BusinessException("AI 返回的答案不在选项中");
        }

        if (draft.getSource() == null || draft.getSource().trim().isEmpty()) {
            draft.setSource("AI_GENERATED");
        }
        if (draft.getProvider() == null || draft.getProvider().trim().isEmpty()) {
            draft.setProvider(llmProperties.getProvider());
        }
    }

    private String normalizeLabel(String label) {
        return label == null ? "" : label.trim().toUpperCase(Locale.ROOT);
    }
}
