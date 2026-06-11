package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Homework;
import com.school.entity.HomeworkSubmission;
import com.school.entity.User;
import com.school.mapper.HomeworkSubmissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HomeworkSubmissionService extends ServiceImpl<HomeworkSubmissionMapper, HomeworkSubmission> {

    @Autowired
    private HomeworkService homeworkService;

    @Autowired
    private UserService userService;

    private final String uploadPath = "/app/uploads/";

    public List<HomeworkSubmission> listByHomeworkId(Integer homeworkId) {
        return this.lambdaQuery()
                .eq(HomeworkSubmission::getHomeworkId, homeworkId)
                .list();
    }

    public List<HomeworkSubmission> listByStudentId(Integer studentId) {
        return this.lambdaQuery()
                .eq(HomeworkSubmission::getStudentId, studentId)
                .list();
    }

    public HomeworkSubmission getByHomeworkAndStudent(Integer homeworkId, Integer studentId) {
        return this.lambdaQuery()
                .eq(HomeworkSubmission::getHomeworkId, homeworkId)
                .eq(HomeworkSubmission::getStudentId, studentId)
                .one();
    }

    @Transactional
    public Map<String, Object> submitHomework(Integer homeworkId, Integer studentId, String fileUrl, String fileName) {
        Map<String, Object> result = new java.util.HashMap<>();

        Homework homework = homeworkService.getById(homeworkId);
        if (homework == null) {
            result.put("success", false);
            result.put("message", "作业不存在");
            return result;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(homework.getDeadline())) {
            result.put("success", false);
            result.put("message", "已超过截止时间，无法提交");
            return result;
        }

        User student = userService.getById(studentId);
        if (student == null) {
            result.put("success", false);
            result.put("message", "学生不存在");
            return result;
        }

        HomeworkSubmission submission = getByHomeworkAndStudent(homeworkId, studentId);
        boolean isNew = (submission == null);

        if (isNew) {
            submission = new HomeworkSubmission();
            submission.setHomeworkId(homeworkId);
            submission.setStudentId(studentId);
            submission.setStudentName(student.getUsername());
            submission.setStudentNo(student.getUserno());
            submission.setSubmissionCount(1);
        } else {
            String oldFileUrl = submission.getFileUrl();
            if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
                String oldFilePath = uploadPath + oldFileUrl.substring("/uploads/".length());
                File oldFile = new File(oldFilePath);
                if (oldFile.exists()) {
                    oldFile.delete();
                }
            }
            submission.setSubmissionCount(submission.getSubmissionCount() + 1);
        }

        submission.setFileUrl(fileUrl);
        submission.setFileName(fileName);
        submission.setStatus("SUBMITTED");
        submission.setSubmittedAt(now);
        submission.setScore(null);
        submission.setComment(null);
        submission.setGradedAt(null);
        submission.setGradedBy(null);
        submission.setGradedByName(null);

        this.saveOrUpdate(submission);

        result.put("success", true);
        result.put("message", isNew ? "提交成功" : "重新提交成功，已覆盖旧文件");
        result.put("submission", submission);
        return result;
    }

    @Transactional
    public Map<String, Object> gradeHomework(Integer submissionId, BigDecimal score, String comment, Integer teacherId, String teacherName) {
        Map<String, Object> result = new java.util.HashMap<>();

        HomeworkSubmission submission = this.getById(submissionId);
        if (submission == null) {
            result.put("success", false);
            result.put("message", "提交记录不存在");
            return result;
        }

        if (!"SUBMITTED".equals(submission.getStatus()) && !"REJECTED".equals(submission.getStatus())) {
            result.put("success", false);
            result.put("message", "该作业状态不允许批改");
            return result;
        }

        if (score == null) {
            result.put("success", false);
            result.put("message", "请输入分数");
            return result;
        }

        Homework homework = homeworkService.getById(submission.getHomeworkId());
        if (homework != null && score.compareTo(homework.getFullScore()) > 0) {
            result.put("success", false);
            result.put("message", "分数不能超过满分 " + homework.getFullScore());
            return result;
        }

        if (score.compareTo(BigDecimal.ZERO) < 0) {
            result.put("success", false);
            result.put("message", "分数不能小于0");
            return result;
        }

        submission.setScore(score);
        submission.setComment(comment);
        submission.setStatus("GRADED");
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(teacherId);
        submission.setGradedByName(teacherName);

        this.updateById(submission);

        result.put("success", true);
        result.put("message", "批改成功");
        result.put("submission", submission);
        return result;
    }

    @Transactional
    public Map<String, Object> rejectHomework(Integer submissionId, String comment, Integer teacherId, String teacherName) {
        Map<String, Object> result = new java.util.HashMap<>();

        HomeworkSubmission submission = this.getById(submissionId);
        if (submission == null) {
            result.put("success", false);
            result.put("message", "提交记录不存在");
            return result;
        }

        Homework homework = homeworkService.getById(submission.getHomeworkId());
        LocalDateTime now = LocalDateTime.now();
        if (homework != null && now.isAfter(homework.getDeadline())) {
            result.put("success", false);
            result.put("message", "已超过截止时间，无法打回重交");
            return result;
        }

        if (!"SUBMITTED".equals(submission.getStatus())) {
            result.put("success", false);
            result.put("message", "该作业状态不允许打回");
            return result;
        }

        submission.setStatus("REJECTED");
        submission.setComment(comment);
        submission.setGradedAt(LocalDateTime.now());
        submission.setGradedBy(teacherId);
        submission.setGradedByName(teacherName);
        submission.setScore(null);

        this.updateById(submission);

        result.put("success", true);
        result.put("message", "已打回，学生可在截止前重新提交");
        result.put("submission", submission);
        return result;
    }

    public List<HomeworkSubmission> getSubmissionsWithStatus(Integer homeworkId) {
        Homework homework = homeworkService.getById(homeworkId);
        if (homework == null) {
            return new ArrayList<>();
        }

        List<User> classStudents = userService.lambdaQuery()
                .eq(User::getClassId, homework.getClassId())
                .eq(User::getCheckedok, "已通过")
                .list();

        List<HomeworkSubmission> existingSubmissions = listByHomeworkId(homeworkId);
        Map<Integer, HomeworkSubmission> submissionMap = existingSubmissions.stream()
                .collect(Collectors.toMap(HomeworkSubmission::getStudentId, s -> s));

        List<HomeworkSubmission> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = now.isAfter(homework.getDeadline());

        for (User student : classStudents) {
            HomeworkSubmission submission = submissionMap.get(student.getUid());
            if (submission == null) {
                submission = new HomeworkSubmission();
                submission.setHomeworkId(homeworkId);
                submission.setStudentId(student.getUid());
                submission.setStudentName(student.getUsername());
                submission.setStudentNo(student.getUserno());
                submission.setSubmissionCount(0);
                submission.setStatus(isOverdue ? "OVERDUE" : "NOT_SUBMITTED");
            } else {
                if ("NOT_SUBMITTED".equals(submission.getStatus()) && isOverdue) {
                    submission.setStatus("OVERDUE");
                }
            }
            result.add(submission);
        }

        result.sort((a, b) -> {
            String[] statusOrder = {"NOT_SUBMITTED", "SUBMITTED", "REJECTED", "GRADED", "OVERDUE"};
            int orderA = 0, orderB = 0;
            for (int i = 0; i < statusOrder.length; i++) {
                if (statusOrder[i].equals(a.getStatus())) orderA = i;
                if (statusOrder[i].equals(b.getStatus())) orderB = i;
            }
            if (orderA != orderB) return orderA - orderB;
            return a.getStudentNo().compareTo(b.getStudentNo());
        });

        return result;
    }

    public List<User> getUnsubmittedStudents(Integer homeworkId) {
        Homework homework = homeworkService.getById(homeworkId);
        if (homework == null) {
            return new ArrayList<>();
        }

        List<User> classStudents = userService.lambdaQuery()
                .eq(User::getClassId, homework.getClassId())
                .eq(User::getCheckedok, "已通过")
                .list();

        List<HomeworkSubmission> submissions = listByHomeworkId(homeworkId);
        List<Integer> submittedStudentIds = submissions.stream()
                .filter(s -> !"NOT_SUBMITTED".equals(s.getStatus()) && !"OVERDUE".equals(s.getStatus()))
                .map(HomeworkSubmission::getStudentId)
                .collect(Collectors.toList());

        return classStudents.stream()
                .filter(s -> !submittedStudentIds.contains(s.getUid()))
                .collect(Collectors.toList());
    }

    public List<HomeworkSubmission> listByStudentIdWithOverdueCheck(Integer studentId) {
        List<HomeworkSubmission> submissions = listByStudentId(studentId);
        LocalDateTime now = LocalDateTime.now();

        for (HomeworkSubmission submission : submissions) {
            Homework homework = homeworkService.getById(submission.getHomeworkId());
            if (homework != null && now.isAfter(homework.getDeadline())) {
                if ("NOT_SUBMITTED".equals(submission.getStatus())) {
                    submission.setStatus("OVERDUE");
                }
            }
        }
        return submissions;
    }
}
