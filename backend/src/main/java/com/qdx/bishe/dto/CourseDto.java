package com.qdx.bishe.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseDto {

    private Long id;
    private String courseName;
    private String description;
}

