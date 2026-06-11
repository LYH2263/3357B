package com.school.controller;

import com.school.service.LearningProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin
public class LearningProgressController {

    @Autowired
    private LearningProgressService progressService;

    @GetMapping("/personal")
    public Map<String, Object> personalProgress(@RequestParam Integer studentId) {
        Map<String, Object> progress = progressService.getPersonalProgress(studentId);
        Map<String, Object> result = new HashMap<>();
        result.put("progress", progress);
        return result;
    }

    @GetMapping("/courses")
    public List<Map<String, Object>> coursesWithProgress(@RequestParam Integer studentId) {
        return progressService.getAllCoursesWithProgress(studentId);
    }

    @GetMapping("/completed-timeline")
    public List<Map<String, Object>> completedTimeline(@RequestParam Integer studentId) {
        return progressService.getCompletedTimeline(studentId);
    }

    @GetMapping("/uncompleted")
    public List<Map<String, Object>> uncompletedCourses(@RequestParam Integer studentId) {
        return progressService.getUncompletedCourses(studentId);
    }

    @PostMapping("/mark-completed")
    public Map<String, Object> markCompleted(@RequestBody Map<String, Integer> params) {
        Integer studentId = params.get("studentId");
        Integer courseId = params.get("courseId");
        boolean success = progressService.markCompleted(studentId, courseId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return result;
    }

    @PostMapping("/mark-uncompleted")
    public Map<String, Object> markUncompleted(@RequestBody Map<String, Integer> params) {
        Integer studentId = params.get("studentId");
        Integer courseId = params.get("courseId");
        boolean success = progressService.markUncompleted(studentId, courseId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        return result;
    }

    @GetMapping("/class-students")
    public List<Map<String, Object>> classStudentProgress(@RequestParam Integer classId) {
        return progressService.getClassStudentProgress(classId);
    }

    @GetMapping("/class-average")
    public Map<String, Object> classAverageProgress(@RequestParam Integer classId) {
        return progressService.getClassAverageProgress(classId);
    }

    @GetMapping("/course-heat")
    public List<Map<String, Object>> courseHeatRanking() {
        return progressService.getCourseHeatRanking();
    }

    @GetMapping("/all-class-average")
    public List<Map<String, Object>> allClassAverageProgress() {
        return progressService.getAllClassAverageProgress();
    }
}
