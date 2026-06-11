package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO)
    private Integer announcementId;
    private String title;
    private String content;
    private String contentType;
    private String targetType;
    private Integer isPinned;
    private Integer pinOrder;
    private String status;
    private LocalDateTime effectiveTime;
    private LocalDateTime expireTime;
    private Integer createdBy;
    private String createdByName;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private List<Integer> classIds;

    @TableField(exist = false)
    private List<AnnouncementClass> classList;

    @TableField(exist = false)
    private Boolean isRead;

    @TableField(exist = false)
    private Integer unreadCount;
}
