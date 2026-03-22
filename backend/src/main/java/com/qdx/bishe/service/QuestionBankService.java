package com.qdx.bishe.service;

import com.qdx.bishe.dto.CreateQuestionRequest;
import com.qdx.bishe.dto.QuestionListItemDto;

import java.util.List;

public interface QuestionBankService {

    List<QuestionListItemDto> listQuestions(Long courseId, Long knowledgePointId, String questionType);

    Long createSingleChoiceQuestion(CreateQuestionRequest request, Long currentUserId);
}

