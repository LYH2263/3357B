package com.school.controller;

import com.school.entity.Exam;
import com.school.entity.ExamAttempt;
import com.school.entity.ExamQuestion;
import com.school.service.ExamAttemptService;
import com.school.service.ExamQuestionService;
import com.school.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam-student")
@CrossOrigin
public class ExamStudentController {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamAttemptService attemptService;

    @Autowired
    private ExamQuestionService questionService;

    @GetMapping("/available")
    public List<Exam> availableExams() {
        return examService.listPublishedForStudent();
    }

    @GetMapping("/my-attempts/{studentId}")
    public List<ExamAttempt> myAttempts(@PathVariable Integer studentId) {
        return attemptService.listByStudentId(studentId);
    }

    @GetMapping("/attempt-detail/{attemptId}")
    public ExamAttempt attemptDetail(@PathVariable Integer attemptId) {
        return attemptService.getAttemptDetail(attemptId);
    }

    @PostMapping("/start")
    public Map<String, Object> startExam(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer examId = (Integer) params.get("examId");
            Integer studentId = (Integer) params.get("studentId");
            String studentName = (String) params.get("studentName");
            String studentNo = (String) params.get("studentNo");

            ExamAttempt attempt = attemptService.startAttempt(examId, studentId, studentName, studentNo);
            List<ExamQuestion> questions = questionService.listByExamId(examId, false);

            result.put("success", true);
            result.put("attempt", attempt);
            result.put("questions", questions);
            result.put("exam", examService.getById(examId));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/submit")
    public Map<String, Object> submitExam(@RequestBody Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        try {
            Integer attemptId = (Integer) params.get("attemptId");
            boolean forceTimeout = params.get("forceTimeout") != null && (Boolean) params.get("forceTimeout");

            Map<String, List<String>> rawAnswers = (Map<String, List<String>>) params.get("answers");
            Map<Integer, List<String>> answersMap = new HashMap<>();
            if (rawAnswers != null) {
                for (Map.Entry<String, List<String>> entry : rawAnswers.entrySet()) {
                    answersMap.put(Integer.parseInt(entry.getKey()), entry.getValue());
                }
            }

            ExamAttempt attempt = attemptService.submitAttempt(attemptId, answersMap, forceTimeout);
            result.put("success", true);
            result.put("attempt", attempt);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/best-attempt/{examId}/{studentId}")
    public ExamAttempt bestAttempt(@PathVariable Integer examId, @PathVariable Integer studentId) {
        return attemptService.getBestAttempt(examId, studentId);
    }
}
