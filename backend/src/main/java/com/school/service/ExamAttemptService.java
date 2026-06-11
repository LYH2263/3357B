package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.entity.*;
import com.school.mapper.ExamAttemptMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExamAttemptService extends ServiceImpl<ExamAttemptMapper, ExamAttempt> {

    @Autowired
    private ExamService examService;
    @Autowired
    private ExamQuestionService questionService;
    @Autowired
    private ExamAnswerService answerService;
    @Autowired
    private ExamAnswerService examAnswerService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public ExamAttempt startAttempt(Integer examId, Integer studentId, String studentName, String studentNo) {
        Exam exam = examService.getById(examId);
        if (exam == null) {
            throw new RuntimeException("试卷不存在");
        }
        if (!"PUBLISHED".equals(exam.getStatus())) {
            throw new RuntimeException("试卷未发布或已截止");
        }
        LocalDateTime now = LocalDateTime.now();
        if (exam.getEndTime() != null && now.isAfter(exam.getEndTime())) {
            examService.closeExam(examId);
            throw new RuntimeException("试卷已截止");
        }
        if (exam.getStartTime() != null && now.isBefore(exam.getStartTime())) {
            throw new RuntimeException("试卷尚未开始");
        }

        long submittedCount = this.lambdaQuery()
                .eq(ExamAttempt::getExamId, examId)
                .eq(ExamAttempt::getStudentId, studentId)
                .eq(ExamAttempt::getIsSubmitted, 1)
                .count();
        if (submittedCount >= exam.getMaxAttempts()) {
            throw new RuntimeException("已达到最大作答次数");
        }

        long unsubmittedCount = this.lambdaQuery()
                .eq(ExamAttempt::getExamId, examId)
                .eq(ExamAttempt::getStudentId, studentId)
                .eq(ExamAttempt::getIsSubmitted, 0)
                .count();
        if (unsubmittedCount > 0) {
            ExamAttempt existing = this.lambdaQuery()
                    .eq(ExamAttempt::getExamId, examId)
                    .eq(ExamAttempt::getStudentId, studentId)
                    .eq(ExamAttempt::getIsSubmitted, 0)
                    .last("LIMIT 1")
                    .one();
            Duration elapsed = Duration.between(existing.getStartTime(), now);
            if (elapsed.toMinutes() < exam.getDurationMinutes()) {
                return existing;
            } else {
                submitAttempt(existing.getAttemptId(), null, true);
            }
        }

        ExamAttempt attempt = new ExamAttempt();
        attempt.setExamId(examId);
        attempt.setStudentId(studentId);
        attempt.setStudentName(studentName);
        attempt.setStudentNo(studentNo);
        attempt.setAttemptNo((int) submittedCount + 1);
        attempt.setTotalScore(exam.getTotalScore());
        attempt.setIsSubmitted(0);
        attempt.setIsTimeout(0);
        attempt.setStartTime(LocalDateTime.now());
        this.save(attempt);
        return attempt;
    }

    @Transactional
    public ExamAttempt submitAttempt(Integer attemptId, Map<Integer, List<String>> answersMap, boolean forceTimeout) {
        ExamAttempt attempt = this.getById(attemptId);
        if (attempt == null) {
            throw new RuntimeException("作答记录不存在");
        }
        if (attempt.getIsSubmitted() == 1) {
            return attempt;
        }

        Exam exam = examService.getById(attempt.getExamId());
        LocalDateTime now = LocalDateTime.now();
        Duration elapsed = Duration.between(attempt.getStartTime(), now);
        boolean isTimeout = forceTimeout || elapsed.toMinutes() >= exam.getDurationMinutes();

        List<ExamQuestion> questions = questionService.listByExamId(attempt.getExamId(), true);
        BigDecimal totalScore = BigDecimal.ZERO;
        ObjectMapper om = new ObjectMapper();

        for (ExamQuestion q : questions) {
            List<String> studentAnswers = answersMap != null ? answersMap.getOrDefault(q.getQuestionId(), new ArrayList<>()) : new ArrayList<>();
            Collections.sort(studentAnswers);
            List<String> correctAnswers = q.getCorrectAnswerLabels() != null ? q.getCorrectAnswerLabels() : new ArrayList<>();

            ExamAnswer answer = new ExamAnswer();
            answer.setAttemptId(attemptId);
            answer.setQuestionId(q.getQuestionId());
            answer.setQuestionText(q.getQuestionText());
            answer.setQuestionType(q.getQuestionType());
            answer.setScore(q.getScore());
            answer.setStudentAnswers(String.join(",", studentAnswers));
            answer.setCorrectAnswers(String.join(",", correctAnswers));
            answer.setAnalysis(q.getAnalysis());

            try {
                answer.setOptionSnapshot(om.writeValueAsString(q.getOptions()));
            } catch (Exception e) {
                answer.setOptionSnapshot("[]");
            }

            BigDecimal actualScore = calculateScore(q, studentAnswers, correctAnswers, exam.getScoringRule());
            answer.setActualScore(actualScore);
            answer.setIsCorrect(actualScore.compareTo(q.getScore()) == 0 ? 1 : (actualScore.compareTo(BigDecimal.ZERO) > 0 ? 0 : 0));
            if (actualScore.compareTo(q.getScore()) == 0) {
                answer.setIsCorrect(1);
            } else {
                answer.setIsCorrect(0);
            }

            totalScore = totalScore.add(actualScore);
            answerService.save(answer);
        }

        attempt.setScore(totalScore);
        attempt.setTimeSpentSeconds((int) Math.min(elapsed.getSeconds(), (long) exam.getDurationMinutes() * 60));
        attempt.setIsSubmitted(1);
        attempt.setIsTimeout(isTimeout ? 1 : 0);
        attempt.setSubmitTime(now);
        this.updateById(attempt);

        return attempt;
    }

    private BigDecimal calculateScore(ExamQuestion question, List<String> studentAnswers, List<String> correctAnswers, String scoringRule) {
        if ("SINGLE".equals(question.getQuestionType())) {
            if (studentAnswers.size() == 1 && correctAnswers.contains(studentAnswers.get(0))) {
                return question.getScore();
            }
            return BigDecimal.ZERO;
        }

        if ("MULTIPLE".equals(question.getQuestionType())) {
            if ("ALL_OR_NOTHING".equals(scoringRule)) {
                if (studentAnswers.equals(correctAnswers)) {
                    return question.getScore();
                }
                return BigDecimal.ZERO;
            } else if ("PROPORTIONAL".equals(scoringRule)) {
                if (studentAnswers.isEmpty()) return BigDecimal.ZERO;
                Set<String> correctSet = new HashSet<>(correctAnswers);
                Set<String> studentSet = new HashSet<>(studentAnswers);
                Set<String> wrongSet = new HashSet<>(studentSet);
                wrongSet.removeAll(correctSet);

                if (!wrongSet.isEmpty()) {
                    return BigDecimal.ZERO;
                }

                long correctCount = studentSet.stream().filter(correctSet::contains).count();
                double ratio = (double) correctCount / correctAnswers.size();
                return question.getScore().multiply(BigDecimal.valueOf(ratio)).setScale(2, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO;
    }

    public long countCorrectAnswers(Integer examId, Integer questionId) {
        List<ExamAttempt> attempts = this.lambdaQuery()
                .eq(ExamAttempt::getExamId, examId)
                .eq(ExamAttempt::getIsSubmitted, 1)
                .list();
        if (attempts.isEmpty()) return 0;

        List<Integer> attemptIds = attempts.stream().map(ExamAttempt::getAttemptId).collect(Collectors.toList());
        return answerService.lambdaQuery()
                .in(ExamAnswer::getAttemptId, attemptIds)
                .eq(ExamAnswer::getQuestionId, questionId)
                .eq(ExamAnswer::getIsCorrect, 1)
                .count();
    }

    public ExamAttempt getAttemptDetail(Integer attemptId) {
        ExamAttempt attempt = this.getById(attemptId);
        if (attempt != null) {
            attempt.setAnswers(answerService.listByAttemptId(attemptId));
            attempt.setExam(examService.getById(attempt.getExamId()));
        }
        return attempt;
    }

    public List<ExamAttempt> listByExamId(Integer examId) {
        return this.lambdaQuery()
                .eq(ExamAttempt::getExamId, examId)
                .eq(ExamAttempt::getIsSubmitted, 1)
                .orderByDesc(ExamAttempt::getSubmitTime)
                .list();
    }

    public List<ExamAttempt> listByStudentId(Integer studentId) {
        return this.lambdaQuery()
                .eq(ExamAttempt::getStudentId, studentId)
                .eq(ExamAttempt::getIsSubmitted, 1)
                .orderByDesc(ExamAttempt::getSubmitTime)
                .list();
    }

    public ExamAttempt getBestAttempt(Integer examId, Integer studentId) {
        List<ExamAttempt> attempts = this.lambdaQuery()
                .eq(ExamAttempt::getExamId, examId)
                .eq(ExamAttempt::getStudentId, studentId)
                .eq(ExamAttempt::getIsSubmitted, 1)
                .orderByDesc(ExamAttempt::getScore)
                .list();
        return attempts.isEmpty() ? null : attempts.get(0);
    }

    @Transactional
    public boolean deleteByExamId(Integer examId) {
        List<ExamAttempt> attempts = this.lambdaQuery()
                .eq(ExamAttempt::getExamId, examId)
                .list();
        for (ExamAttempt a : attempts) {
            answerService.deleteByAttemptId(a.getAttemptId());
        }
        return this.lambdaUpdate()
                .eq(ExamAttempt::getExamId, examId)
                .remove();
    }
}
