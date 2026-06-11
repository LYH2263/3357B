package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.AnnouncementRead;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementReadMapper extends BaseMapper<AnnouncementRead> {

    @Select("SELECT * FROM announcement_read WHERE student_id = #{studentId}")
    List<AnnouncementRead> findByStudentId(@Param("studentId") Integer studentId);

    @Select("SELECT * FROM announcement_read WHERE announcement_id = #{announcementId} AND student_id = #{studentId}")
    AnnouncementRead findByAnnouncementAndStudent(@Param("announcementId") Integer announcementId, @Param("studentId") Integer studentId);
}
