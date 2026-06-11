package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("homework_submission")
public class HomeworkSubmission {
    @TableId(type = IdType.AUTO)
    private Integer submissionId;
    private Integer homeworkId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String fileUrl;
    private String fileName;
    private Integer submissionCount;
    private String status;
    private BigDecimal score;
    private String comment;
    private LocalDateTime submittedAt;
    private LocalDateTime gradedAt;
    private Integer gradedBy;
    private String gradedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
