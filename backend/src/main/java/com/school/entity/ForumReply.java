package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_reply")
public class ForumReply {
    @TableId(type = IdType.AUTO)
    private Integer replyId;
    private Integer postId;
    private String content;
    private String authorType;
    private Integer authorId;
    private String authorName;
    private Integer likeCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @TableField(exist = false)
    private Boolean isLiked;
}
