package com.qdx.bishe.llm;

import com.qdx.bishe.dto.AiPaperDraftDto;
import com.qdx.bishe.dto.GenerateAiPaperDraftRequest;
import com.qdx.bishe.dto.QuestionListItemDto;
import com.qdx.bishe.dto.TeacherKnowledgeMasteryDto;

import java.util.List;

public interface LlmPaperDraftPlanner {

    String getProviderName();

    AiPaperDraftDto generatePaperDraft(GenerateAiPaperDraftRequest request,
                                       String courseName,
                                       List<QuestionListItemDto> candidates,
                                       List<TeacherKnowledgeMasteryDto> knowledgeMastery);
}
