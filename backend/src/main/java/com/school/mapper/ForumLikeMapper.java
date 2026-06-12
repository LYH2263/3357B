package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.ForumLike;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ForumLikeMapper extends BaseMapper<ForumLike> {

    @Select("SELECT * FROM forum_like WHERE target_type = #{targetType} " +
            "AND target_id = #{targetId} AND user_type = #{userType} AND user_id = #{userId}")
    ForumLike findOne(@Param("targetType") String targetType,
                      @Param("targetId") Integer targetId,
                      @Param("userType") String userType,
                      @Param("userId") Integer userId);

    @Delete("DELETE FROM forum_like WHERE target_type = #{targetType} " +
            "AND target_id = #{targetId} AND user_type = #{userType} AND user_id = #{userId}")
    Integer deleteOne(@Param("targetType") String targetType,
                      @Param("targetId") Integer targetId,
                      @Param("userType") String userType,
                      @Param("userId") Integer userId);

    @Select("SELECT target_id FROM forum_like WHERE target_type = #{targetType} " +
            "AND user_type = #{userType} AND user_id = #{userId} AND target_id IN (${ids})")
    List<Integer> findLikedIds(@Param("targetType") String targetType,
                               @Param("userType") String userType,
                               @Param("userId") Integer userId,
                               @Param("ids") String ids);

    @Delete("DELETE FROM forum_like WHERE target_type = #{targetType} AND target_id = #{targetId}")
    Integer deleteByTarget(@Param("targetType") String targetType, @Param("targetId") Integer targetId);
}
