package com.school.controller;

import com.school.entity.PointDetail;
import com.school.entity.PointRule;
import com.school.service.PointDetailService;
import com.school.service.PointRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/point")
@CrossOrigin
public class PointController {

    @Autowired
    private PointDetailService pointDetailService;

    @Autowired
    private PointRuleService pointRuleService;

    @PostMapping("/award")
    public Map<String, Object> awardPoints(@RequestBody Map<String, Object> params) {
        String ruleCode = (String) params.get("ruleCode");
        Integer studentId = (Integer) params.get("studentId");
        String sourceId = (String) params.get("sourceId");
        return pointDetailService.awardPoints(ruleCode, studentId, sourceId);
    }

    @PostMapping("/manual-adjust")
    public Map<String, Object> manualAdjust(@RequestBody Map<String, Object> params) {
        Integer studentId = (Integer) params.get("studentId");
        Integer pointValue = (Integer) params.get("pointValue");
        String reason = (String) params.get("reason");
        Integer operatorId = (Integer) params.get("operatorId");
        String operatorName = (String) params.get("operatorName");
        return pointDetailService.manualAdjust(studentId, pointValue, reason, operatorId, operatorName);
    }

    @GetMapping("/student/info")
    public Map<String, Object> studentInfo(@RequestParam Integer studentId) {
        return pointDetailService.getStudentPointInfo(studentId);
    }

    @GetMapping("/student/details")
    public List<PointDetail> studentDetails(@RequestParam Integer studentId) {
        return pointDetailService.listByStudentId(studentId);
    }

    @GetMapping("/student/details-page")
    public Map<String, Object> studentDetailsPage(@RequestParam Integer studentId,
                                                    @RequestParam(defaultValue = "1") Integer page,
                                                    @RequestParam(defaultValue = "20") Integer size) {
        Map<String, Object> result = new java.util.HashMap<>();
        List<PointDetail> details = pointDetailService.listByStudentIdPage(studentId, page, size);
        Long total = pointDetailService.countByStudentId(studentId);
        result.put("details", details);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("totalPages", (total + size - 1) / size);
        return result;
    }

    @GetMapping("/class-ranking")
    public List<Map<String, Object>> classRanking(@RequestParam Integer classId) {
        return pointDetailService.getClassRankingWithTies(classId);
    }

    @GetMapping("/school-ranking")
    public List<Map<String, Object>> schoolRanking() {
        return pointDetailService.getSchoolRankingWithTies();
    }

    @GetMapping("/rules")
    public List<PointRule> listRules() {
        return pointRuleService.listEnabled();
    }

    @GetMapping("/rules/all")
    public List<PointRule> listAllRules() {
        return pointRuleService.list();
    }

    @PostMapping("/rules/update")
    public Map<String, Object> updateRule(@RequestBody PointRule rule) {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            pointRuleService.updateById(rule);
            result.put("success", true);
            result.put("message", "规则更新成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "规则更新失败：" + e.getMessage());
        }
        return result;
    }

    @PostMapping("/rules/create")
    public Map<String, Object> createRule(@RequestBody PointRule rule) {
        Map<String, Object> result = new java.util.HashMap<>();
        try {
            pointRuleService.save(rule);
            result.put("success", true);
            result.put("message", "规则创建成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "规则创建失败：" + e.getMessage());
        }
        return result;
    }
}
