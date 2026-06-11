package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("elective_course")
public class ElectiveCourse {
    @TableId(type = IdType.AUTO)
    private Integer courseId;
    private String courseName;
    private String description;
    private Integer teacherId;
    private String teacherName;
    private Integer capacity;
    private Integer enrolledCount;
    private LocalDateTime enrollStartTime;
    private LocalDateTime enrollEndTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
