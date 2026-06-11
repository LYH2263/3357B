package com.school.controller;

import com.school.entity.Homework;
import com.school.entity.HomeworkSubmission;
import com.school.service.HomeworkService;
import com.school.service.HomeworkSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/homework")
@CrossOrigin
public class HomeworkController {

    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private HomeworkSubmissionService submissionService;

    private final String uploadPath = "/app/uploads/";

    @GetMapping("/teacher/list")
    public List<Homework> teacherList(@RequestParam Integer teacherId) {
        return homeworkService.listForTeacher(teacherId);
    }

    @GetMapping("/student/list")
    public List<Homework> studentList(@RequestParam Integer classId) {
        return homeworkService.listForStudent(classId);
    }

    @PostMapping("/create")
    public Map<String, Object> create(@RequestBody Homework homework) {
        return homeworkService.createHomework(homework);
    }

    @GetMapping("/detail/{id}")
    public Homework detail(@PathVariable Integer id) {
        return homeworkService.getDetail(id);
    }

    @DeleteMapping("/delete/{id}")
    public boolean delete(@PathVariable Integer id) {
        return homeworkService.deleteHomework(id);
    }

    @GetMapping("/statistics/{id}")
    public Map<String, Object> statistics(@PathVariable Integer id) {
        return homeworkService.getHomeworkStatistics(id);
    }

    @GetMapping("/submissions/{homeworkId}")
    public List<HomeworkSubmission> submissions(@PathVariable Integer homeworkId) {
        return submissionService.getSubmissionsWithStatus(homeworkId);
    }

    @GetMapping("/student/submission/{homeworkId}")
    public Map<String, Object> studentSubmission(@PathVariable Integer homeworkId, @RequestParam Integer studentId) {
        Map<String, Object> result = new HashMap<>();
        Homework homework = homeworkService.getDetail(homeworkId);
        HomeworkSubmission submission = submissionService.getByHomeworkAndStudent(homeworkId, studentId);

        if (submission == null) {
            submission = new HomeworkSubmission();
            submission.setHomeworkId(homeworkId);
            submission.setStudentId(studentId);
            submission.setStatus("NOT_SUBMITTED");
            submission.setSubmissionCount(0);
        }

        result.put("homework", homework);
        result.put("submission", submission);
        return result;
    }

    @PostMapping("/submit")
    public Map<String, Object> submit(@RequestBody Map<String, Object> params) {
        Integer homeworkId = (Integer) params.get("homeworkId");
        Integer studentId = (Integer) params.get("studentId");
        String fileUrl = (String) params.get("fileUrl");
        String fileName = (String) params.get("fileName");
        return submissionService.submitHomework(homeworkId, studentId, fileUrl, fileName);
    }

    @PostMapping("/grade")
    public Map<String, Object> grade(@RequestBody Map<String, Object> params) {
        Integer submissionId = (Integer) params.get("submissionId");
        BigDecimal score = new BigDecimal(params.get("score").toString());
        String comment = (String) params.get("comment");
        Integer teacherId = (Integer) params.get("teacherId");
        String teacherName = (String) params.get("teacherName");
        return submissionService.gradeHomework(submissionId, score, comment, teacherId, teacherName);
    }

    @PostMapping("/reject")
    public Map<String, Object> reject(@RequestBody Map<String, Object> params) {
        Integer submissionId = (Integer) params.get("submissionId");
        String comment = (String) params.get("comment");
        Integer teacherId = (Integer) params.get("teacherId");
        String teacherName = (String) params.get("teacherName");
        return submissionService.rejectHomework(submissionId, comment, teacherId, teacherName);
    }

    @GetMapping("/download/{submissionId}")
    public ResponseEntity<Resource> download(@PathVariable Integer submissionId) {
        HomeworkSubmission submission = submissionService.getById(submissionId);
        if (submission == null || submission.getFileUrl() == null) {
            return ResponseEntity.notFound().build();
        }

        String filePath = uploadPath + submission.getFileUrl().substring("/uploads/".length());
        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        String encodedFileName = URLEncoder.encode(submission.getFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .body(new FileSystemResource(file));
    }
}
