package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.AnnouncementClass;
import com.school.entity.Classes;
import com.school.mapper.AnnouncementClassMapper;
import com.school.mapper.ClassesMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class AnnouncementClassService extends ServiceImpl<AnnouncementClassMapper, AnnouncementClass> {

    @Autowired
    private ClassesMapper classesMapper;

    public List<AnnouncementClass> getByAnnouncementId(Integer announcementId) {
        return baseMapper.findByAnnouncementId(announcementId);
    }

    @Transactional
    public void removeByAnnouncementId(Integer announcementId) {
        baseMapper.deleteByAnnouncementId(announcementId);
    }

    @Transactional
    public void updateAnnouncementClasses(Integer announcementId, List<Integer> classIds) {
        baseMapper.deleteByAnnouncementId(announcementId);
        if (classIds != null && !classIds.isEmpty()) {
            List<AnnouncementClass> relations = new ArrayList<>();
            for (Integer classId : classIds) {
                Classes c = classesMapper.selectById(classId);
                AnnouncementClass ac = new AnnouncementClass();
                ac.setAnnouncementId(announcementId);
                ac.setClassId(classId);
                ac.setClassName(c != null ? c.getCname() : null);
                relations.add(ac);
            }
            saveBatch(relations);
        }
    }
}
