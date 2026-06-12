package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.ForumReply;
import com.school.mapper.ForumPostMapper;
import com.school.mapper.ForumReplyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ForumReplyService extends ServiceImpl<ForumReplyMapper, ForumReply> {

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ForumLikeService forumLikeService;

    @Transactional
    public Map<String, Object> createReply(Integer postId, String content,
                                           String authorType, Integer authorId, String authorName) {
        Map<String, Object> result = new HashMap<>();

        if (forumPostMapper.selectById(postId) == null) {
            result.put("success", false);
            result.put("message", "帖子不存在");
            return result;
        }

        ForumReply reply = new ForumReply();
        reply.setPostId(postId);
        reply.setContent(content);
        reply.setAuthorType(authorType);
        reply.setAuthorId(authorId);
        reply.setAuthorName(authorName);
        reply.setLikeCount(0);
        reply.setCreatedAt(LocalDateTime.now());
        reply.setUpdatedAt(LocalDateTime.now());
        save(reply);

        forumPostMapper.updateReplyCount(postId, 1);
        forumPostMapper.refreshLastReplyAt(postId);

        result.put("success", true);
        result.put("reply", reply);
        return result;
    }

    public Map<String, Object> getRepliesPaged(Integer postId, Integer page, Integer size,
                                               String userType, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        int offset = (page - 1) * size;
        List<ForumReply> replies = baseMapper.findByPostIdPaged(postId, offset, size);
        Integer total = baseMapper.countByPostId(postId);

        if (userId != null && userType != null && !replies.isEmpty()) {
            List<Integer> replyIds = replies.stream()
                    .map(ForumReply::getReplyId)
                    .collect(java.util.stream.Collectors.toList());
            var likedIds = forumLikeService.getLikedReplyIds(userType, userId, replyIds);
            for (ForumReply r : replies) {
                r.setIsLiked(likedIds.contains(r.getReplyId()));
            }
        }

        result.put("replies", replies);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        return result;
    }

    @Transactional
    public Map<String, Object> deleteReply(Integer replyId, String operatorType, Integer operatorId) {
        Map<String, Object> result = new HashMap<>();
        ForumReply reply = getById(replyId);
        if (reply == null) {
            result.put("success", false);
            result.put("message", "回帖不存在");
            return result;
        }

        boolean isOwner = reply.getAuthorType().equals(operatorType) && reply.getAuthorId().equals(operatorId);
        boolean isTeacher = "TEACHER".equals(operatorType);

        if (!isOwner && !isTeacher) {
            result.put("success", false);
            result.put("message", "无权删除此回帖");
            return result;
        }

        forumLikeService.clearLikesForReply(replyId);
        removeById(replyId);
        forumPostMapper.updateReplyCount(reply.getPostId(), -1);

        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    @Transactional
    public void deleteRepliesByPostId(Integer postId) {
        List<Integer> replyIds = baseMapper.findReplyIdsByPostId(postId);
        forumLikeService.clearLikesForReplies(replyIds);
        if (!replyIds.isEmpty()) {
            QueryWrapper<ForumReply> wrapper = new QueryWrapper<>();
            wrapper.eq("post_id", postId);
            remove(wrapper);
        }
    }
}
