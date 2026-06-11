package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.CourseEnrollment;
import com.school.entity.ElectiveCourse;
import com.school.entity.User;
import com.school.mapper.CourseEnrollmentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CourseEnrollmentService extends ServiceImpl<CourseEnrollmentMapper, CourseEnrollment> {

    @Autowired
    private ElectiveCourseService electiveCourseService;

    @Autowired
    private UserService userService;

    public List<CourseEnrollment> listByCourseId(Integer courseId) {
        return this.lambdaQuery()
                .eq(CourseEnrollment::getCourseId, courseId)
                .eq(CourseEnrollment::getStatus, "ENROLLED")
                .orderByAsc(CourseEnrollment::getEnrollTime)
                .list();
    }

    public List<CourseEnrollment> listByStudentId(Integer studentId) {
        return this.lambdaQuery()
                .eq(CourseEnrollment::getStudentId, studentId)
                .eq(CourseEnrollment::getStatus, "ENROLLED")
                .orderByDesc(CourseEnrollment::getEnrollTime)
                .list();
    }

    public CourseEnrollment getByCourseAndStudent(Integer courseId, Integer studentId) {
        return this.lambdaQuery()
                .eq(CourseEnrollment::getCourseId, courseId)
                .eq(CourseEnrollment::getStudentId, studentId)
                .one();
    }

    public boolean isEnrolled(Integer courseId, Integer studentId) {
        CourseEnrollment enrollment = getByCourseAndStudent(courseId, studentId);
        return enrollment != null && "ENROLLED".equals(enrollment.getStatus());
    }

    @Transactional
    public Map<String, Object> enrollCourse(Integer courseId, Integer studentId) {
        Map<String, Object> result = new HashMap<>();

        ElectiveCourse course = electiveCourseService.getById(courseId);
        if (course == null) {
            result.put("success", false);
            result.put("message", "课程不存在");
            return result;
        }

        if (!electiveCourseService.isEnrollmentOpen(course)) {
            result.put("success", false);
            result.put("message", "不在选课时间范围内");
            return result;
        }

        CourseEnrollment existing = getByCourseAndStudent(courseId, studentId);
        if (existing != null && "ENROLLED".equals(existing.getStatus())) {
            result.put("success", true);
            result.put("message", "您已选过该课程");
            result.put("alreadyEnrolled", true);
            result.put("enrollment", existing);
            return result;
        }

        if (existing != null && "WITHDRAWN".equals(existing.getStatus())) {
            boolean incremented = electiveCourseService.incrementEnrolledCount(courseId);
            if (!incremented) {
                result.put("success", false);
                result.put("message", "选课失败，名额已满");
                return result;
            }
            existing.setStatus("ENROLLED");
            existing.setEnrollTime(LocalDateTime.now());
            existing.setUpdatedAt(LocalDateTime.now());
            this.updateById(existing);
            result.put("success", true);
            result.put("message", "选课成功");
            result.put("enrollment", existing);
            return result;
        }

        boolean incremented = electiveCourseService.incrementEnrolledCount(courseId);
        if (!incremented) {
            result.put("success", false);
            result.put("message", "选课失败，名额已满");
            return result;
        }

        try {
            User student = userService.getById(studentId);
            CourseEnrollment enrollment = new CourseEnrollment();
            enrollment.setCourseId(courseId);
            enrollment.setStudentId(studentId);
            if (student != null) {
                enrollment.setStudentName(student.getUsername());
                enrollment.setStudentNo(student.getUserno());
                enrollment.setClassName(student.getClassname());
            }
            enrollment.setEnrollTime(LocalDateTime.now());
            enrollment.setStatus("ENROLLED");
            enrollment.setCreatedAt(LocalDateTime.now());
            enrollment.setUpdatedAt(LocalDateTime.now());
            this.save(enrollment);

            result.put("success", true);
            result.put("message", "选课成功");
            result.put("enrollment", enrollment);
        } catch (Exception e) {
            electiveCourseService.decrementEnrolledCount(courseId);
            result.put("success", false);
            result.put("message", "选课失败，请稍后重试");
        }

        return result;
    }

    @Transactional
    public Map<String, Object> withdrawCourse(Integer courseId, Integer studentId) {
        Map<String, Object> result = new HashMap<>();

        ElectiveCourse course = electiveCourseService.getById(courseId);
        if (course == null) {
            result.put("success", false);
            result.put("message", "课程不存在");
            return result;
        }

        if (!electiveCourseService.isEnrollmentOpen(course)) {
            result.put("success", false);
            result.put("message", "选课已截止，无法退选");
            return result;
        }

        CourseEnrollment enrollment = getByCourseAndStudent(courseId, studentId);
        if (enrollment == null || !"ENROLLED".equals(enrollment.getStatus())) {
            result.put("success", false);
            result.put("message", "您未选修该课程");
            return result;
        }

        enrollment.setStatus("WITHDRAWN");
        enrollment.setUpdatedAt(LocalDateTime.now());
        this.updateById(enrollment);

        electiveCourseService.decrementEnrolledCount(courseId);

        result.put("success", true);
        result.put("message", "退选成功，名额已释放");
        return result;
    }

    public Map<String, Object> getCourseStatistics(Integer courseId) {
        Map<String, Object> result = new HashMap<>();
        ElectiveCourse course = electiveCourseService.getById(courseId);
        if (course == null) {
            return result;
        }

        List<CourseEnrollment> enrollments = listByCourseId(courseId);
        int enrolledCount = enrollments.size();
        int remaining = course.getCapacity() - enrolledCount;

        result.put("course", course);
        result.put("enrolledCount", enrolledCount);
        result.put("remainingCount", remaining);
        result.put("capacity", course.getCapacity());
        result.put("enrollments", enrollments);
        return result;
    }
}
