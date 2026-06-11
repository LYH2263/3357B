package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Course;
import com.school.entity.LearningProgress;
import com.school.entity.User;
import com.school.mapper.CourseMapper;
import com.school.mapper.LearningProgressMapper;
import com.school.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LearningProgressService extends ServiceImpl<LearningProgressMapper, LearningProgress> {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseMapper courseMapper;

    public Map<String, Object> getPersonalProgress(Integer studentId) {
        int totalCourses = baseMapper.getTotalCourseCount();
        int completedCount = baseMapper.getCompletedCount(studentId);
        double percentage = totalCourses > 0 ? Math.round(completedCount * 10000.0 / totalCourses) / 100.0 : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalCourses", totalCourses);
        result.put("completedCount", completedCount);
        result.put("uncompletedCount", totalCourses - completedCount);
        result.put("percentage", percentage);
        return result;
    }

    public List<Map<String, Object>> getAllCoursesWithProgress(Integer studentId) {
        return baseMapper.getAllCoursesWithProgress(studentId);
    }

    public List<Map<String, Object>> getCompletedTimeline(Integer studentId) {
        return baseMapper.getCompletedTimeline(studentId);
    }

    public List<Map<String, Object>> getUncompletedCourses(Integer studentId) {
        List<Map<String, Object>> all = baseMapper.getAllCoursesWithProgress(studentId);
        return all.stream()
                .filter(m -> ((Number) m.get("isCompleted")).intValue() == 0)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean markCompleted(Integer studentId, Integer courseId) {
        QueryWrapper<LearningProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId).eq("course_id", courseId);
        LearningProgress existing = getOne(wrapper);

        if (existing != null) {
            if (existing.getIsCompleted() == 1) {
                return true;
            }
            existing.setIsCompleted(1);
            existing.setCompletedAt(LocalDateTime.now());
            return updateById(existing);
        }

        User student = userMapper.selectById(studentId);
        Course course = courseMapper.selectById(courseId);

        LearningProgress progress = new LearningProgress();
        progress.setStudentId(studentId);
        progress.setStudentName(student != null ? student.getUsername() : null);
        progress.setCourseId(courseId);
        progress.setCourseTitle(course != null ? course.getCtitle() : null);
        progress.setIsCompleted(1);
        progress.setCompletedAt(LocalDateTime.now());
        return save(progress);
    }

    @Transactional
    public boolean markUncompleted(Integer studentId, Integer courseId) {
        QueryWrapper<LearningProgress> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId).eq("course_id", courseId);
        LearningProgress existing = getOne(wrapper);

        if (existing == null) {
            return true;
        }

        if (existing.getIsCompleted() == 0) {
            return true;
        }

        existing.setIsCompleted(0);
        existing.setCompletedAt(null);
        return updateById(existing);
    }

    public List<Map<String, Object>> getClassStudentProgress(Integer classId) {
        int totalCourses = baseMapper.getTotalCourseCount();
        List<Map<String, Object>> students = baseMapper.getClassStudentProgress(classId);
        for (Map<String, Object> s : students) {
            int completed = ((Number) s.get("completedCount")).intValue();
            double percentage = totalCourses > 0 ? Math.round(completed * 10000.0 / totalCourses) / 100.0 : 0.0;
            s.put("totalCourses", totalCourses);
            s.put("percentage", percentage);
        }
        return students;
    }

    public Map<String, Object> getClassAverageProgress(Integer classId) {
        int totalCourses = baseMapper.getTotalCourseCount();
        List<Map<String, Object>> students = baseMapper.getClassStudentProgress(classId);
        int totalStudents = students.size();
        int totalCompleted = students.stream()
                .mapToInt(s -> ((Number) s.get("completedCount")).intValue())
                .sum();
        double avgPercentage = (totalStudents > 0 && totalCourses > 0)
                ? Math.round(totalCompleted * 10000.0 / (totalStudents * totalCourses)) / 100.0
                : 0.0;

        Map<String, Object> result = new HashMap<>();
        result.put("totalStudents", totalStudents);
        result.put("totalCourses", totalCourses);
        result.put("totalCompleted", totalCompleted);
        result.put("averagePercentage", avgPercentage);
        return result;
    }

    public List<Map<String, Object>> getCourseHeatRanking() {
        return baseMapper.getCourseHeatRanking();
    }

    public List<Map<String, Object>> getAllClassAverageProgress() {
        int totalCourses = baseMapper.getTotalCourseCount();
        List<Map<String, Object>> classes = baseMapper.getClassAverageProgress();
        for (Map<String, Object> c : classes) {
            int totalStudents = ((Number) c.get("totalStudents")).intValue();
            int totalCompleted = ((Number) c.get("totalCompleted")).intValue();
            double avgPercentage = (totalStudents > 0 && totalCourses > 0)
                    ? Math.round(totalCompleted * 10000.0 / (totalStudents * totalCourses)) / 100.0
                    : 0.0;
            c.put("totalCourses", totalCourses);
            c.put("averagePercentage", avgPercentage);
        }
        return classes;
    }

    @Transactional
    public void initProgressForNewCourse(Integer courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) return;

        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.isNotNull("class_id");
        List<User> students = userMapper.selectList(userWrapper);

        for (User student : students) {
            QueryWrapper<LearningProgress> wrapper = new QueryWrapper<>();
            wrapper.eq("student_id", student.getUid()).eq("course_id", courseId);
            if (getOne(wrapper) == null) {
                LearningProgress progress = new LearningProgress();
                progress.setStudentId(student.getUid());
                progress.setStudentName(student.getUsername());
                progress.setCourseId(courseId);
                progress.setCourseTitle(course.getCtitle());
                progress.setIsCompleted(0);
                save(progress);
            }
        }
    }
}
