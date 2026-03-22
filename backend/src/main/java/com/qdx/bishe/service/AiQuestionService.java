package com.qdx.bishe.service;

import com.qdx.bishe.dto.AiQuestionDraftDto;
import com.qdx.bishe.dto.GenerateAiQuestionDraftRequest;

public interface AiQuestionService {

    AiQuestionDraftDto generateQuestionDraft(GenerateAiQuestionDraftRequest request);
}
