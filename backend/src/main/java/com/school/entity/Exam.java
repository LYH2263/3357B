package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("exam")
public class Exam {
    @TableId(type = IdType.AUTO)
    private Integer examId;
    private String examTitle;
    private Integer courseId;
    private String courseName;
    private Integer durationMinutes;
    private BigDecimal passScore;
    private BigDecimal totalScore;
    private String status;
    private Integer maxAttempts;
    private String scoringRule;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<ExamQuestion> questions;
}
