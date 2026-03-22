package com.qdx.bishe.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qdx.bishe.common.BusinessException;
import com.qdx.bishe.dto.AiSubjectiveGradingResultDto;
import com.qdx.bishe.dto.QuestionOptionDto;
import com.qdx.bishe.dto.StudentExamDetailDto;
import com.qdx.bishe.dto.StudentExamListItemDto;
import com.qdx.bishe.dto.StudentExamQuestionDto;
import com.qdx.bishe.dto.StudentExamSubmitResultDto;
import com.qdx.bishe.dto.SubmitStudentAnswerRequest;
import com.qdx.bishe.dto.SubmitStudentExamRequest;
import com.qdx.bishe.entity.ExamRecord;
import com.qdx.bishe.entity.Paper;
import com.qdx.bishe.entity.PaperQuestion;
import com.qdx.bishe.entity.Question;
import com.qdx.bishe.entity.QuestionOption;
import com.qdx.bishe.entity.StudentAnswer;
import com.qdx.bishe.mapper.ExamRecordMapper;
import com.qdx.bishe.mapper.PaperMapper;
import com.qdx.bishe.mapper.PaperQuestionMapper;
import com.qdx.bishe.mapper.QuestionMapper;
import com.qdx.bishe.mapper.QuestionOptionMapper;
import com.qdx.bishe.mapper.StudentAnswerMapper;
import com.qdx.bishe.mapper.StudentExamMapper;
import com.qdx.bishe.service.AiGradingService;
import com.qdx.bishe.service.StudentExamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentExamServiceImpl implements StudentExamService {

    private static final String SINGLE_CHOICE = "SINGLE_CHOICE";
    private static final String SHORT_ANSWER = "SHORT_ANSWER";
    private static final String TRUE_FALSE = "TRUE_FALSE";

    private final StudentExamMapper studentExamMapper;
    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final StudentAnswerMapper studentAnswerMapper;
    private final AiGradingService aiGradingService;

    @Override
    public List<StudentExamListItemDto> listStudentExams(Long studentId) {
        return studentExamMapper.selectStudentExams(studentId);
    }

    @Override
    public StudentExamDetailDto getStudentExamDetail(Long paperId, Long studentId) {
        StudentExamDetailDto detail = studentExamMapper.selectStudentExamDetail(paperId, studentId);
        if (detail == null) {
            throw new BusinessException("试卷不存在或未发布");
        }

        List<StudentExamQuestionDto> questions = studentExamMapper.selectStudentExamQuestions(paperId, studentId);
        if (questions.isEmpty()) {
            throw new BusinessException("当前试卷暂无题目");
        }

        Map<Long, List<QuestionOptionDto>> optionMap = buildOptionMap(questions);
        for (StudentExamQuestionDto question : questions) {
            question.setOptions(optionMap.getOrDefault(question.getQuestionId(), Collections.emptyList()));
        }
        detail.setQuestions(questions);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentExamSubmitResultDto submitStudentExam(Long paperId, Long studentId, SubmitStudentExamRequest request) {
        Paper paper = paperMapper.selectById(paperId);
        if (paper == null || !"PUBLISHED".equals(paper.getStatus())) {
            throw new BusinessException("试卷不存在或未发布");
        }

        List<PaperQuestion> paperQuestions = paperQuestionMapper.selectList(
                new LambdaQueryWrapper<PaperQuestion>()
                        .eq(PaperQuestion::getPaperId, paperId)
                        .orderByAsc(PaperQuestion::getQuestionOrder)
        );
        if (paperQuestions.isEmpty()) {
            throw new BusinessException("当前试卷暂无题目");
        }

        Set<Long> questionIds = paperQuestions.stream()
                .map(PaperQuestion::getQuestionId)
                .collect(Collectors.toSet());

        Map<Long, SubmitStudentAnswerRequest> answerMap = new LinkedHashMap<>();
        List<SubmitStudentAnswerRequest> submittedAnswers = request.getAnswers() == null
                ? Collections.emptyList()
                : request.getAnswers();
        for (SubmitStudentAnswerRequest item : submittedAnswers) {
            if (!questionIds.contains(item.getQuestionId())) {
                throw new BusinessException("提交答案包含无效题目");
            }
            if (answerMap.putIfAbsent(item.getQuestionId(), item) != null) {
                throw new BusinessException("同一道题只能提交一次答案");
            }
        }

        List<Question> questions = questionMapper.selectBatchIds(questionIds);
        if (questions.size() != questionIds.size()) {
            throw new BusinessException("试卷题目数据异常");
        }

        ExamRecord examRecord = examRecordMapper.selectOne(
                new LambdaQueryWrapper<ExamRecord>()
                        .eq(ExamRecord::getPaperId, paperId)
                        .eq(ExamRecord::getStudentId, studentId)
                        .last("LIMIT 1")
        );
        if (examRecord != null && "SUBMITTED".equals(examRecord.getStatus())) {
            throw new BusinessException("试卷已提交，不能重复提交");
        }

        if (examRecord == null) {
            examRecord = new ExamRecord();
            examRecord.setPaperId(paperId);
            examRecord.setStudentId(studentId);
            examRecord.setStatus("PENDING");
            examRecord.setAutoScore(BigDecimal.ZERO);
            examRecord.setSubjectiveScore(BigDecimal.ZERO);
            examRecord.setTotalScore(BigDecimal.ZERO);
            examRecordMapper.insert(examRecord);
        } else {
            studentAnswerMapper.delete(
                    new LambdaQueryWrapper<StudentAnswer>()
                            .eq(StudentAnswer::getExamRecordId, examRecord.getId())
            );
        }

        Map<Long, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, item -> item));

        BigDecimal autoScore = BigDecimal.ZERO;
        BigDecimal subjectiveScore = BigDecimal.ZERO;
        for (PaperQuestion paperQuestion : paperQuestions) {
            Question question = questionMap.get(paperQuestion.getQuestionId());
            String answerContent = normalizeAnswer(answerMap.get(paperQuestion.getQuestionId()));

            StudentAnswer studentAnswer = new StudentAnswer();
            studentAnswer.setExamRecordId(examRecord.getId());
            studentAnswer.setQuestionId(paperQuestion.getQuestionId());
            studentAnswer.setAnswerContent(answerContent);

            if (isObjectiveQuestion(question.getQuestionType())) {
                boolean correct = normalizeChoice(answerContent).equals(normalizeChoice(question.getAnswer()));
                BigDecimal score = correct ? paperQuestion.getScore() : BigDecimal.ZERO;
                studentAnswer.setIsCorrect(correct ? 1 : 0);
                studentAnswer.setScore(score);
                studentAnswer.setFeedback(correct
                        ? "回答正确"
                        : "标准答案：" + formatObjectiveAnswer(question.getQuestionType(), question.getAnswer()));
                autoScore = autoScore.add(score);
            } else if (SHORT_ANSWER.equals(question.getQuestionType())) {
                AiSubjectiveGradingResultDto gradingResult = aiGradingService.gradeShortAnswer(
                        question,
                        answerContent,
                        paperQuestion.getScore()
                );
                studentAnswer.setIsCorrect(0);
                studentAnswer.setScore(gradingResult.getScore());
                studentAnswer.setFeedback(gradingResult.getFeedback());
                subjectiveScore = subjectiveScore.add(gradingResult.getScore());
            } else {
                studentAnswer.setIsCorrect(0);
                studentAnswer.setScore(BigDecimal.ZERO);
                studentAnswer.setFeedback("待智能评阅");
            }

            studentAnswerMapper.insert(studentAnswer);
        }

        LocalDateTime submittedAt = LocalDateTime.now();
        examRecord.setStatus("SUBMITTED");
        examRecord.setAutoScore(autoScore);
        examRecord.setSubjectiveScore(subjectiveScore);
        examRecord.setTotalScore(autoScore.add(subjectiveScore));
        examRecord.setSubmittedAt(submittedAt);
        examRecordMapper.updateById(examRecord);

        return StudentExamSubmitResultDto.builder()
                .examRecordId(examRecord.getId())
                .status(examRecord.getStatus())
                .autoScore(examRecord.getAutoScore())
                .subjectiveScore(examRecord.getSubjectiveScore())
                .totalScore(examRecord.getTotalScore())
                .submittedAt(submittedAt)
                .build();
    }

    private Map<Long, List<QuestionOptionDto>> buildOptionMap(List<StudentExamQuestionDto> questions) {
        List<Long> objectiveQuestionIds = questions.stream()
                .filter(question -> isObjectiveQuestion(question.getQuestionType()))
                .map(StudentExamQuestionDto::getQuestionId)
                .collect(Collectors.toList());
        if (objectiveQuestionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return questionOptionMapper.selectList(
                        new LambdaQueryWrapper<QuestionOption>()
                                .in(QuestionOption::getQuestionId, objectiveQuestionIds)
                                .orderByAsc(QuestionOption::getQuestionId, QuestionOption::getOptionLabel)
                ).stream()
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

    private String normalizeAnswer(SubmitStudentAnswerRequest request) {
        if (request == null || request.getAnswerContent() == null) {
            return "";
        }
        return request.getAnswerContent().trim();
    }

    private String normalizeChoice(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private boolean isObjectiveQuestion(String questionType) {
        return SINGLE_CHOICE.equals(questionType) || TRUE_FALSE.equals(questionType);
    }

    private String formatObjectiveAnswer(String questionType, String answer) {
        if (TRUE_FALSE.equals(questionType)) {
            return "TRUE".equals(normalizeChoice(answer)) ? "正确" : "错误";
        }
        return answer;
    }
}
