package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Classes;
import com.school.entity.SignInRecord;
import com.school.entity.SignInTask;
import com.school.entity.User;
import com.school.mapper.SignInTaskMapper;
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
public class SignInTaskService extends ServiceImpl<SignInTaskMapper, SignInTask> {

    @Autowired
    private ClassesService classesService;

    @Autowired
    private UserService userService;

    @Autowired
    private SignInRecordService signInRecordService;

    @Transactional
    public Map<String, Object> createTask(SignInTask task) {
        Map<String, Object> result = new HashMap<>();

        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "签到主题不能为空");
            return result;
        }

        if (task.getClassId() == null) {
            result.put("success", false);
            result.put("message", "请选择班级");
            return result;
        }

        if (task.getDurationMinutes() == null || task.getDurationMinutes() < 1) {
            result.put("success", false);
            result.put("message", "有效时长必须大于0分钟");
            return result;
        }

        if (task.getDurationMinutes() > 180) {
            result.put("success", false);
            result.put("message", "有效时长不能超过180分钟");
            return result;
        }

        Classes cls = classesService.getById(task.getClassId());
        if (cls == null) {
            result.put("success", false);
            result.put("message", "班级不存在");
            return result;
        }
        task.setClassName(cls.getCname());

        LocalDateTime now = LocalDateTime.now();
        task.setStartTime(now);
        task.setEndTime(now.plusMinutes(task.getDurationMinutes()));
        task.setStatus("ONGOING");
        task.setSignedCount(0);
        task.setAbsentCount(0);
        task.setLeaveCount(0);
        task.setAttendanceRate(BigDecimal.ZERO);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);

        List<User> students = userService.lambdaQuery()
                .eq(User::getClassId, task.getClassId())
                .eq(User::getCheckedok, "已通过")
                .list();
        task.setTotalStudents(students.size());

        this.save(task);

        for (User student : students) {
            SignInRecord record = new SignInRecord();
            record.setTaskId(task.getTaskId());
            record.setStudentId(student.getUid());
            record.setStudentName(student.getUsername());
            record.setStudentNo(student.getUserno());
            record.setStatus("ABSENT");
            record.setIsManual(0);
            signInRecordService.save(record);
        }

        result.put("success", true);
        result.put("message", "签到任务发起成功");
        result.put("taskId", task.getTaskId());
        return result;
    }

    @Transactional
    public Map<String, Object> endTask(Integer taskId, Integer teacherId, String teacherName) {
        Map<String, Object> result = new HashMap<>();

        SignInTask task = this.getById(taskId);
        if (task == null) {
            result.put("success", false);
            result.put("message", "签到任务不存在");
            return result;
        }

        if ("ENDED".equals(task.getStatus())) {
            result.put("success", false);
            result.put("message", "签到任务已结束");
            return result;
        }

        task.setStatus("ENDED");
        task.setEndTime(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        this.updateById(task);

        updateTaskStatistics(taskId);

        result.put("success", true);
        result.put("message", "签到任务已结束");
        return result;
    }

    public void checkAndEndExpiredTasks() {
        List<SignInTask> ongoingTasks = this.lambdaQuery()
                .eq(SignInTask::getStatus, "ONGOING")
                .list();
        LocalDateTime now = LocalDateTime.now();
        for (SignInTask task : ongoingTasks) {
            if (now.isAfter(task.getEndTime())) {
                task.setStatus("ENDED");
                task.setUpdatedAt(now);
                this.updateById(task);
                updateTaskStatistics(task.getTaskId());
            }
        }
    }

    private SignInTask refreshTaskStatus(SignInTask task) {
        if (task == null) return null;
        if ("ONGOING".equals(task.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(task.getEndTime())) {
                task.setStatus("ENDED");
                task.setUpdatedAt(now);
                this.updateById(task);
                updateTaskStatistics(task.getTaskId());
            } else {
                long remaining = Duration.between(now, task.getEndTime()).getSeconds();
                task.setRemainingSeconds(remaining);
            }
        }
        return task;
    }

    public SignInTask getTaskDetail(Integer taskId) {
        SignInTask task = this.getById(taskId);
        task = refreshTaskStatus(task);
        if (task != null) {
            List<SignInRecord> records = signInRecordService.listByTaskId(taskId);
            task.setRecords(records);
        }
        return task;
    }

    public List<SignInTask> listForTeacher(Integer teacherId) {
        checkAndEndExpiredTasks();
        List<SignInTask> list = this.lambdaQuery()
                .eq(SignInTask::getCreatedBy, teacherId)
                .orderByDesc(SignInTask::getCreatedAt)
                .list();
        list.forEach(this::refreshTaskStatus);
        return list;
    }

    public List<SignInTask> listForClass(Integer classId) {
        checkAndEndExpiredTasks();
        List<SignInTask> list = this.lambdaQuery()
                .eq(SignInTask::getClassId, classId)
                .orderByDesc(SignInTask::getCreatedAt)
                .list();
        list.forEach(this::refreshTaskStatus);
        return list;
    }

    public List<SignInTask> listOngoingForStudent(Integer classId) {
        checkAndEndExpiredTasks();
        LocalDateTime now = LocalDateTime.now();
        List<SignInTask> list = this.lambdaQuery()
                .eq(SignInTask::getClassId, classId)
                .eq(SignInTask::getStatus, "ONGOING")
                .orderByDesc(SignInTask::getCreatedAt)
                .list();
        list.forEach(this::refreshTaskStatus);
        return list;
    }

    @Transactional
    public void updateTaskStatistics(Integer taskId) {
        SignInTask task = this.getById(taskId);
        if (task == null) return;

        List<SignInRecord> records = signInRecordService.listByTaskId(taskId);
        int signedCount = 0;
        int absentCount = 0;
        int leaveCount = 0;
        for (SignInRecord r : records) {
            switch (r.getStatus()) {
                case "SIGNED":
                    signedCount++;
                    break;
                case "ABSENT":
                    absentCount++;
                    break;
                case "LEAVE":
                    leaveCount++;
                    break;
            }
        }

        task.setSignedCount(signedCount);
        task.setAbsentCount(absentCount);
        task.setLeaveCount(leaveCount);

        BigDecimal rate = BigDecimal.ZERO;
        if (task.getTotalStudents() != null && task.getTotalStudents() > 0) {
            int attended = signedCount + leaveCount;
            rate = BigDecimal.valueOf(attended)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(task.getTotalStudents()), 2, RoundingMode.HALF_UP);
        }
        task.setAttendanceRate(rate);
        task.setUpdatedAt(LocalDateTime.now());
        this.updateById(task);
    }

    public Map<String, Object> getTaskStatistics(Integer taskId) {
        Map<String, Object> result = new HashMap<>();
        SignInTask task = getTaskDetail(taskId);
        if (task == null) {
            result.put("success", false);
            result.put("message", "任务不存在");
            return result;
        }

        List<SignInRecord> records = signInRecordService.listByTaskId(taskId);
        result.put("task", task);
        result.put("records", records);

        List<SignInRecord> signedList = records.stream()
                .filter(r -> "SIGNED".equals(r.getStatus()))
                .sorted(Comparator.comparing(SignInRecord::getSignInTime))
                .collect(Collectors.toList());
        List<SignInRecord> absentList = records.stream()
                .filter(r -> "ABSENT".equals(r.getStatus()))
                .collect(Collectors.toList());
        List<SignInRecord> leaveList = records.stream()
                .filter(r -> "LEAVE".equals(r.getStatus()))
                .collect(Collectors.toList());

        result.put("signedList", signedList);
        result.put("absentList", absentList);
        result.put("leaveList", leaveList);
        result.put("signedCount", signedList.size());
        result.put("absentCount", absentList.size());
        result.put("leaveCount", leaveList.size());
        result.put("totalStudents", task.getTotalStudents());
        result.put("attendanceRate", task.getAttendanceRate());

        return result;
    }

    public Map<String, Object> getStudentCumulativeStats(Integer studentId, Integer classId) {
        Map<String, Object> result = new HashMap<>();

        List<SignInTask> allTasks = listForClass(classId);
        List<SignInRecord> studentRecords = signInRecordService.listByStudentId(studentId);

        int totalTasks = allTasks.size();
        int signedCount = 0;
        int absentCount = 0;
        int leaveCount = 0;
        int missedTasks = 0;

        Map<Integer, SignInRecord> recordMap = studentRecords.stream()
                .collect(Collectors.toMap(SignInRecord::getTaskId, r -> r));

        for (SignInTask task : allTasks) {
            SignInRecord record = recordMap.get(task.getTaskId());
            if (record != null) {
                switch (record.getStatus()) {
                    case "SIGNED":
                        signedCount++;
                        break;
                    case "ABSENT":
                        absentCount++;
                        break;
                    case "LEAVE":
                        leaveCount++;
                        break;
                }
            } else {
                missedTasks++;
                absentCount++;
            }
        }

        BigDecimal rate = BigDecimal.ZERO;
        if (totalTasks > 0) {
            int attended = signedCount + leaveCount;
            rate = BigDecimal.valueOf(attended)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalTasks), 2, RoundingMode.HALF_UP);
        }

        result.put("totalTasks", totalTasks);
        result.put("signedCount", signedCount);
        result.put("absentCount", absentCount);
        result.put("leaveCount", leaveCount);
        result.put("attendanceRate", rate);
        result.put("taskList", allTasks);
        result.put("recordMap", recordMap);

        return result;
    }

    public List<Map<String, Object>> getClassCumulativeStats(Integer classId) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<SignInTask> allTasks = listForClass(classId);
        int totalTasks = allTasks.size();

        List<User> students = userService.lambdaQuery()
                .eq(User::getClassId, classId)
                .eq(User::getCheckedok, "已通过")
                .list();

        for (User student : students) {
            Map<String, Object> stat = new HashMap<>();
            List<SignInRecord> records = signInRecordService.listByTaskIds(
                    allTasks.stream().map(SignInTask::getTaskId).collect(Collectors.toList()),
                    student.getUid()
            );
            Map<Integer, SignInRecord> recordMap = records.stream()
                    .collect(Collectors.toMap(SignInRecord::getTaskId, r -> r));

            int signedCount = 0;
            int absentCount = 0;
            int leaveCount = 0;

            for (SignInTask task : allTasks) {
                SignInRecord record = recordMap.get(task.getTaskId());
                if (record != null) {
                    switch (record.getStatus()) {
                        case "SIGNED":
                            signedCount++;
                            break;
                        case "ABSENT":
                            absentCount++;
                            break;
                        case "LEAVE":
                            leaveCount++;
                            break;
                    }
                } else {
                    absentCount++;
                }
            }

            BigDecimal rate = BigDecimal.ZERO;
            if (totalTasks > 0) {
                int attended = signedCount + leaveCount;
                rate = BigDecimal.valueOf(attended)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(totalTasks), 2, RoundingMode.HALF_UP);
            }

            stat.put("studentId", student.getUid());
            stat.put("studentName", student.getUsername());
            stat.put("studentNo", student.getUserno());
            stat.put("totalTasks", totalTasks);
            stat.put("signedCount", signedCount);
            stat.put("absentCount", absentCount);
            stat.put("leaveCount", leaveCount);
            stat.put("attendanceRate", rate);
            result.add(stat);
        }

        result.sort((a, b) -> ((BigDecimal) b.get("attendanceRate")).compareTo((BigDecimal) a.get("attendanceRate")));
        return result;
    }
}
