package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.AnnouncementClass;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementClassMapper extends BaseMapper<AnnouncementClass> {

    @Select("SELECT * FROM announcement_class WHERE announcement_id = #{announcementId}")
    List<AnnouncementClass> findByAnnouncementId(@Param("announcementId") Integer announcementId);

    @Delete("DELETE FROM announcement_class WHERE announcement_id = #{announcementId}")
    void deleteByAnnouncementId(@Param("announcementId") Integer announcementId);
}
