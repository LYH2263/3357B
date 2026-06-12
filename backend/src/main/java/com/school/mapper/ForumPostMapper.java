package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {

    @Select("SELECT * FROM forum_post WHERE class_id = #{classId} " +
            "ORDER BY is_pinned DESC, pin_order ASC, " +
            "COALESCE(last_reply_at, created_at) DESC " +
            "LIMIT #{offset}, #{size}")
    List<ForumPost> findByClassIdPaged(@Param("classId") Integer classId,
                                       @Param("offset") Integer offset,
                                       @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM forum_post WHERE class_id = #{classId}")
    Integer countByClassId(@Param("classId") Integer classId);

    @Update("UPDATE forum_post SET reply_count = reply_count + #{delta} WHERE post_id = #{postId}")
    Integer updateReplyCount(@Param("postId") Integer postId, @Param("delta") Integer delta);

    @Update("UPDATE forum_post SET like_count = like_count + #{delta} WHERE post_id = #{postId}")
    Integer updateLikeCount(@Param("postId") Integer postId, @Param("delta") Integer delta);

    @Update("UPDATE forum_post SET last_reply_at = NOW() WHERE post_id = #{postId}")
    Integer refreshLastReplyAt(@Param("postId") Integer postId);

    @Select("SELECT * FROM forum_post WHERE class_id = #{classId} AND is_pinned = 1 " +
            "ORDER BY pin_order ASC, COALESCE(last_reply_at, created_at) DESC")
    List<ForumPost> findPinnedByClassId(@Param("classId") Integer classId);
}
