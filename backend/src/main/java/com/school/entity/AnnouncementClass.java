package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("announcement_class")
public class AnnouncementClass {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer announcementId;
    private Integer classId;
    private String className;
    private LocalDateTime createdAt;
}
