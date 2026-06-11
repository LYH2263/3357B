package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.entity.*;
import com.school.mapper.ExamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamService extends ServiceImpl<ExamMapper, Exam> {

    @Autowired
    private ExamQuestionService questionService;
    @Autowired
    private ExamOptionService optionService;
    @Autowired
    private ExamAttemptService attemptService;

    public Exam getDetail(Integer examId, boolean withCorrectAnswer) {
        Exam exam = this.getById(examId);
        if (exam != null) {
            exam.setQuestions(questionService.listByExamId(examId, withCorrectAnswer));
        }
        return exam;
    }

    public List<Exam> listPublishedForStudent() {
        return this.lambdaQuery()
                .eq(Exam::getStatus, "PUBLISHED")
                .list();
    }

    public boolean publishExam(Integer examId) {
        Exam exam = this.getById(examId);
        if (exam == null) return false;
        if ("PUBLISHED".equals(exam.getStatus()) || "CLOSED".equals(exam.getStatus())) {
            return true;
        }
        List<ExamQuestion> questions = questionService.listByExamId(examId, true);
        if (questions.isEmpty()) {
            throw new RuntimeException("试卷没有题目，无法发布");
        }
        for (ExamQuestion q : questions) {
            if (q.getCorrectAnswerLabels() == null || q.getCorrectAnswerLabels().isEmpty()) {
                throw new RuntimeException("题目【" + q.getQuestionText().substring(0, Math.min(10, q.getQuestionText().length())) + "...】没有正确答案，无法发布");
            }
        }
        BigDecimal totalScore = questions.stream()
                .map(ExamQuestion::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        exam.setTotalScore(totalScore);
        exam.setStatus("PUBLISHED");
        exam.setUpdatedAt(LocalDateTime.now());
        return this.updateById(exam);
    }

    public boolean closeExam(Integer examId) {
        return this.lambdaUpdate()
                .eq(Exam::getExamId, examId)
                .set(Exam::getStatus, "CLOSED")
                .set(Exam::getUpdatedAt, LocalDateTime.now())
                .update();
    }

    @Transactional
    public boolean saveExamWithQuestions(Exam exam) {
        boolean isNew = exam.getExamId() == null;
        if (!isNew && "PUBLISHED".equals(exam.getStatus())) {
            Exam existing = this.getById(exam.getExamId());
            if (!Objects.equals(existing.getCourseId(), exam.getCourseId()) ||
                    !Objects.equals(existing.getDurationMinutes(), exam.getDurationMinutes()) ||
                    !Objects.equals(existing.getPassScore(), exam.getPassScore()) ||
                    !Objects.equals(existing.getMaxAttempts(), exam.getMaxAttempts()) ||
                    !Objects.equals(existing.getScoringRule(), exam.getScoringRule())) {
                throw new RuntimeException("已发布试卷仅允许修改标题、描述和时间，其他字段修改需先撤回草稿");
            }
        }

        this.saveOrUpdate(exam);

        if (exam.getQuestions() != null) {
            if (!isNew) {
                questionService.deleteByExamId(exam.getExamId());
            }

            int sortOrder = 0;
            for (ExamQuestion q : exam.getQuestions()) {
                q.setExamId(exam.getExamId());
                q.setSortOrder(sortOrder++);
                questionService.save(q);

                if (q.getOptions() != null) {
                    int optOrder = 0;
                    for (ExamOption opt : q.getOptions()) {
                        opt.setQuestionId(q.getQuestionId());
                        opt.setSortOrder(optOrder++);
                        optionService.save(opt);
                    }
                }
            }
        }
        return true;
    }

    @Transactional
    public boolean deleteExam(Integer examId) {
        questionService.deleteByExamId(examId);
        attemptService.deleteByExamId(examId);
        return this.removeById(examId);
    }

    public Map<String, Object> getExamStatistics(Integer examId) {
        Map<String, Object> result = new HashMap<>();
        Exam exam = this.getById(examId);
        result.put("exam", exam);

        List<ExamAttempt> submitted = attemptService.lambdaQuery()
                .eq(ExamAttempt::getExamId, examId)
                .eq(ExamAttempt::getIsSubmitted, 1)
                .list();

        int totalSubmissions = submitted.size();
        result.put("totalSubmissions", totalSubmissions);

        if (totalSubmissions > 0) {
            BigDecimal avgScore = submitted.stream()
                    .map(ExamAttempt::getScore)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(totalSubmissions), 2, BigDecimal.ROUND_HALF_UP);
            result.put("avgScore", avgScore);

            long passCount = submitted.stream()
                    .filter(a -> a.getScore() != null && a.getScore().compareTo(exam.getPassScore()) >= 0)
                    .count();
            result.put("passCount", passCount);
            result.put("passRate", BigDecimal.valueOf(passCount)
                    .divide(BigDecimal.valueOf(totalSubmissions), 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100)));
        } else {
            result.put("avgScore", BigDecimal.ZERO);
            result.put("passCount", 0);
            result.put("passRate", BigDecimal.ZERO);
        }

        List<ExamQuestion> questions = questionService.listByExamId(examId, true);
        List<Map<String, Object>> questionStats = new ArrayList<>();
        for (ExamQuestion q : questions) {
            Map<String, Object> qs = new HashMap<>();
            qs.put("questionId", q.getQuestionId());
            qs.put("questionText", q.getQuestionText());
            qs.put("score", q.getScore());
            qs.put("questionType", q.getQuestionType());

            if (totalSubmissions > 0) {
                long correctCount = attemptService.countCorrectAnswers(examId, q.getQuestionId());
                qs.put("correctCount", correctCount);
                qs.put("correctRate", BigDecimal.valueOf(correctCount)
                        .divide(BigDecimal.valueOf(totalSubmissions), 4, BigDecimal.ROUND_HALF_UP)
                        .multiply(BigDecimal.valueOf(100)));
            } else {
                qs.put("correctCount", 0);
                qs.put("correctRate", BigDecimal.ZERO);
            }
            questionStats.add(qs);
        }
        result.put("questionStats", questionStats);
        return result;
    }
}
