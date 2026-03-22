package com.qdx.bishe.service;

import com.qdx.bishe.dto.AiPaperDraftDto;
import com.qdx.bishe.dto.GenerateAiPaperDraftRequest;

public interface AiPaperService {

    AiPaperDraftDto generatePaperDraft(GenerateAiPaperDraftRequest request);
}
