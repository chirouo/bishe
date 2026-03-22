package com.qdx.bishe.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QuestionOptionDto {

    private String label;
    private String content;
}

