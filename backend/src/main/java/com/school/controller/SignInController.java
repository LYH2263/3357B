package com.school.controller;

import com.school.entity.SignInRecord;
import com.school.entity.SignInTask;
import com.school.service.SignInRecordService;
import com.school.service.SignInTaskService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/signin")
@CrossOrigin
public class SignInController {

    @Autowired
    private SignInTaskService signInTaskService;

    @Autowired
    private SignInRecordService signInRecordService;

    @PostMapping("/task/create")
    public Map<String, Object> createTask(@RequestBody SignInTask task) {
        return signInTaskService.createTask(task);
    }

    @PostMapping("/task/end/{taskId}")
    public Map<String, Object> endTask(@PathVariable Integer taskId, @RequestBody Map<String, Object> params) {
        Integer teacherId = (Integer) params.get("teacherId");
        String teacherName = (String) params.get("teacherName");
        return signInTaskService.endTask(taskId, teacherId, teacherName);
    }

    @GetMapping("/task/teacher/list")
    public List<SignInTask> teacherList(@RequestParam Integer teacherId) {
        return signInTaskService.listForTeacher(teacherId);
    }

    @GetMapping("/task/class/list")
    public List<SignInTask> classList(@RequestParam Integer classId) {
        return signInTaskService.listForClass(classId);
    }

    @GetMapping("/task/ongoing")
    public List<SignInTask> ongoingTasks(@RequestParam Integer classId) {
        return signInTaskService.listOngoingForStudent(classId);
    }

    @GetMapping("/task/detail/{taskId}")
    public SignInTask taskDetail(@PathVariable Integer taskId) {
        return signInTaskService.getTaskDetail(taskId);
    }

    @GetMapping("/task/statistics/{taskId}")
    public Map<String, Object> taskStatistics(@PathVariable Integer taskId) {
        return signInTaskService.getTaskStatistics(taskId);
    }

    @GetMapping("/task/records/{taskId}")
    public List<SignInRecord> taskRecords(@PathVariable Integer taskId) {
        return signInRecordService.listByTaskId(taskId);
    }

    @PostMapping("/student/signin")
    public Map<String, Object> studentSignIn(@RequestBody Map<String, Object> params, HttpServletRequest request) {
        Integer taskId = (Integer) params.get("taskId");
        Integer studentId = (Integer) params.get("studentId");
        String clientIp = getClientIp(request);
        return signInRecordService.studentSignIn(taskId, studentId, clientIp);
    }

    @GetMapping("/student/record/{taskId}")
    public SignInRecord studentRecord(@PathVariable Integer taskId, @RequestParam Integer studentId) {
        return signInRecordService.getByTaskAndStudent(taskId, studentId);
    }

    @GetMapping("/student/cumulative")
    public Map<String, Object> studentCumulative(@RequestParam Integer studentId, @RequestParam Integer classId) {
        return signInTaskService.getStudentCumulativeStats(studentId, classId);
    }

    @PostMapping("/record/manual")
    public Map<String, Object> manualUpdateRecord(@RequestBody Map<String, Object> params) {
        Integer taskId = (Integer) params.get("taskId");
        Integer studentId = (Integer) params.get("studentId");
        String targetStatus = (String) params.get("targetStatus");
        String remark = (String) params.get("remark");
        Integer teacherId = (Integer) params.get("teacherId");
        String teacherName = (String) params.get("teacherName");
        return signInRecordService.manualUpdateRecord(taskId, studentId, targetStatus, remark, teacherId, teacherName);
    }

    @GetMapping("/class/cumulative")
    public List<Map<String, Object>> classCumulative(@RequestParam Integer classId) {
        return signInTaskService.getClassCumulativeStats(classId);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
