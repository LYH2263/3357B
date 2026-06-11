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
@TableName("exam_attempt")
public class ExamAttempt {
    @TableId(type = IdType.AUTO)
    private Integer attemptId;
    private Integer examId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private Integer attemptNo;
    private BigDecimal score;
    private BigDecimal totalScore;
    private Integer timeSpentSeconds;
    private Integer isSubmitted;
    private Integer isTimeout;
    private LocalDateTime startTime;
    private LocalDateTime submitTime;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private Exam exam;

    @TableField(exist = false)
    private List<ExamAnswer> answers;
}
