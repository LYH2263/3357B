package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.ForumLike;
import com.school.mapper.ForumLikeMapper;
import com.school.mapper.ForumPostMapper;
import com.school.mapper.ForumReplyMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ForumLikeService extends ServiceImpl<ForumLikeMapper, ForumLike> {

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ForumReplyMapper forumReplyMapper;

    @Transactional
    public Map<String, Object> toggleLike(String targetType, Integer targetId,
                                          String userType, Integer userId, String userName) {
        Map<String, Object> result = new HashMap<>();
        ForumLike existing = baseMapper.findOne(targetType, targetId, userType, userId);

        if (existing != null) {
            baseMapper.deleteOne(targetType, targetId, userType, userId);
            if ("POST".equals(targetType)) {
                forumPostMapper.updateLikeCount(targetId, -1);
            } else if ("REPLY".equals(targetType)) {
                forumReplyMapper.updateLikeCount(targetId, -1);
            }
            result.put("liked", false);
            result.put("action", "unliked");
        } else {
            try {
                ForumLike like = new ForumLike();
                like.setTargetType(targetType);
                like.setTargetId(targetId);
                like.setUserType(userType);
                like.setUserId(userId);
                like.setUserName(userName);
                like.setCreatedAt(LocalDateTime.now());
                save(like);
                if ("POST".equals(targetType)) {
                    forumPostMapper.updateLikeCount(targetId, 1);
                } else if ("REPLY".equals(targetType)) {
                    forumReplyMapper.updateLikeCount(targetId, 1);
                }
                result.put("liked", true);
                result.put("action", "liked");
            } catch (DuplicateKeyException e) {
                result.put("liked", true);
                result.put("action", "idempotent");
            }
        }

        Integer currentCount;
        if ("POST".equals(targetType)) {
            currentCount = forumPostMapper.selectById(targetId).getLikeCount();
        } else {
            currentCount = forumReplyMapper.selectById(targetId).getLikeCount();
        }
        result.put("likeCount", currentCount);
        result.put("success", true);
        return result;
    }

    public Set<Integer> getLikedPostIds(String userType, Integer userId, List<Integer> postIds) {
        if (postIds == null || postIds.isEmpty()) return Collections.emptySet();
        String ids = String.join(",", postIds.stream().map(String::valueOf).toArray(String[]::new));
        List<Integer> liked = baseMapper.findLikedIds("POST", userType, userId, ids);
        return new HashSet<>(liked);
    }

    public Set<Integer> getLikedReplyIds(String userType, Integer userId, List<Integer> replyIds) {
        if (replyIds == null || replyIds.isEmpty()) return Collections.emptySet();
        String ids = String.join(",", replyIds.stream().map(String::valueOf).toArray(String[]::new));
        List<Integer> liked = baseMapper.findLikedIds("REPLY", userType, userId, ids);
        return new HashSet<>(liked);
    }

    public Boolean isLiked(String targetType, Integer targetId, String userType, Integer userId) {
        return baseMapper.findOne(targetType, targetId, userType, userId) != null;
    }

    @Transactional
    public void clearLikesForPost(Integer postId) {
        baseMapper.deleteByTarget("POST", postId);
    }

    @Transactional
    public void clearLikesForReply(Integer replyId) {
        baseMapper.deleteByTarget("REPLY", replyId);
    }

    @Transactional
    public void clearLikesForReplies(List<Integer> replyIds) {
        if (replyIds == null || replyIds.isEmpty()) return;
        for (Integer replyId : replyIds) {
            baseMapper.deleteByTarget("REPLY", replyId);
        }
    }
}
