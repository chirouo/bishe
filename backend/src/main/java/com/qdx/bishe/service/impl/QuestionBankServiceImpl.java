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
    private static final String SHORT_ANSWER = "SHORT_ANSWER";
    private static final String TRUE_FALSE = "TRUE_FALSE";

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
    public Long createQuestion(CreateQuestionRequest request, Long currentUserId) {
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException("课程不存在或已停用");
        }

        KnowledgePoint knowledgePoint = knowledgePointMapper.selectById(request.getKnowledgePointId());
        if (knowledgePoint == null || !request.getCourseId().equals(knowledgePoint.getCourseId())) {
            throw new BusinessException("知识点不存在或不属于当前课程");
        }

        String questionType = normalizeQuestionType(request.getQuestionType());
        String answer = normalizeAnswerByType(questionType, request.getAnswer());
        List<QuestionOption> optionEntities = buildQuestionOptions(questionType, request.getOptions(), answer);

        Question question = new Question();
        question.setCourseId(request.getCourseId());
        question.setKnowledgePointId(request.getKnowledgePointId());
        question.setQuestionType(questionType);
        question.setStem(request.getStem());
        question.setDifficulty(request.getDifficulty());
        question.setAnswer(answer);
        question.setAnalysis(request.getAnalysis());
        question.setSource(resolveSource(request.getSource()));
        question.setCreatedBy(currentUserId);
        questionMapper.insert(question);

        optionEntities.forEach(option -> {
            option.setQuestionId(question.getId());
            questionOptionMapper.insert(option);
        });
        return question.getId();
    }

    private String normalizeQuestionType(String questionType) {
        if (questionType == null) {
            throw new BusinessException("题型不能为空");
        }
        String normalized = questionType.trim().toUpperCase(Locale.ROOT);
        if (SINGLE_CHOICE.equals(normalized) || SHORT_ANSWER.equals(normalized) || TRUE_FALSE.equals(normalized)) {
            return normalized;
        }
        throw new BusinessException("题型不合法");
    }

    private String normalizeAnswerByType(String questionType, String answer) {
        if (SINGLE_CHOICE.equals(questionType)) {
            return normalizeLabel(answer);
        }
        if (TRUE_FALSE.equals(questionType)) {
            return normalizeTrueFalseAnswer(answer);
        }
        return answer == null ? "" : answer.trim();
    }

    private List<QuestionOption> buildQuestionOptions(String questionType,
                                                      List<CreateQuestionOptionRequest> options,
                                                      String answer) {
        if (SHORT_ANSWER.equals(questionType)) {
            return new ArrayList<>();
        }

        if (TRUE_FALSE.equals(questionType)) {
            QuestionOption trueOption = new QuestionOption();
            trueOption.setOptionLabel("TRUE");
            trueOption.setOptionContent("正确");

            QuestionOption falseOption = new QuestionOption();
            falseOption.setOptionLabel("FALSE");
            falseOption.setOptionContent("错误");

            List<QuestionOption> result = new ArrayList<>();
            result.add(trueOption);
            result.add(falseOption);
            return result;
        }

        List<CreateQuestionOptionRequest> safeOptions = options == null ? new ArrayList<>() : options;
        Set<String> labels = new LinkedHashSet<>();
        for (CreateQuestionOptionRequest option : safeOptions) {
            labels.add(normalizeLabel(option.getLabel()));
        }
        if (labels.size() != safeOptions.size()) {
            throw new BusinessException("选项标识不能重复");
        }
        if (labels.size() < 2) {
            throw new BusinessException("单选题至少需要两个有效选项");
        }

        if (!labels.contains(answer)) {
            throw new BusinessException("答案必须出现在选项标识中");
        }

        return safeOptions.stream()
                .map(option -> {
                    QuestionOption entity = new QuestionOption();
                    entity.setOptionLabel(normalizeLabel(option.getLabel()));
                    entity.setOptionContent(option.getContent());
                    return entity;
                })
                .collect(Collectors.toList());
    }

    private String normalizeLabel(String label) {
        return label == null ? "" : label.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeTrueFalseAnswer(String answer) {
        String normalized = answer == null ? "" : answer.trim().toUpperCase(Locale.ROOT);
        if ("TRUE".equals(normalized) || "正确".equals(normalized)) {
            return "TRUE";
        }
        if ("FALSE".equals(normalized) || "错误".equals(normalized)) {
            return "FALSE";
        }
        throw new BusinessException("判断题答案只能是正确或错误");
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
