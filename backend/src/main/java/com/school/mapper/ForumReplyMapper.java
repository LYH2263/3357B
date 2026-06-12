package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.ForumReply;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ForumReplyMapper extends BaseMapper<ForumReply> {

    @Select("SELECT * FROM forum_reply WHERE post_id = #{postId} " +
            "ORDER BY created_at ASC LIMIT #{offset}, #{size}")
    List<ForumReply> findByPostIdPaged(@Param("postId") Integer postId,
                                       @Param("offset") Integer offset,
                                       @Param("size") Integer size);

    @Select("SELECT COUNT(*) FROM forum_reply WHERE post_id = #{postId}")
    Integer countByPostId(@Param("postId") Integer postId);

    @Update("UPDATE forum_reply SET like_count = like_count + #{delta} WHERE reply_id = #{replyId}")
    Integer updateLikeCount(@Param("replyId") Integer replyId, @Param("delta") Integer delta);

    @Select("SELECT reply_id FROM forum_reply WHERE post_id = #{postId}")
    List<Integer> findReplyIdsByPostId(@Param("postId") Integer postId);
}
