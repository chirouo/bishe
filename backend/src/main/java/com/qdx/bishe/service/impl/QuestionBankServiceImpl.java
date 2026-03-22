package com.qdx.bishe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qdx.bishe.common.BusinessException;
import com.qdx.bishe.dto.CreateQuestionOptionRequest;
import com.qdx.bishe.dto.CreateQuestionRequest;
import com.qdx.bishe.dto.QuestionListItemDto;
import com.qdx.bishe.dto.QuestionOptionDto;
import com.qdx.bishe.entity.Course;
import com.qdx.bishe.entity.KnowledgePoint;
import com.qdx.bishe.entity.Question;
import com.qdx.bishe.entity.QuestionOption;
import com.qdx.bishe.mapper.CourseMapper;
import com.qdx.bishe.mapper.KnowledgePointMapper;
import com.qdx.bishe.mapper.QuestionMapper;
import com.qdx.bishe.mapper.QuestionOptionMapper;
import com.qdx.bishe.service.QuestionBankService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionBankServiceImpl implements QuestionBankService {

    private static final String SINGLE_CHOICE = "SINGLE_CHOICE";

    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final CourseMapper courseMapper;
    private final KnowledgePointMapper knowledgePointMapper;

    @Override
    public List<QuestionListItemDto> listQuestions(Long courseId, Long knowledgePointId, String questionType) {
        List<QuestionListItemDto> questions = questionMapper.selectQuestionList(courseId, knowledgePointId, questionType);
        if (questions.isEmpty()) {
            return questions;
        }

        List<Long> questionIds = questions.stream()
                .map(QuestionListItemDto::getId)
                .collect(Collectors.toList());

        Map<Long, List<QuestionOptionDto>> optionMap = questionOptionMapper.selectList(new LambdaQueryWrapper<QuestionOption>()
                        .in(QuestionOption::getQuestionId, questionIds)
                        .orderByAsc(QuestionOption::getQuestionId, QuestionOption::getOptionLabel))
                .stream()
                .collect(Collectors.groupingBy(
                        QuestionOption::getQuestionId,
                        Collectors.mapping(option -> QuestionOptionDto.builder()
                                        .label(option.getOptionLabel())
                                        .content(option.getOptionContent())
                                        .build(),
                                Collectors.toList())));

        questions.forEach(question -> question.setOptions(optionMap.getOrDefault(question.getId(), new ArrayList<>())));
        return questions;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSingleChoiceQuestion(CreateQuestionRequest request, Long currentUserId) {
        if (!SINGLE_CHOICE.equals(request.getQuestionType())) {
            throw new BusinessException("当前版本仅支持新增单选题");
        }

        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException("课程不存在或已停用");
        }

        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(request.getKnowledgePointId());
        if (knowledgePoint == null || !request.getCourseId().equals(knowledgePoint.getCourseId())) {
            throw new BusinessException("知识点不存在或不属于当前课程");
        }

        Set<String> labels = new LinkedHashSet<>();
        for (CreateQuestionOptionRequest option : request.getOptions()) {
            labels.add(normalizeLabel(option.getLabel()));
        }
        if (labels.size() != request.getOptions().size()) {
            throw new BusinessException("选项标识不能重复");
        }
        if (labels.size() < 2) {
            throw new BusinessException("单选题至少需要两个有效选项");
        }

        String answer = normalizeLabel(request.getAnswer());
        if (!labels.contains(answer)) {
            throw new BusinessException("答案必须出现在选项标识中");
        }

        Question question = new Question();
        question.setCourseId(request.getCourseId());
        question.setKnowledgePointId(request.getKnowledgePointId());
        question.setQuestionType(request.getQuestionType());
        question.setStem(request.getStem());
        question.setDifficulty(request.getDifficulty());
        question.setAnswer(answer);
        question.setAnalysis(request.getAnalysis());
        question.setSource(resolveSource(request.getSource()));
        question.setCreatedBy(currentUserId);
        questionMapper.insert(question);

        List<QuestionOption> optionEntities = request.getOptions().stream()
                .map(option -> {
                    QuestionOption entity = new QuestionOption();
                    entity.setQuestionId(question.getId());
                    entity.setOptionLabel(normalizeLabel(option.getLabel()));
                    entity.setOptionContent(option.getContent());
                    return entity;
                })
                .collect(Collectors.toList());

        optionEntities.forEach(questionOptionMapper::insert);
        return question.getId();
    }

    private String normalizeLabel(String label) {
        return label == null ? "" : label.trim().toUpperCase(Locale.ROOT);
    }

    private String resolveSource(String source) {
        if (source == null || source.trim().isEmpty()) {
            return "MANUAL";
        }
        String normalized = source.trim().toUpperCase(Locale.ROOT);
        if ("MANUAL".equals(normalized) || "AI_MOCK".equals(normalized) || "AI_GENERATED".equals(normalized)) {
            return normalized;
        }
        throw new BusinessException("题目来源不合法");
    }
}
