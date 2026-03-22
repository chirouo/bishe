package com.qdx.bishe.llm;

import com.qdx.bishe.dto.AiQuestionDraftDto;
import com.qdx.bishe.dto.GenerateAiQuestionDraftRequest;

public interface LlmQuestionGenerator {

    String getProviderName();

    AiQuestionDraftDto generateQuestionDraft(GenerateAiQuestionDraftRequest request,
                                             String courseName,
                                             String knowledgePointName);
}
