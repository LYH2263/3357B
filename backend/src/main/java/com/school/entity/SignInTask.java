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
@TableName("sign_in_task")
public class SignInTask {
    @TableId(type = IdType.AUTO)
    private Integer taskId;
    private String title;
    private Integer classId;
    private String className;
    private Integer durationMinutes;
    private String status;
    private Integer createdBy;
    private String createdByName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer signedCount;
    private Integer absentCount;
    private Integer leaveCount;
    private Integer totalStudents;
    private BigDecimal attendanceRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<SignInRecord> records;

    @TableField(exist = false)
    private Long remainingSeconds;
}
