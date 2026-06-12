package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("point_detail")
public class PointDetail {
    @TableId(type = IdType.AUTO)
    private Integer detailId;
    private Integer studentId;
    private String studentName;
    private String ruleCode;
    private String ruleName;
    private Integer pointValue;
    private String sourceType;
    private String sourceId;
    private String reason;
    private Integer operatorId;
    private String operatorName;
    private Integer balanceAfter;
    private LocalDateTime createdAt;
}
