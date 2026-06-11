package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("course_enrollment")
public class CourseEnrollment {
    @TableId(type = IdType.AUTO)
    private Integer enrollmentId;
    private Integer courseId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String className;
    private LocalDateTime enrollTime;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
