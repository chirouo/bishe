package com.qdx.bishe.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class GenerateAiPaperDraftRequest {

    @NotNull(message = "课程不能为空")
    private Long courseId;

    @NotNull(message = "题目数量不能为空")
    @Min(value = 1, message = "题目数量至少为 1")
    @Max(value = 20, message = "题目数量不能超过 20")
    private Integer questionCount;

    private BigDecimal scorePerQuestion;

    private String requirements;
}
