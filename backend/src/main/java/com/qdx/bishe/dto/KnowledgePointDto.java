package com.qdx.bishe.dto;

import lombok.Data;

@Data
public class KnowledgePointDto {

    private Long id;
    private Long courseId;
    private String courseName;
    private String pointName;
    private Long parentId;
    private String difficulty;
    private String description;
}

