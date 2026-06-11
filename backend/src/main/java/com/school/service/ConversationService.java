package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Conversation;
import com.school.entity.Message;
import com.school.entity.User;
import com.school.entity.Teacher;
import com.school.mapper.ConversationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConversationService extends ServiceImpl<ConversationMapper, Conversation> {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private TeacherService teacherService;

    @Transactional
    public Map<String, Object> getOrCreateConversation(Integer studentId, Integer teacherId) {
        Map<String, Object> result = new HashMap<>();

        User student = userService.getById(studentId);
        if (student == null) {
            result.put("success", false);
            result.put("message", "学生不存在");
            return result;
        }

        Teacher teacher = teacherService.getById(teacherId);
        if (teacher == null) {
            result.put("success", false);
            result.put("message", "教师不存在");
            return result;
        }

        Conversation existing = this.lambdaQuery()
                .eq(Conversation::getStudentId, studentId)
                .eq(Conversation::getTeacherId, teacherId)
                .one();

        if (existing != null) {
            result.put("success", true);
            result.put("conversation", existing);
            result.put("created", false);
            return result;
        }

        Conversation conversation = new Conversation();
        conversation.setStudentId(studentId);
        conversation.setStudentName(student.getUsername());
        conversation.setTeacherId(teacherId);
        conversation.setTeacherName(teacher.getTname());
        conversation.setStudentUnreadCount(0);
        conversation.setTeacherUnreadCount(0);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        this.save(conversation);

        result.put("success", true);
        result.put("conversation", conversation);
        result.put("created", true);
        return result;
    }

    public List<Conversation> listByStudent(Integer studentId) {
        return this.lambdaQuery()
                .eq(Conversation::getStudentId, studentId)
                .orderByDesc(Conversation::getLastMessageTime)
                .list();
    }

    public List<Conversation> listByTeacher(Integer teacherId) {
        return this.lambdaQuery()
                .eq(Conversation::getTeacherId, teacherId)
                .orderByDesc(Conversation::getLastMessageTime)
                .list();
    }

    public Map<String, Object> getUnreadCount(Integer userId, String role) {
        Map<String, Object> result = new HashMap<>();
        int unreadCount = 0;

        if ("STUDENT".equals(role)) {
            List<Conversation> conversations = this.lambdaQuery()
                    .eq(Conversation::getStudentId, userId)
                    .list();
            for (Conversation c : conversations) {
                unreadCount += c.getStudentUnreadCount();
            }
        } else if ("TEACHER".equals(role)) {
            List<Conversation> conversations = this.lambdaQuery()
                    .eq(Conversation::getTeacherId, userId)
                    .list();
            for (Conversation c : conversations) {
                unreadCount += c.getTeacherUnreadCount();
            }
        }

        result.put("unreadCount", unreadCount);
        return result;
    }

    @Transactional
    public Map<String, Object> sendMessage(Integer conversationId, String senderType, Integer senderId, String content) {
        Map<String, Object> result = new HashMap<>();

        Conversation conversation = this.getById(conversationId);
        if (conversation == null) {
            result.put("success", false);
            result.put("message", "会话不存在");
            return result;
        }

        if (content == null || content.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "消息内容不能为空");
            return result;
        }

        String senderName;
        if ("STUDENT".equals(senderType)) {
            User student = userService.getById(senderId);
            if (student == null) {
                result.put("success", false);
                result.put("message", "发送方学生不存在");
                return result;
            }
            senderName = student.getUsername();
        } else {
            Teacher teacher = teacherService.getById(senderId);
            if (teacher == null) {
                result.put("success", false);
                result.put("message", "发送方教师不存在");
                return result;
            }
            senderName = teacher.getTname();
        }

        Message message = new Message();
        message.setConversationId(conversationId);
        message.setSenderType(senderType);
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setContent(content.trim());
        message.setIsRead(0);
        message.setCreatedAt(LocalDateTime.now());
        messageService.save(message);

        String preview = content.trim();
        if (preview.length() > 50) {
            preview = preview.substring(0, 50) + "...";
        }

        conversation.setLastMessage(preview);
        conversation.setLastMessageTime(LocalDateTime.now());

        if ("STUDENT".equals(senderType)) {
            conversation.setTeacherUnreadCount(conversation.getTeacherUnreadCount() + 1);
        } else {
            conversation.setStudentUnreadCount(conversation.getStudentUnreadCount() + 1);
        }

        conversation.setUpdatedAt(LocalDateTime.now());
        this.updateById(conversation);

        result.put("success", true);
        result.put("message", message);
        return result;
    }

    @Transactional
    public void markAsRead(Integer conversationId, String readerRole) {
        Conversation conversation = this.getById(conversationId);
        if (conversation == null) return;

        if ("STUDENT".equals(readerRole)) {
            messageService.lambdaUpdate()
                    .eq(Message::getConversationId, conversationId)
                    .eq(Message::getSenderType, "TEACHER")
                    .eq(Message::getIsRead, 0)
                    .set(Message::getIsRead, 1)
                    .update();

            conversation.setStudentUnreadCount(0);
        } else {
            messageService.lambdaUpdate()
                    .eq(Message::getConversationId, conversationId)
                    .eq(Message::getSenderType, "STUDENT")
                    .eq(Message::getIsRead, 0)
                    .set(Message::getIsRead, 1)
                    .update();

            conversation.setTeacherUnreadCount(0);
        }

        this.updateById(conversation);
    }

    public List<Message> getMessages(Integer conversationId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        List<Message> allMessages = messageService.lambdaQuery()
                .eq(Message::getConversationId, conversationId)
                .orderByAsc(Message::getCreatedAt)
                .list();

        int total = allMessages.size();
        int fromIndex = Math.max(0, total - offset - size);
        int toIndex = Math.max(0, total - offset);

        if (fromIndex >= toIndex && page > 1) {
            return List.of();
        }

        return allMessages.subList(fromIndex, toIndex);
    }

    public Integer getMessageCount(Integer conversationId) {
        return (int) messageService.lambdaQuery()
                .eq(Message::getConversationId, conversationId)
                .count();
    }
}
