package com.school.controller;

import com.school.entity.Conversation;
import com.school.entity.Message;
import com.school.service.ConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/message")
@CrossOrigin
public class MessageController {

    @Autowired
    private ConversationService conversationService;

    @PostMapping("/conversation/get-or-create")
    public Map<String, Object> getOrCreateConversation(@RequestBody Map<String, Object> params) {
        Integer studentId = (Integer) params.get("studentId");
        Integer teacherId = (Integer) params.get("teacherId");
        return conversationService.getOrCreateConversation(studentId, teacherId);
    }

    @GetMapping("/conversation/list/student")
    public List<Conversation> studentConversationList(@RequestParam Integer studentId) {
        return conversationService.listByStudent(studentId);
    }

    @GetMapping("/conversation/list/teacher")
    public List<Conversation> teacherConversationList(@RequestParam Integer teacherId) {
        return conversationService.listByTeacher(teacherId);
    }

    @GetMapping("/conversation/unread-count")
    public Map<String, Object> unreadCount(@RequestParam Integer userId, @RequestParam String role) {
        return conversationService.getUnreadCount(userId, role);
    }

    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> params) {
        Integer conversationId = (Integer) params.get("conversationId");
        String senderType = (String) params.get("senderType");
        Integer senderId = (Integer) params.get("senderId");
        String content = (String) params.get("content");
        return conversationService.sendMessage(conversationId, senderType, senderId, content);
    }

    @GetMapping("/list")
    public Map<String, Object> messageList(
            @RequestParam Integer conversationId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String readerRole) {

        if (readerRole != null && !readerRole.isEmpty()) {
            conversationService.markAsRead(conversationId, readerRole);
        }

        List<Message> messages = conversationService.getMessages(conversationId, page, size);
        Integer total = conversationService.getMessageCount(conversationId);

        Map<String, Object> result = new HashMap<>();
        result.put("messages", messages);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return result;
    }

    @PostMapping("/mark-read")
    public Map<String, Object> markRead(@RequestBody Map<String, Object> params) {
        Integer conversationId = (Integer) params.get("conversationId");
        String readerRole = (String) params.get("readerRole");
        conversationService.markAsRead(conversationId, readerRole);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        return result;
    }

    @GetMapping("/conversation/detail/{id}")
    public Conversation conversationDetail(@PathVariable Integer id) {
        return conversationService.getById(id);
    }
}
