package com.qdx.bishe.llm;

import com.qdx.bishe.dto.AiPaperDraftDto;
import com.qdx.bishe.dto.AiPaperDraftQuestionDto;
import com.qdx.bishe.dto.GenerateAiPaperDraftRequest;
import com.qdx.bishe.dto.QuestionListItemDto;
import com.qdx.bishe.dto.TeacherKnowledgeMasteryDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MockLlmPaperDraftPlanner implements LlmPaperDraftPlanner {

    private static final BigDecimal WEAK_POINT_THRESHOLD = new BigDecimal("60");

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public AiPaperDraftDto generatePaperDraft(GenerateAiPaperDraftRequest request,
                                              String courseName,
                                              List<QuestionListItemDto> candidates,
                                              List<TeacherKnowledgeMasteryDto> knowledgeMastery) {
        Map<Long, BigDecimal> masteryMap = knowledgeMastery == null
                ? new LinkedHashMap<>()
                : knowledgeMastery.stream()
                .collect(Collectors.toMap(
                        TeacherKnowledgeMasteryDto::getKnowledgePointId,
                        item -> item.getMasteryRate() == null ? new BigDecimal("60") : item.getMasteryRate(),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));

        List<QuestionListItemDto> sortedCandidates = new ArrayList<>(candidates);
        sortedCandidates.sort(Comparator
                .comparing((QuestionListItemDto item) -> masteryMap.getOrDefault(item.getKnowledgePointId(), new BigDecimal("60")))
                .thenComparing(this::difficultyWeight)
                .thenComparing(QuestionListItemDto::getId));

        List<QuestionListItemDto> selectedQuestions = selectQuestions(sortedCandidates, request.getQuestionCount());
        BigDecimal recommendedScore = request.getScorePerQuestion() == null
                ? new BigDecimal("10.00")
                : request.getScorePerQuestion();

        List<AiPaperDraftQuestionDto> draftQuestions = selectedQuestions.stream()
                .map(item -> AiPaperDraftQuestionDto.builder()
                        .questionId(item.getId())
                        .knowledgePointId(item.getKnowledgePointId())
                        .knowledgePointName(item.getKnowledgePointName())
                        .questionType(item.getQuestionType())
                        .difficulty(item.getDifficulty())
                        .stem(item.getStem())
                        .recommendedScore(recommendedScore)
                        .recommendationReason(buildReason(item, masteryMap))
                        .build())
                .collect(Collectors.toList());

        String weakestPointName = knowledgeMastery == null || knowledgeMastery.isEmpty()
                ? null
                : knowledgeMastery.get(0).getPointName();
        String strategySummary = weakestPointName == null
                ? "当前缺少班级作答数据，本次先按课程核心知识点和题目难度均衡抽题。"
                : "优先覆盖班级相对薄弱的“" + weakestPointName + "”，再补充其他核心知识点，形成一份可直接调整的组卷草稿。";

        String requirementSuffix = request.getRequirements() == null || request.getRequirements().trim().isEmpty()
                ? "可在保存前继续微调题目与分值。"
                : "附加要求：" + request.getRequirements().trim();

        return AiPaperDraftDto.builder()
                .provider("mock")
                .source("AI_MOCK")
                .title("AI 组卷草稿 - " + courseName + "（" + request.getQuestionCount() + "题）")
                .description("基于题库与知识点掌握情况生成的试卷草稿。" + requirementSuffix)
                .strategySummary(strategySummary)
                .questions(draftQuestions)
                .build();
    }

    private List<QuestionListItemDto> selectQuestions(List<QuestionListItemDto> candidates, Integer targetCount) {
        Map<Long, List<QuestionListItemDto>> grouped = candidates.stream()
                .collect(Collectors.groupingBy(
                        QuestionListItemDto::getKnowledgePointId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<QuestionListItemDto> selected = new ArrayList<>();
        Set<Long> selectedIds = new LinkedHashSet<>();

        for (List<QuestionListItemDto> items : grouped.values()) {
            if (selected.size() >= targetCount || items.isEmpty()) {
                break;
            }
            QuestionListItemDto first = items.get(0);
            selected.add(first);
            selectedIds.add(first.getId());
        }

        for (QuestionListItemDto item : candidates) {
            if (selected.size() >= targetCount) {
                break;
            }
            if (selectedIds.add(item.getId())) {
                selected.add(item);
            }
        }

        return selected;
    }

    private int difficultyWeight(QuestionListItemDto item) {
        if ("HARD".equalsIgnoreCase(item.getDifficulty())) {
            return 3;
        }
        if ("MEDIUM".equalsIgnoreCase(item.getDifficulty())) {
            return 2;
        }
        return 1;
    }

    private String buildReason(QuestionListItemDto question, Map<Long, BigDecimal> masteryMap) {
        BigDecimal masteryRate = masteryMap.get(question.getKnowledgePointId());
        if (masteryRate != null && masteryRate.compareTo(WEAK_POINT_THRESHOLD) < 0) {
            return "优先覆盖班级薄弱知识点“" + question.getKnowledgePointName() + "”。";
        }
        return "补充课程核心知识点，保证组卷覆盖面。";
    }
}
