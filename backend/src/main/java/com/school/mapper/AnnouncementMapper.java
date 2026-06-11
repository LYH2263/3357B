package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    @Select("SELECT DISTINCT a.* FROM announcement a " +
            "LEFT JOIN announcement_class ac ON a.announcement_id = ac.announcement_id " +
            "WHERE a.status = 'PUBLISHED' " +
            "AND (a.target_type = 'ALL' OR ac.class_id = #{classId}) " +
            "AND (a.effective_time IS NULL OR a.effective_time <= NOW()) " +
            "AND (a.expire_time IS NULL OR a.expire_time >= NOW()) " +
            "ORDER BY a.is_pinned DESC, a.pin_order ASC, a.published_at DESC")
    List<Announcement> findVisibleByStudentClass(@Param("classId") Integer classId);

    @Select("SELECT COUNT(DISTINCT a.announcement_id) FROM announcement a " +
            "LEFT JOIN announcement_class ac ON a.announcement_id = ac.announcement_id " +
            "LEFT JOIN announcement_read ar ON a.announcement_id = ar.announcement_id AND ar.student_id = #{studentId} " +
            "WHERE a.status = 'PUBLISHED' " +
            "AND (a.target_type = 'ALL' OR ac.class_id = #{classId}) " +
            "AND (a.effective_time IS NULL OR a.effective_time <= NOW()) " +
            "AND (a.expire_time IS NULL OR a.expire_time >= NOW()) " +
            "AND ar.read_id IS NULL")
    Integer countUnreadForStudent(@Param("studentId") Integer studentId, @Param("classId") Integer classId);
}
