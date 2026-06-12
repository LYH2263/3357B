package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("point_rule")
public class PointRule {
    @TableId(type = IdType.AUTO)
    private Integer ruleId;
    private String ruleCode;
    private String ruleName;
    private Integer pointValue;
    private String sourceType;
    private String description;
    private Integer isEnabled;
    private Integer dailyLimit;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
