package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("point_summary")
public class PointSummary {
    @TableId(type = IdType.AUTO)
    private Integer summaryId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private Integer classId;
    private String className;
    private Integer totalPoints;
    private LocalDateTime updatedAt;
}
