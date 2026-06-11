package com.school.controller;

import com.school.entity.Exam;
import com.school.entity.ExamAttempt;
import com.school.service.ExamAttemptService;
import com.school.service.ExamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/exam")
@CrossOrigin
public class ExamController {

    @Autowired
    private ExamService examService;

    @Autowired
    private ExamAttemptService attemptService;

    @GetMapping("/list")
    public List<Exam> list() {
        return examService.list();
    }

    @GetMapping("/detail/{id}")
    public Exam detail(@PathVariable Integer id) {
        return examService.getDetail(id, true);
    }

    @PostMapping("/save")
    public Map<String, Object> save(@RequestBody Exam exam) {
        Map<String, Object> result = new HashMap<>();
        try {
            examService.saveExamWithQuestions(exam);
            result.put("success", true);
            result.put("examId", exam.getExamId());
            result.put("message", "保存成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @DeleteMapping("/delete/{id}")
    public boolean delete(@PathVariable Integer id) {
        return examService.deleteExam(id);
    }

    @PostMapping("/publish/{id}")
    public Map<String, Object> publish(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            examService.publishExam(id);
            result.put("success", true);
            result.put("message", "发布成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @PostMapping("/close/{id}")
    public boolean close(@PathVariable Integer id) {
        return examService.closeExam(id);
    }

    @PostMapping("/withdraw/{id}")
    public Map<String, Object> withdraw(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        Exam exam = examService.getById(id);
        if (exam == null) {
            result.put("success", false);
            result.put("message", "试卷不存在");
            return result;
        }
        exam.setStatus("DRAFT");
        examService.updateById(exam);
        result.put("success", true);
        result.put("message", "已撤回草稿");
        return result;
    }

    @GetMapping("/statistics/{id}")
    public Map<String, Object> statistics(@PathVariable Integer id) {
        return examService.getExamStatistics(id);
    }

    @GetMapping("/attempts/{examId}")
    public List<ExamAttempt> attempts(@PathVariable Integer examId) {
        return attemptService.listByExamId(examId);
    }
}
