package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sign_in_record")
public class SignInRecord {
    @TableId(type = IdType.AUTO)
    private Integer recordId;
    private Integer taskId;
    private Integer studentId;
    private String studentName;
    private String studentNo;
    private String status;
    private LocalDateTime signInTime;
    private String signInIp;
    private Integer isManual;
    private Integer manualBy;
    private String manualByName;
    private LocalDateTime manualTime;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
