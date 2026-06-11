package com.school.controller;

import com.school.entity.CourseEnrollment;
import com.school.entity.ElectiveCourse;
import com.school.service.CourseEnrollmentService;
import com.school.service.ElectiveCourseService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/elective")
@CrossOrigin
public class ElectiveCourseController {

    @Autowired
    private ElectiveCourseService electiveCourseService;

    @Autowired
    private CourseEnrollmentService enrollmentService;

    @GetMapping("/teacher/list")
    public List<ElectiveCourse> teacherList(@RequestParam Integer teacherId) {
        return electiveCourseService.listForTeacher(teacherId);
    }

    @GetMapping("/student/list")
    public List<ElectiveCourse> studentList() {
        return electiveCourseService.listForStudent();
    }

    @GetMapping("/detail/{courseId}")
    public ElectiveCourse detail(@PathVariable Integer courseId) {
        return electiveCourseService.getById(courseId);
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody ElectiveCourse course) {
        Map<String, Object> result = new HashMap<>();
        boolean success = electiveCourseService.createCourse(course);
        result.put("success", success);
        result.put("message", success ? "课程发布成功" : "课程发布失败");
        result.put("courseId", course.getCourseId());
        return result;
    }

    @PostMapping("/update")
    public Map<String, Object> update(@RequestBody ElectiveCourse course) {
        Map<String, Object> result = new HashMap<>();
        boolean success = electiveCourseService.updateCourse(course);
        result.put("success", success);
        result.put("message", success ? "更新成功" : "更新失败");
        return result;
    }

    @PostMapping("/close/{courseId}")
    public Map<String, Object> close(@PathVariable Integer courseId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = electiveCourseService.closeCourse(courseId);
        result.put("success", success);
        result.put("message", success ? "已截止选课" : "操作失败");
        return result;
    }

    @DeleteMapping("/delete/{courseId}")
    public Map<String, Object> delete(@PathVariable Integer courseId) {
        Map<String, Object> result = new HashMap<>();
        boolean success = electiveCourseService.removeById(courseId);
        result.put("success", success);
        result.put("message", success ? "删除成功" : "删除失败");
        return result;
    }

    @GetMapping("/statistics/{courseId}")
    public Map<String, Object> statistics(@PathVariable Integer courseId) {
        return enrollmentService.getCourseStatistics(courseId);
    }

    @GetMapping("/enrollments/{courseId}")
    public List<CourseEnrollment> enrollments(@PathVariable Integer courseId) {
        return enrollmentService.listByCourseId(courseId);
    }

    @PostMapping("/enroll")
    public Map<String, Object> enroll(@RequestBody Map<String, Integer> params) {
        Integer courseId = params.get("courseId");
        Integer studentId = params.get("studentId");
        return enrollmentService.enrollCourse(courseId, studentId);
    }

    @PostMapping("/withdraw")
    public Map<String, Object> withdraw(@RequestBody Map<String, Integer> params) {
        Integer courseId = params.get("courseId");
        Integer studentId = params.get("studentId");
        return enrollmentService.withdrawCourse(courseId, studentId);
    }

    @GetMapping("/my-courses")
    public List<ElectiveCourse> myCourses(@RequestParam Integer studentId) {
        List<CourseEnrollment> enrollments = enrollmentService.listByStudentId(studentId);
        List<Integer> courseIds = enrollments.stream()
                .map(CourseEnrollment::getCourseId)
                .toList();
        if (courseIds.isEmpty()) {
            return List.of();
        }
        return electiveCourseService.listByIds(courseIds);
    }

    @GetMapping("/my-enrollments")
    public List<CourseEnrollment> myEnrollments(@RequestParam Integer studentId) {
        return enrollmentService.listByStudentId(studentId);
    }

    @GetMapping("/check-enrolled")
    public Map<String, Object> checkEnrolled(@RequestParam Integer courseId, @RequestParam Integer studentId) {
        Map<String, Object> result = new HashMap<>();
        boolean enrolled = enrollmentService.isEnrolled(courseId, studentId);
        result.put("enrolled", enrolled);
        return result;
    }

    @GetMapping("/export/{courseId}")
    public void exportEnrollments(@PathVariable Integer courseId, HttpServletResponse response) throws IOException {
        ElectiveCourse course = electiveCourseService.getById(courseId);
        if (course == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        List<CourseEnrollment> enrollments = enrollmentService.listByCourseId(courseId);

        String fileName = course.getCourseName() + "_选课名单.csv";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replace("+", "%20");

        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);

        try (OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write('\uFEFF');
            writer.write("序号,学号,姓名,班级,选课时间\n");

            int index = 1;
            for (CourseEnrollment e : enrollments) {
                writer.write(index++ + ",");
                writer.write((e.getStudentNo() != null ? e.getStudentNo() : "") + ",");
                writer.write((e.getStudentName() != null ? e.getStudentName() : "") + ",");
                writer.write((e.getClassName() != null ? e.getClassName() : "") + ",");
                writer.write((e.getEnrollTime() != null ? e.getEnrollTime().toString() : "") + "\n");
            }
            writer.flush();
        }
    }
}
