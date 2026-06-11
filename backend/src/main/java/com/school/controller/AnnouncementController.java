package com.school.controller;

import com.school.entity.Announcement;
import com.school.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcement")
@CrossOrigin
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping("/list")
    public List<Announcement> list() {
        return announcementService.listForTeacher();
    }

    @GetMapping("/detail/{id}")
    public Announcement detail(@PathVariable Integer id) {
        return announcementService.getDetail(id);
    }

    @PostMapping("/save")
    public boolean save(@RequestBody Announcement announcement) {
        return announcementService.saveAnnouncement(announcement);
    }

    @DeleteMapping("/delete/{id}")
    public boolean delete(@PathVariable Integer id) {
        return announcementService.removeById(id);
    }

    @PostMapping("/publish/{id}")
    public boolean publish(@PathVariable Integer id) {
        return announcementService.publishAnnouncement(id);
    }

    @PostMapping("/revoke/{id}")
    public boolean revoke(@PathVariable Integer id) {
        return announcementService.revokeAnnouncement(id);
    }

    @GetMapping("/student/list")
    public List<Announcement> studentList(@RequestParam Integer studentId, @RequestParam Integer classId) {
        return announcementService.getVisibleForStudent(studentId, classId);
    }

    @GetMapping("/student/unread-count")
    public Map<String, Object> unreadCount(@RequestParam Integer studentId, @RequestParam Integer classId) {
        int count = announcementService.getUnreadCount(studentId, classId);
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", count);
        return result;
    }

    @PostMapping("/student/mark-read")
    public Map<String, Object> markRead(@RequestBody Map<String, Integer> params) {
        Integer announcementId = params.get("announcementId");
        Integer studentId = params.get("studentId");
        boolean success = announcementService.markAsRead(announcementId, studentId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return result;
    }

    @PostMapping("/student/mark-all-read")
    public Map<String, Object> markAllRead(@RequestBody Map<String, Integer> params) {
        Integer studentId = params.get("studentId");
        Integer classId = params.get("classId");
        boolean success = announcementService.markAllAsRead(studentId, classId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return result;
    }
}
