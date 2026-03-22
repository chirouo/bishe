package com.qdx.bishe.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AiPaperDraftDto {

    private String provider;
    private String source;
    private String title;
    private String description;
    private String strategySummary;
    private List<AiPaperDraftQuestionDto> questions;
}
