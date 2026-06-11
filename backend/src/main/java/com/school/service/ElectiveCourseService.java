package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.ElectiveCourse;
import com.school.mapper.ElectiveCourseMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ElectiveCourseService extends ServiceImpl<ElectiveCourseMapper, ElectiveCourse> {

    public List<ElectiveCourse> listForTeacher(Integer teacherId) {
        return this.lambdaQuery()
                .eq(ElectiveCourse::getTeacherId, teacherId)
                .orderByDesc(ElectiveCourse::getCreatedAt)
                .list();
    }

    public List<ElectiveCourse> listForStudent() {
        return this.lambdaQuery()
                .eq(ElectiveCourse::getStatus, "OPEN")
                .orderByDesc(ElectiveCourse::getCreatedAt)
                .list();
    }

    public boolean incrementEnrolledCount(Integer courseId) {
        return baseMapper.incrementEnrolledCount(courseId) > 0;
    }

    public boolean decrementEnrolledCount(Integer courseId) {
        return baseMapper.decrementEnrolledCount(courseId) > 0;
    }

    public boolean isEnrollmentOpen(ElectiveCourse course) {
        if (course == null || !"OPEN".equals(course.getStatus())) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(course.getEnrollStartTime()) && !now.isAfter(course.getEnrollEndTime());
    }

    public boolean createCourse(ElectiveCourse course) {
        course.setEnrolledCount(0);
        course.setStatus("OPEN");
        course.setCreatedAt(LocalDateTime.now());
        course.setUpdatedAt(LocalDateTime.now());
        return this.save(course);
    }

    public boolean updateCourse(ElectiveCourse course) {
        course.setUpdatedAt(LocalDateTime.now());
        return this.updateById(course);
    }

    public boolean closeCourse(Integer courseId) {
        ElectiveCourse course = new ElectiveCourse();
        course.setCourseId(courseId);
        course.setStatus("CLOSED");
        course.setUpdatedAt(LocalDateTime.now());
        return this.updateById(course);
    }
}
