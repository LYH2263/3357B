package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("forum_like")
public class ForumLike {
    @TableId(type = IdType.AUTO)
    private Integer likeId;
    private String targetType;
    private Integer targetId;
    private String userType;
    private Integer userId;
    private String userName;
    private LocalDateTime createdAt;
}
