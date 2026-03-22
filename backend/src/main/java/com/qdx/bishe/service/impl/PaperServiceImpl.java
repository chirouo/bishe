package com.qdx.bishe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qdx.bishe.common.BusinessException;
import com.qdx.bishe.dto.CreatePaperQuestionRequest;
import com.qdx.bishe.dto.CreatePaperRequest;
import com.qdx.bishe.dto.PaperListItemDto;
import com.qdx.bishe.dto.QuestionOptionDto;
import com.qdx.bishe.dto.TeacherPaperQuestionAnswerDto;
import com.qdx.bishe.dto.TeacherPaperResultDetailDto;
import com.qdx.bishe.dto.TeacherPaperStudentAnswerDetailDto;
import com.qdx.bishe.dto.TeacherPaperStudentResultDto;
import com.qdx.bishe.entity.Course;
import com.qdx.bishe.entity.Paper;
import com.qdx.bishe.entity.PaperQuestion;
import com.qdx.bishe.entity.Question;
import com.qdx.bishe.entity.QuestionOption;
import com.qdx.bishe.mapper.CourseMapper;
import com.qdx.bishe.mapper.PaperMapper;
import com.qdx.bishe.mapper.PaperQuestionMapper;
import com.qdx.bishe.mapper.QuestionMapper;
import com.qdx.bishe.mapper.QuestionOptionMapper;
import com.qdx.bishe.service.PaperService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final CourseMapper courseMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;

    @Override
    public List<PaperListItemDto> listPapers(Long courseId) {
        return paperMapper.selectPaperList(courseId);
    }

    @Override
    public TeacherPaperResultDetailDto getPaperResultDetail(Long paperId) {
        TeacherPaperResultDetailDto detail = paperMapper.selectPaperResultDetail(paperId);
        if (detail == null) {
            throw new BusinessException("试卷不存在");
        }

        List<TeacherPaperStudentResultDto> records = paperMapper.selectPaperStudentResults(paperId);
        if (detail.getAverageScoreRate() != null) {
            detail.setAverageScoreRate(detail.getAverageScoreRate().setScale(2, RoundingMode.HALF_UP));
        }
        for (TeacherPaperStudentResultDto item : records) {
            if (item.getScoreRate() != null) {
                item.setScoreRate(item.getScoreRate().setScale(2, RoundingMode.HALF_UP));
            }
        }
        detail.setRecords(records == null ? Collections.emptyList() : records);
        return detail;
    }

    @Override
    public TeacherPaperStudentAnswerDetailDto getPaperStudentAnswerDetail(Long paperId, Long studentId) {
        TeacherPaperStudentAnswerDetailDto detail = paperMapper.selectPaperStudentAnswerDetail(paperId, studentId);
        if (detail == null) {
            throw new BusinessException("未找到该学生的已提交答卷");
        }

        List<TeacherPaperQuestionAnswerDto> questions = paperMapper.selectPaperStudentAnswerQuestions(paperId, studentId);
        Map<Long, List<QuestionOptionDto>> optionMap = buildQuestionOptionMap(questions);
        for (TeacherPaperQuestionAnswerDto item : questions) {
            if (item.getFullScore() != null) {
                item.setFullScore(item.getFullScore().setScale(2, RoundingMode.HALF_UP));
            }
            if (item.getGainedScore() != null) {
                item.setGainedScore(item.getGainedScore().setScale(2, RoundingMode.HALF_UP));
            }
            item.setOptions(optionMap.getOrDefault(item.getQuestionId(), Collections.emptyList()));
        }
        detail.setQuestions(questions == null ? Collections.emptyList() : questions);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPaper(CreatePaperRequest request, Long currentUserId) {
        Course course = courseMapper.selectById(request.getCourseId());
        if (course == null || course.getStatus() == null || course.getStatus() != 1) {
            throw new BusinessException("课程不存在或已停用");
        }

        if (!"DRAFT".equals(request.getStatus()) && !"PUBLISHED".equals(request.getStatus())) {
            throw new BusinessException("试卷状态不合法");
        }

        Set<Long> questionIds = new LinkedHashSet<>();
        for (CreatePaperQuestionRequest item : request.getQuestions()) {
            questionIds.add(item.getQuestionId());
        }
        if (questionIds.size() != request.getQuestions().size()) {
            throw new BusinessException("试卷题目不能重复");
        }

        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        if (questions.size() != questionIds.size()) {
            throw new BusinessException("存在无效题目，无法创建试卷");
        }

        boolean invalidCourseQuestion = questions.stream()
                .anyMatch(question -> !request.getCourseId().equals(question.getCourseId()));
        if (invalidCourseQuestion) {
            throw new BusinessException("所选题目必须全部属于当前课程");
        }

        BigDecimal totalScore = request.getQuestions().stream()
                .map(CreatePaperQuestionRequest::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Paper paper = new Paper();
        paper.setCourseId(request.getCourseId());
        paper.setTitle(request.getTitle());
        paper.setDescription(request.getDescription());
        paper.setStatus(request.getStatus());
        paper.setTotalScore(totalScore);
        paper.setCreatedBy(currentUserId);
        paperMapper.insert(paper);

        Map<Long, CreatePaperQuestionRequest> questionMap = request.getQuestions().stream()
                .collect(Collectors.toMap(CreatePaperQuestionRequest::getQuestionId, item -> item));

        int order = 1;
        for (Long questionId : questionIds) {
            PaperQuestion paperQuestion = new PaperQuestion();
            paperQuestion.setPaperId(paper.getId());
            paperQuestion.setQuestionId(questionId);
            paperQuestion.setQuestionOrder(order++);
            paperQuestion.setScore(questionMap.get(questionId).getScore());
            paperQuestionMapper.insert(paperQuestion);
        }

        return paper.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishPaper(Long paperId, Long currentUserId) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null) {
            throw new BusinessException("试卷不存在");
        }

        if (paper.getCreatedBy() != null && currentUserId != null && !currentUserId.equals(paper.getCreatedBy())) {
            throw new BusinessException("只能发布自己创建的试卷");
        }

        Long questionCount = paperQuestionMapper.selectCount(new LambdaQueryWrapper<PaperQuestion>()
                .eq(PaperQuestion::getPaperId, paperId));
        if (questionCount == null || questionCount == 0) {
            throw new BusinessException("试卷暂无题目，不能发布");
        }

        if ("PUBLISHED".equals(paper.getStatus())) {
            return;
        }

        paper.setStatus("PUBLISHED");
        paperMapper.updateById(paper);
    }

    private Map<Long, List<QuestionOptionDto>> buildQuestionOptionMap(List<TeacherPaperQuestionAnswerDto> questions) {
        if (questions == null || questions.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> questionIds = questions.stream()
                .filter(item -> "SINGLE_CHOICE".equals(item.getQuestionType()) || "TRUE_FALSE".equals(item.getQuestionType()))
                .map(TeacherPaperQuestionAnswerDto::getQuestionId)
                .collect(Collectors.toList());
        if (questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return questionOptionMapper.selectList(new LambdaQueryWrapper<QuestionOption>()
                        .in(QuestionOption::getQuestionId, questionIds)
                        .orderByAsc(QuestionOption::getQuestionId, QuestionOption::getOptionLabel))
                .stream()
                .collect(Collectors.groupingBy(
                        QuestionOption::getQuestionId,
                        LinkedHashMap::new,
                        Collectors.mapping(option -> QuestionOptionDto.builder()
                                        .label(option.getOptionLabel())
                                        .content(option.getOptionContent())
                                        .build(),
                                Collectors.toList())
                ));
    }
}
