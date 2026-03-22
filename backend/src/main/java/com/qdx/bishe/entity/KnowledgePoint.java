package com.qdx.bishe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_point")
public class KnowledgePoint {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private String pointName;
    private Long parentId;
    private String difficulty;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

