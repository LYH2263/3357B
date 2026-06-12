package com.school.controller;

import com.school.service.ForumLikeService;
import com.school.service.ForumPostService;
import com.school.service.ForumReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
@CrossOrigin
public class ForumController {

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumReplyService forumReplyService;

    @Autowired
    private ForumLikeService forumLikeService;

    @PostMapping("/post/create")
    public Map<String, Object> createPost(@RequestBody Map<String, Object> params) {
        Integer classId = (Integer) params.get("classId");
        String title = (String) params.get("title");
        String content = (String) params.get("content");
        String authorType = (String) params.get("authorType");
        Integer authorId = (Integer) params.get("authorId");
        String authorName = (String) params.get("authorName");

        if (classId == null || title == null || title.trim().isEmpty()
                || content == null || content.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "标题和内容不能为空");
            return result;
        }

        return forumPostService.createPost(classId, title.trim(), content.trim(),
                authorType, authorId, authorName);
    }

    @GetMapping("/post/list")
    public Map<String, Object> postList(
            @RequestParam Integer classId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) Integer userId) {
        return forumPostService.getPostsPaged(classId, page, size, userType, userId);
    }

    @GetMapping("/post/detail/{postId}")
    public Map<String, Object> postDetail(
            @PathVariable Integer postId,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) Integer userId) {
        return forumPostService.getPostDetail(postId, userType, userId);
    }

    @PostMapping("/post/delete")
    public Map<String, Object> deletePost(@RequestBody Map<String, Object> params) {
        Integer postId = (Integer) params.get("postId");
        String operatorType = (String) params.get("operatorType");
        Integer operatorId = (Integer) params.get("operatorId");
        return forumPostService.deletePost(postId, operatorType, operatorId);
    }

    @PostMapping("/post/toggle-pin")
    public Map<String, Object> togglePin(@RequestBody Map<String, Object> params) {
        Integer postId = (Integer) params.get("postId");
        Integer teacherId = (Integer) params.get("teacherId");
        return forumPostService.togglePin(postId, teacherId);
    }

    @PostMapping("/reply/create")
    public Map<String, Object> createReply(@RequestBody Map<String, Object> params) {
        Integer postId = (Integer) params.get("postId");
        String content = (String) params.get("content");
        String authorType = (String) params.get("authorType");
        Integer authorId = (Integer) params.get("authorId");
        String authorName = (String) params.get("authorName");

        if (content == null || content.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "回帖内容不能为空");
            return result;
        }

        return forumReplyService.createReply(postId, content.trim(),
                authorType, authorId, authorName);
    }

    @GetMapping("/reply/list")
    public Map<String, Object> replyList(
            @RequestParam Integer postId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) Integer userId) {
        return forumReplyService.getRepliesPaged(postId, page, size, userType, userId);
    }

    @PostMapping("/reply/delete")
    public Map<String, Object> deleteReply(@RequestBody Map<String, Object> params) {
        Integer replyId = (Integer) params.get("replyId");
        String operatorType = (String) params.get("operatorType");
        Integer operatorId = (Integer) params.get("operatorId");
        return forumReplyService.deleteReply(replyId, operatorType, operatorId);
    }

    @PostMapping("/like/toggle")
    public Map<String, Object> toggleLike(@RequestBody Map<String, Object> params) {
        String targetType = (String) params.get("targetType");
        Integer targetId = (Integer) params.get("targetId");
        String userType = (String) params.get("userType");
        Integer userId = (Integer) params.get("userId");
        String userName = (String) params.get("userName");
        return forumLikeService.toggleLike(targetType, targetId, userType, userId, userName);
    }
}
