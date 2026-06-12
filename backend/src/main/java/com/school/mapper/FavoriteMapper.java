package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT resource_id AS resourceId, resource_title AS resourceTitle, COUNT(*) AS favoriteCount " +
            "FROM favorite " +
            "WHERE resource_type = #{resourceType} " +
            "GROUP BY resource_id, resource_title " +
            "ORDER BY favoriteCount DESC " +
            "LIMIT #{limit}")
    List<Map<String, Object>> getFavoriteRanking(@Param("resourceType") String resourceType, @Param("limit") Integer limit);

    @Select("SELECT resource_id AS resourceId, resource_type AS resourceType FROM favorite WHERE student_id = #{studentId}")
    List<Map<String, Object>> getFavoriteIdsByStudent(@Param("studentId") Integer studentId);
}
