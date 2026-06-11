package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.SignInRecord;
import com.school.entity.SignInTask;
import com.school.entity.User;
import com.school.mapper.SignInRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SignInRecordService extends ServiceImpl<SignInRecordMapper, SignInRecord> {

    @Autowired
    private SignInTaskService signInTaskService;

    @Autowired
    private UserService userService;

    public List<SignInRecord> listByTaskId(Integer taskId) {
        return this.lambdaQuery()
                .eq(SignInRecord::getTaskId, taskId)
                .list();
    }

    public List<SignInRecord> listByStudentId(Integer studentId) {
        return this.lambdaQuery()
                .eq(SignInRecord::getStudentId, studentId)
                .list();
    }

    public List<SignInRecord> listByTaskIds(List<Integer> taskIds, Integer studentId) {
        return this.lambdaQuery()
                .in(SignInRecord::getTaskId, taskIds)
                .eq(SignInRecord::getStudentId, studentId)
                .list();
    }

    public SignInRecord getByTaskAndStudent(Integer taskId, Integer studentId) {
        return this.lambdaQuery()
                .eq(SignInRecord::getTaskId, taskId)
                .eq(SignInRecord::getStudentId, studentId)
                .one();
    }

    @Transactional
    public synchronized Map<String, Object> studentSignIn(Integer taskId, Integer studentId, String clientIp) {
        Map<String, Object> result = new HashMap<>();

        SignInTask task = signInTaskService.getById(taskId);
        if (task == null) {
            result.put("success", false);
            result.put("message", "签到任务不存在");
            return result;
        }

        if ("ONGOING".equals(task.getStatus())) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isAfter(task.getEndTime())) {
                task.setStatus("ENDED");
                signInTaskService.updateById(task);
                signInTaskService.updateTaskStatistics(taskId);
                result.put("success", false);
                result.put("message", "签到已超时结束");
                return result;
            }
        } else if ("ENDED".equals(task.getStatus())) {
            result.put("success", false);
            result.put("message", "签到已结束");
            return result;
        }

        User student = userService.getById(studentId);
        if (student == null) {
            result.put("success", false);
            result.put("message", "学生不存在");
            return result;
        }

        if (!task.getClassId().equals(student.getClassId())) {
            result.put("success", false);
            result.put("message", "您不属于该签到班级");
            return result;
        }

        SignInRecord record = getByTaskAndStudent(taskId, studentId);
        if (record == null) {
            record = new SignInRecord();
            record.setTaskId(taskId);
            record.setStudentId(studentId);
            record.setStudentName(student.getUsername());
            record.setStudentNo(student.getUserno());
            record.setIsManual(0);
        }

        if ("SIGNED".equals(record.getStatus())) {
            result.put("success", true);
            result.put("message", "您已完成签到，无需重复操作");
            result.put("alreadySigned", true);
            result.put("record", record);
            return result;
        }

        if ("LEAVE".equals(record.getStatus())) {
            result.put("success", false);
            result.put("message", "您已被标记为请假，如需签到请联系老师");
            return result;
        }

        LocalDateTime now = LocalDateTime.now();
        record.setStatus("SIGNED");
        record.setSignInTime(now);
        record.setSignInIp(clientIp);
        record.setUpdatedAt(now);

        this.saveOrUpdate(record);
        signInTaskService.updateTaskStatistics(taskId);

        result.put("success", true);
        result.put("message", "签到成功");
        result.put("record", record);
        return result;
    }

    @Transactional
    public Map<String, Object> manualUpdateRecord(Integer taskId, Integer studentId, String targetStatus,
                                                   String remark, Integer teacherId, String teacherName) {
        Map<String, Object> result = new HashMap<>();

        if (!"SIGNED".equals(targetStatus) && !"ABSENT".equals(targetStatus) && !"LEAVE".equals(targetStatus)) {
            result.put("success", false);
            result.put("message", "无效的状态值");
            return result;
        }

        SignInTask task = signInTaskService.getById(taskId);
        if (task == null) {
            result.put("success", false);
            result.put("message", "签到任务不存在");
            return result;
        }

        User student = userService.getById(studentId);
        if (student == null) {
            result.put("success", false);
            result.put("message", "学生不存在");
            return result;
        }

        SignInRecord record = getByTaskAndStudent(taskId, studentId);
        boolean isNew = (record == null);
        if (isNew) {
            record = new SignInRecord();
            record.setTaskId(taskId);
            record.setStudentId(studentId);
            record.setStudentName(student.getUsername());
            record.setStudentNo(student.getUserno());
        }

        LocalDateTime now = LocalDateTime.now();
        String oldStatus = record.getStatus();

        if (!oldStatus.equals(targetStatus)) {
            record.setStatus(targetStatus);

            if ("SIGNED".equals(targetStatus) && record.getSignInTime() == null) {
                record.setSignInTime(now);
            }

            record.setIsManual(1);
            record.setManualBy(teacherId);
            record.setManualByName(teacherName);
            record.setManualTime(now);
            record.setRemark(remark);
            record.setUpdatedAt(now);

            this.saveOrUpdate(record);
            signInTaskService.updateTaskStatistics(taskId);

            result.put("success", true);
            result.put("message", "补登成功，状态已从「" + getStatusText(oldStatus) + "」更新为「" + getStatusText(targetStatus) + "」");
            result.put("record", record);
        } else {
            record.setRemark(remark);
            record.setIsManual(1);
            record.setManualBy(teacherId);
            record.setManualByName(teacherName);
            record.setManualTime(now);
            record.setUpdatedAt(now);
            this.saveOrUpdate(record);

            result.put("success", true);
            result.put("message", "备注信息已更新");
            result.put("record", record);
        }

        return result;
    }

    private String getStatusText(String status) {
        switch (status) {
            case "SIGNED":
                return "已签到";
            case "ABSENT":
                return "缺勤";
            case "LEAVE":
                return "请假";
            default:
                return "未知";
        }
    }
}
