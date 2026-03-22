package com.qdx.bishe.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreatePaperQuestionRequest {

    @NotNull(message = "题目不能为空")
    private Long questionId;

    @NotNull(message = "分值不能为空")
    @DecimalMin(value = "0.01", message = "分值必须大于 0")
    private BigDecimal score;
}

