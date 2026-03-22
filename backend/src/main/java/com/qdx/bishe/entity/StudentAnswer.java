package com.qdx.bishe.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("student_answer")
public class StudentAnswer {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long examRecordId;
    private Long questionId;
    private String answerContent;
    private Integer isCorrect;
    private BigDecimal score;
    private String feedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
