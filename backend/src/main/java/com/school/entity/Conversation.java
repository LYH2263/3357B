package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("conversation")
public class Conversation {
    @TableId(type = IdType.AUTO)
    private Integer conversationId;
    private Integer studentId;
    private String studentName;
    private Integer teacherId;
    private String teacherName;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Integer studentUnreadCount;
    private Integer teacherUnreadCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<Message> messages;

    @TableField(exist = false)
    private Integer messageCount;
}
