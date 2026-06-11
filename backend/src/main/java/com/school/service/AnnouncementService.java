package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.*;
import com.school.mapper.AnnouncementMapper;
import com.school.mapper.ClassesMapper;
import com.school.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnnouncementService extends ServiceImpl<AnnouncementMapper, Announcement> {

    @Autowired
    private AnnouncementClassService announcementClassService;

    @Autowired
    private AnnouncementReadService announcementReadService;

    @Autowired
    private ClassesMapper classesMapper;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public boolean saveAnnouncement(Announcement announcement) {
        if (announcement.getAnnouncementId() == null) {
            announcement.setCreatedAt(LocalDateTime.now());
        }
        announcement.setUpdatedAt(LocalDateTime.now());
        boolean result = saveOrUpdate(announcement);

        if (result && "SPECIFIED".equals(announcement.getTargetType())) {
            announcementClassService.updateAnnouncementClasses(announcement.getAnnouncementId(), announcement.getClassIds());
        } else if (result && "ALL".equals(announcement.getTargetType())) {
            announcementClassService.removeByAnnouncementId(announcement.getAnnouncementId());
        }

        return result;
    }

    @Transactional
    public boolean publishAnnouncement(Integer announcementId) {
        Announcement announcement = getById(announcementId);
        if (announcement == null) return false;
        announcement.setStatus("PUBLISHED");
        announcement.setPublishedAt(LocalDateTime.now());
        announcement.setUpdatedAt(LocalDateTime.now());
        return updateById(announcement);
    }

    @Transactional
    public boolean revokeAnnouncement(Integer announcementId) {
        Announcement announcement = getById(announcementId);
        if (announcement == null) return false;
        announcement.setStatus("REVOKED");
        announcement.setUpdatedAt(LocalDateTime.now());
        return updateById(announcement);
    }

    public List<Announcement> listForTeacher() {
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("created_at");
        List<Announcement> list = list(wrapper);
        for (Announcement a : list) {
            a.setClassList(announcementClassService.getByAnnouncementId(a.getAnnouncementId()));
        }
        return list;
    }

    public Announcement getDetail(Integer announcementId) {
        Announcement announcement = getById(announcementId);
        if (announcement != null) {
            announcement.setClassList(announcementClassService.getByAnnouncementId(announcementId));
            if (announcement.getClassList() != null && !announcement.getClassList().isEmpty()) {
                announcement.setClassIds(announcement.getClassList().stream()
                        .map(AnnouncementClass::getClassId)
                        .collect(Collectors.toList()));
            }
        }
        return announcement;
    }

    public List<Announcement> getVisibleForStudent(Integer studentId, Integer classId) {
        List<Announcement> visibleAnnouncements = baseMapper.findVisibleByStudentClass(classId);
        Set<Integer> readAnnouncementIds = announcementReadService.getReadAnnouncementIds(studentId);

        for (Announcement a : visibleAnnouncements) {
            a.setIsRead(readAnnouncementIds.contains(a.getAnnouncementId()));
            a.setClassList(announcementClassService.getByAnnouncementId(a.getAnnouncementId()));
        }
        return visibleAnnouncements;
    }

    public int getUnreadCount(Integer studentId, Integer classId) {
        Integer count = baseMapper.countUnreadForStudent(studentId, classId);
        return count != null ? count : 0;
    }

    @Transactional
    public boolean markAsRead(Integer announcementId, Integer studentId) {
        AnnouncementRead existing = announcementReadService.getByAnnouncementAndStudent(announcementId, studentId);
        if (existing != null) {
            return true;
        }

        User student = userMapper.selectById(studentId);
        AnnouncementRead read = new AnnouncementRead();
        read.setAnnouncementId(announcementId);
        read.setStudentId(studentId);
        read.setStudentName(student != null ? student.getUsername() : null);
        read.setReadAt(LocalDateTime.now());
        return announcementReadService.save(read);
    }

    @Transactional
    public boolean markAllAsRead(Integer studentId, Integer classId) {
        List<Announcement> visible = getVisibleForStudent(studentId, classId);
        Set<Integer> readIds = announcementReadService.getReadAnnouncementIds(studentId);
        User student = userMapper.selectById(studentId);

        for (Announcement a : visible) {
            if (!readIds.contains(a.getAnnouncementId())) {
                AnnouncementRead read = new AnnouncementRead();
                read.setAnnouncementId(a.getAnnouncementId());
                read.setStudentId(studentId);
                read.setStudentName(student != null ? student.getUsername() : null);
                read.setReadAt(LocalDateTime.now());
                announcementReadService.save(read);
            }
        }
        return true;
    }
}
