package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.ForumPost;
import com.school.mapper.ClassesMapper;
import com.school.mapper.ForumPostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ForumPostService extends ServiceImpl<ForumPostMapper, ForumPost> {

    @Autowired
    private ForumReplyService forumReplyService;

    @Autowired
    private ForumLikeService forumLikeService;

    @Autowired
    private ClassesMapper classesMapper;

    @Transactional
    public Map<String, Object> createPost(Integer classId, String title, String content,
                                          String authorType, Integer authorId, String authorName) {
        Map<String, Object> result = new HashMap<>();
        var classes = classesMapper.selectById(classId);
        if (classes == null) {
            result.put("success", false);
            result.put("message", "班级不存在");
            return result;
        }

        ForumPost post = new ForumPost();
        post.setClassId(classId);
        post.setClassName(classes.getCname());
        post.setTitle(title);
        post.setContent(content);
        post.setAuthorType(authorType);
        post.setAuthorId(authorId);
        post.setAuthorName(authorName);
        post.setIsPinned(0);
        post.setPinOrder(0);
        post.setReplyCount(0);
        post.setLikeCount(0);
        post.setLastReplyAt(null);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        save(post);

        result.put("success", true);
        result.put("post", post);
        return result;
    }

    public Map<String, Object> getPostsPaged(Integer classId, Integer page, Integer size,
                                             String userType, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        int offset = (page - 1) * size;
        List<ForumPost> posts = baseMapper.findByClassIdPaged(classId, offset, size);
        Integer total = baseMapper.countByClassId(classId);

        if (userId != null && userType != null && !posts.isEmpty()) {
            List<Integer> postIds = posts.stream()
                    .map(ForumPost::getPostId)
                    .collect(java.util.stream.Collectors.toList());
            var likedIds = forumLikeService.getLikedPostIds(userType, userId, postIds);
            for (ForumPost p : posts) {
                p.setIsLiked(likedIds.contains(p.getPostId()));
            }
        }

        result.put("posts", posts);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        return result;
    }

    public Map<String, Object> getPostDetail(Integer postId, String userType, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        ForumPost post = getById(postId);
        if (post == null) {
            result.put("success", false);
            result.put("message", "帖子不存在");
            return result;
        }
        if (userId != null && userType != null) {
            post.setIsLiked(forumLikeService.isLiked("POST", postId, userType, userId));
        }
        result.put("success", true);
        result.put("post", post);
        return result;
    }

    @Transactional
    public Map<String, Object> deletePost(Integer postId, String operatorType, Integer operatorId) {
        Map<String, Object> result = new HashMap<>();
        ForumPost post = getById(postId);
        if (post == null) {
            result.put("success", false);
            result.put("message", "帖子不存在");
            return result;
        }

        boolean isOwner = post.getAuthorType().equals(operatorType) && post.getAuthorId().equals(operatorId);
        boolean isTeacher = "TEACHER".equals(operatorType);

        if (!isOwner && !isTeacher) {
            result.put("success", false);
            result.put("message", "无权删除此帖子");
            return result;
        }

        forumReplyService.deleteRepliesByPostId(postId);
        forumLikeService.clearLikesForPost(postId);
        removeById(postId);

        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }

    @Transactional
    public Map<String, Object> togglePin(Integer postId, Integer teacherId) {
        Map<String, Object> result = new HashMap<>();
        ForumPost post = getById(postId);
        if (post == null) {
            result.put("success", false);
            result.put("message", "帖子不存在");
            return result;
        }

        if (post.getIsPinned() == 1) {
            post.setIsPinned(0);
            post.setPinOrder(0);
            result.put("pinned", false);
        } else {
            List<ForumPost> pinned = baseMapper.findPinnedByClassId(post.getClassId());
            int maxOrder = pinned.stream()
                    .mapToInt(ForumPost::getPinOrder)
                    .max().orElse(0);
            post.setIsPinned(1);
            post.setPinOrder(maxOrder + 1);
            result.put("pinned", true);
        }
        post.setUpdatedAt(LocalDateTime.now());
        updateById(post);
        result.put("success", true);
        result.put("post", post);
        return result;
    }
}
