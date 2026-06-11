package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.AnnouncementRead;
import com.school.mapper.AnnouncementReadMapper;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AnnouncementReadService extends ServiceImpl<AnnouncementReadMapper, AnnouncementRead> {

    public Set<Integer> getReadAnnouncementIds(Integer studentId) {
        List<AnnouncementRead> reads = baseMapper.findByStudentId(studentId);
        Set<Integer> ids = new HashSet<>();
        for (AnnouncementRead r : reads) {
            ids.add(r.getAnnouncementId());
        }
        return ids;
    }

    public AnnouncementRead getByAnnouncementAndStudent(Integer announcementId, Integer studentId) {
        return baseMapper.findByAnnouncementAndStudent(announcementId, studentId);
    }
}
