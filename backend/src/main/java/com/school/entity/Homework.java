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
@TableName("homework")
public class Homework {
    @TableId(type = IdType.AUTO)
    private Integer homeworkId;
    private String title;
    private String description;
    private Integer classId;
    private String className;
    private LocalDateTime deadline;
    private BigDecimal fullScore;
    private Integer createdBy;
    private String createdByName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<HomeworkSubmission> submissions;

    @TableField(exist = false)
    private Integer submittedCount;

    @TableField(exist = false)
    private Integer unsubmittedCount;

    @TableField(exist = false)
    private Integer gradedCount;

    @TableField(exist = false)
    private Integer totalStudents;
}
