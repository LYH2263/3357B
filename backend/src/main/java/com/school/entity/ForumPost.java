package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_post")
public class ForumPost {
    @TableId(type = IdType.AUTO)
    private Integer postId;
    private Integer classId;
    private String className;
    private String title;
    private String content;
    private String authorType;
    private Integer authorId;
    private String authorName;
    private Integer isPinned;
    private Integer pinOrder;
    private Integer replyCount;
    private Integer likeCount;
    private LocalDateTime lastReplyAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Boolean isLiked;
}
