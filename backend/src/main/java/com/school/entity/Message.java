package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    @TableId(type = IdType.AUTO)
    private Integer messageId;
    private Integer conversationId;
    private String senderType;
    private Integer senderId;
    private String senderName;
    private String content;
    private Integer isRead;
    private LocalDateTime createdAt;
}
