package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Classes;
import com.school.entity.Homework;
import com.school.entity.HomeworkSubmission;
import com.school.entity.User;
import com.school.mapper.HomeworkMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HomeworkService extends ServiceImpl<HomeworkMapper, Homework> {

    @Autowired
    private ClassesService classesService;

    @Autowired
    private UserService userService;

    @Autowired
    private HomeworkSubmissionService submissionService;

    @Transactional
    public Map<String, Object> createHomework(Homework homework) {
        Map<String, Object> result = new HashMap<>();

        if (homework.getTitle() == null || homework.getTitle().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "作业标题不能为空");
            return result;
        }

        if (homework.getClassId() == null) {
            result.put("success", false);
            result.put("message", "请选择所属班级");
            return result;
        }

        if (homework.getDeadline() == null) {
            result.put("success", false);
            result.put("message", "请设置截止时间");
            return result;
        }

        if (homework.getDeadline().isBefore(LocalDateTime.now())) {
            result.put("success", false);
            result.put("message", "截止时间不能早于当前时间");
            return result;
        }

        Classes cls = classesService.getById(homework.getClassId());
        if (cls != null) {
            homework.setClassName(cls.getCname());
        }

        homework.setStatus("PUBLISHED");
        homework.setCreatedAt(LocalDateTime.now());
        homework.setUpdatedAt(LocalDateTime.now());

        this.save(homework);

        List<User> students = userService.lambdaQuery()
                .eq(User::getClassId, homework.getClassId())
                .eq(User::getCheckedok, "已通过")
                .list();

        for (User student : students) {
            HomeworkSubmission submission = new HomeworkSubmission();
            submission.setHomeworkId(homework.getHomeworkId());
            submission.setStudentId(student.getUid());
            submission.setStudentName(student.getUsername());
            submission.setStudentNo(student.getUserno());
            submission.setSubmissionCount(0);
            submission.setStatus("NOT_SUBMITTED");
            submissionService.save(submission);
        }

        result.put("success", true);
        result.put("message", "作业布置成功");
        result.put("homeworkId", homework.getHomeworkId());
        return result;
    }

    public Homework getDetail(Integer homeworkId) {
        Homework homework = this.getById(homeworkId);
        if (homework != null) {
            List<User> classStudents = userService.lambdaQuery()
                    .eq(User::getClassId, homework.getClassId())
                    .eq(User::getCheckedok, "已通过")
                    .list();
            homework.setTotalStudents(classStudents.size());

            List<HomeworkSubmission> submissions = submissionService.listByHomeworkId(homeworkId);
            int submittedCount = 0;
            int gradedCount = 0;
            for (HomeworkSubmission s : submissions) {
                if ("SUBMITTED".equals(s.getStatus()) || "GRADED".equals(s.getStatus()) || "REJECTED".equals(s.getStatus())) {
                    submittedCount++;
                }
                if ("GRADED".equals(s.getStatus())) {
                    gradedCount++;
                }
            }
            homework.setSubmittedCount(submittedCount);
            homework.setUnsubmittedCount(classStudents.size() - submittedCount);
            homework.setGradedCount(gradedCount);
        }
        return homework;
    }

    public List<Homework> listForTeacher(Integer teacherId) {
        List<Homework> list = this.lambdaQuery()
                .eq(Homework::getCreatedBy, teacherId)
                .orderByDesc(Homework::getCreatedAt)
                .list();
        for (Homework homework : list) {
            enrichHomeworkStats(homework);
        }
        return list;
    }

    public List<Homework> listForStudent(Integer classId) {
        List<Homework> list = this.lambdaQuery()
                .eq(Homework::getClassId, classId)
                .eq(Homework::getStatus, "PUBLISHED")
                .orderByDesc(Homework::getCreatedAt)
                .list();
        for (Homework homework : list) {
            enrichHomeworkStats(homework);
        }
        return list;
    }

    private void enrichHomeworkStats(Homework homework) {
        List<User> classStudents = userService.lambdaQuery()
                .eq(User::getClassId, homework.getClassId())
                .eq(User::getCheckedok, "已通过")
                .list();
        homework.setTotalStudents(classStudents.size());

        List<HomeworkSubmission> submissions = submissionService.listByHomeworkId(homework.getHomeworkId());
        int submittedCount = 0;
        int gradedCount = 0;
        for (HomeworkSubmission s : submissions) {
            if ("SUBMITTED".equals(s.getStatus()) || "GRADED".equals(s.getStatus()) || "REJECTED".equals(s.getStatus())) {
                submittedCount++;
            }
            if ("GRADED".equals(s.getStatus())) {
                gradedCount++;
            }
        }
        homework.setSubmittedCount(submittedCount);
        homework.setUnsubmittedCount(classStudents.size() - submittedCount);
        homework.setGradedCount(gradedCount);
    }

    public Map<String, Object> getHomeworkStatistics(Integer homeworkId) {
        Map<String, Object> result = new HashMap<>();
        Homework homework = getDetail(homeworkId);
        result.put("homework", homework);

        List<HomeworkSubmission> submissions = submissionService.getSubmissionsWithStatus(homeworkId);
        result.put("submissions", submissions);

        int totalStudents = homework.getTotalStudents();
        int submittedCount = homework.getSubmittedCount();
        int gradedCount = homework.getGradedCount();
        int unsubmittedCount = totalStudents - submittedCount;

        result.put("totalStudents", totalStudents);
        result.put("submittedCount", submittedCount);
        result.put("gradedCount", gradedCount);
        result.put("unsubmittedCount", unsubmittedCount);

        List<HomeworkSubmission> gradedSubmissions = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        for (HomeworkSubmission s : submissions) {
            if ("GRADED".equals(s.getStatus()) && s.getScore() != null) {
                gradedSubmissions.add(s);
                totalScore = totalScore.add(s.getScore());
            }
        }

        if (!gradedSubmissions.isEmpty()) {
            BigDecimal avgScore = totalScore.divide(BigDecimal.valueOf(gradedSubmissions.size()), 2, RoundingMode.HALF_UP);
            result.put("avgScore", avgScore);
        } else {
            result.put("avgScore", BigDecimal.ZERO);
        }

        result.put("unsubmittedStudents", submissionService.getUnsubmittedStudents(homeworkId));

        return result;
    }

    @Transactional
    public boolean deleteHomework(Integer homeworkId) {
        submissionService.lambdaUpdate()
                .eq(HomeworkSubmission::getHomeworkId, homeworkId)
                .remove();
        return this.removeById(homeworkId);
    }
}
