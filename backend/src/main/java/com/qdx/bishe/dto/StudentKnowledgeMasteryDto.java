package com.qdx.bishe.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudentKnowledgeMasteryDto {

    private Long knowledgePointId;
    private String pointName;
    private BigDecimal masteryRate;
}
