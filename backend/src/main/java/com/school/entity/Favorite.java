package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("favorite")
public class Favorite {
    @TableId(type = IdType.AUTO)
    private Integer favoriteId;
    private Integer studentId;
    private String studentName;
    private String resourceType;
    private Integer resourceId;
    private String resourceTitle;
    private LocalDateTime createdAt;
}
