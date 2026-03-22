package com.qdx.bishe.llm;

import com.qdx.bishe.dto.AiQuestionDraftDto;
import com.qdx.bishe.dto.GenerateAiQuestionDraftRequest;
import com.qdx.bishe.dto.QuestionOptionDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;

@Component
public class MockLlmQuestionGenerator implements LlmQuestionGenerator {

    @Override
    public String getProviderName() {
        return "mock";
    }

    @Override
    public AiQuestionDraftDto generateQuestionDraft(GenerateAiQuestionDraftRequest request,
                                                    String courseName,
                                                    String knowledgePointName) {
        String difficultyText = toDifficultyText(request.getDifficulty());
        String requirementSuffix = request.getRequirements() == null || request.getRequirements().trim().isEmpty()
                ? "请覆盖基础概念辨析。"
                : "补充要求：" + request.getRequirements().trim();

        AiQuestionDraftDto draft = new AiQuestionDraftDto();
        draft.setProvider("mock");
        draft.setSource("AI_MOCK");
        draft.setCourseId(request.getCourseId());
        draft.setKnowledgePointId(request.getKnowledgePointId());
        draft.setQuestionType(request.getQuestionType());
        draft.setDifficulty(request.getDifficulty());
        draft.setStem("【Mock 题目】在" + courseName + "中，关于“" + knowledgePointName + "”的下列说法，哪一项最准确？（" + difficultyText + "）");
        draft.setAnswer("B");
        draft.setAnalysis("本题为 mock 生成草稿，用于演示大模型出题流程。当前应优先考查“" + knowledgePointName + "”的核心定义与常见混淆点。" + requirementSuffix);
        draft.setOptions(Arrays.asList(
                QuestionOptionDto.builder().label("A").content("与" + knowledgePointName + "无关的表述").build(),
                QuestionOptionDto.builder().label("B").content(knowledgePointName + "的标准定义或正确结论").build(),
                QuestionOptionDto.builder().label("C").content("容易与" + knowledgePointName + "混淆的错误说法").build(),
                QuestionOptionDto.builder().label("D").content("只在特殊条件下成立的片面结论").build()
        ));
        return draft;
    }

    private String toDifficultyText(String difficulty) {
        if ("HARD".equalsIgnoreCase(difficulty)) {
            return "困难";
        }
        if ("MEDIUM".equalsIgnoreCase(difficulty)) {
            return "中等";
        }
        return "简单";
    }
}
