package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.PointDetail;
import com.school.entity.PointRule;
import com.school.entity.PointSummary;
import com.school.entity.User;
import com.school.mapper.PointDetailMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PointDetailService extends ServiceImpl<PointDetailMapper, PointDetail> {

    @Autowired
    private PointSummaryService pointSummaryService;

    @Autowired
    private PointRuleService pointRuleService;

    @Autowired
    private UserService userService;

    public List<PointDetail> listByStudentId(Integer studentId) {
        return this.lambdaQuery()
                .eq(PointDetail::getStudentId, studentId)
                .orderByDesc(PointDetail::getCreatedAt)
                .list();
    }

    public List<PointDetail> listByStudentIdPage(Integer studentId, Integer page, Integer size) {
        return this.lambdaQuery()
                .eq(PointDetail::getStudentId, studentId)
                .orderByDesc(PointDetail::getCreatedAt)
                .last("LIMIT " + size + " OFFSET " + (page - 1) * size)
                .list();
    }

    public Long countByStudentId(Integer studentId) {
        return this.lambdaQuery()
                .eq(PointDetail::getStudentId, studentId)
                .count();
    }

    @Transactional
    public synchronized Map<String, Object> awardPoints(String ruleCode, Integer studentId, String sourceId) {
        Map<String, Object> result = new HashMap<>();

        PointRule rule = pointRuleService.getByCode(ruleCode);
        if (rule == null) {
            result.put("success", false);
            result.put("message", "积分规则不存在：" + ruleCode);
            return result;
        }

        if (rule.getIsEnabled() != 1) {
            result.put("success", false);
            result.put("message", "积分规则已禁用：" + rule.getRuleName());
            return result;
        }

        PointDetail existing = this.lambdaQuery()
                .eq(PointDetail::getSourceId, sourceId)
                .one();
        if (existing != null) {
            result.put("success", true);
            result.put("message", "积分已发放，无需重复操作");
            result.put("alreadyAwarded", true);
            result.put("detail", existing);
            return result;
        }

        if (rule.getDailyLimit() != null && rule.getDailyLimit() > 0) {
            Integer todayPoints = baseMapper.getTodayPointsByRule(studentId, ruleCode);
            if (todayPoints != null && todayPoints + rule.getPointValue() > rule.getDailyLimit()) {
                result.put("success", false);
                result.put("message", "今日该行为积分已达上限");
                return result;
            }
        }

        User student = userService.getById(studentId);
        if (student == null) {
            result.put("success", false);
            result.put("message", "学生不存在");
            return result;
        }

        PointSummary summary = pointSummaryService.getByStudentId(studentId);
        if (summary == null) {
            summary = new PointSummary();
            summary.setStudentId(studentId);
            summary.setStudentName(student.getUsername());
            summary.setStudentNo(student.getUserno());
            summary.setClassId(student.getClassId());
            summary.setClassName(student.getClassname());
            summary.setTotalPoints(0);
            pointSummaryService.save(summary);
        }

        int newTotal = summary.getTotalPoints() + rule.getPointValue();

        PointDetail detail = new PointDetail();
        detail.setStudentId(studentId);
        detail.setStudentName(student.getUsername());
        detail.setRuleCode(ruleCode);
        detail.setRuleName(rule.getRuleName());
        detail.setPointValue(rule.getPointValue());
        detail.setSourceType("SYSTEM");
        detail.setSourceId(sourceId);
        detail.setBalanceAfter(newTotal);
        this.save(detail);

        summary.setTotalPoints(newTotal);
        pointSummaryService.updateById(summary);

        result.put("success", true);
        result.put("message", "积分发放成功：+" + rule.getPointValue() + "（" + rule.getRuleName() + "）");
        result.put("detail", detail);
        result.put("totalPoints", newTotal);
        return result;
    }

    @Transactional
    public synchronized Map<String, Object> manualAdjust(Integer studentId, Integer pointValue,
                                                          String reason, Integer operatorId, String operatorName) {
        Map<String, Object> result = new HashMap<>();

        if (pointValue == 0) {
            result.put("success", false);
            result.put("message", "调整积分不能为0");
            return result;
        }

        if (reason == null || reason.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "手工调整必须填写理由");
            return result;
        }

        User student = userService.getById(studentId);
        if (student == null) {
            result.put("success", false);
            result.put("message", "学生不存在");
            return result;
        }

        PointSummary summary = pointSummaryService.getByStudentId(studentId);
        if (summary == null) {
            summary = new PointSummary();
            summary.setStudentId(studentId);
            summary.setStudentName(student.getUsername());
            summary.setStudentNo(student.getUserno());
            summary.setClassId(student.getClassId());
            summary.setClassName(student.getClassname());
            summary.setTotalPoints(0);
            pointSummaryService.save(summary);
        }

        int newTotal = summary.getTotalPoints() + pointValue;

        String sourceId = "MANUAL_" + studentId + "_" + System.currentTimeMillis();

        PointDetail detail = new PointDetail();
        detail.setStudentId(studentId);
        detail.setStudentName(student.getUsername());
        detail.setRuleCode("MANUAL_ADJUST");
        detail.setRuleName("手工调整");
        detail.setPointValue(pointValue);
        detail.setSourceType("MANUAL");
        detail.setSourceId(sourceId);
        detail.setReason(reason);
        detail.setOperatorId(operatorId);
        detail.setOperatorName(operatorName);
        detail.setBalanceAfter(newTotal);
        this.save(detail);

        summary.setTotalPoints(newTotal);
        pointSummaryService.updateById(summary);

        String action = pointValue > 0 ? "加分" : "扣分";
        result.put("success", true);
        result.put("message", action + "成功：" + (pointValue > 0 ? "+" : "") + pointValue);
        result.put("detail", detail);
        result.put("totalPoints", newTotal);
        return result;
    }

    public Map<String, Object> getStudentPointInfo(Integer studentId) {
        Map<String, Object> result = new HashMap<>();

        User student = userService.getById(studentId);
        if (student == null) {
            result.put("success", false);
            result.put("message", "学生不存在");
            return result;
        }

        PointSummary summary = pointSummaryService.getByStudentId(studentId);
        int totalPoints = (summary != null) ? summary.getTotalPoints() : 0;

        int rankInClass = 0;
        if (student.getClassId() != null) {
            List<PointSummary> classRanking = pointSummaryService.getClassRanking(student.getClassId());
            for (int i = 0; i < classRanking.size(); i++) {
                if (classRanking.get(i).getStudentId().equals(studentId)) {
                    rankInClass = i + 1;
                    break;
                }
            }
        }

        List<PointSummary> schoolRanking = pointSummaryService.getSchoolRanking();
        int rankInSchool = 0;
        for (int i = 0; i < schoolRanking.size(); i++) {
            if (schoolRanking.get(i).getStudentId().equals(studentId)) {
                rankInSchool = i + 1;
                break;
            }
        }

        result.put("success", true);
        result.put("studentId", studentId);
        result.put("studentName", student.getUsername());
        result.put("studentNo", student.getUserno());
        result.put("classId", student.getClassId());
        result.put("className", student.getClassname());
        result.put("totalPoints", totalPoints);
        result.put("rankInClass", rankInClass);
        result.put("rankInSchool", rankInSchool);
        return result;
    }

    public List<Map<String, Object>> getClassRankingWithTies(Integer classId) {
        List<PointSummary> ranking = pointSummaryService.getClassRanking(classId);
        return buildRankingWithTies(ranking);
    }

    public List<Map<String, Object>> getSchoolRankingWithTies() {
        List<PointSummary> ranking = pointSummaryService.getSchoolRanking();
        return buildRankingWithTies(ranking);
    }

    private List<Map<String, Object>> buildRankingWithTies(List<PointSummary> ranking) {
        java.util.ArrayList<Map<String, Object>> result = new java.util.ArrayList<>();
        int rank = 0;
        int prevPoints = Integer.MIN_VALUE;
        int sameRankCount = 0;

        for (int i = 0; i < ranking.size(); i++) {
            PointSummary s = ranking.get(i);
            Map<String, Object> item = new HashMap<>();
            item.put("summaryId", s.getSummaryId());
            item.put("studentId", s.getStudentId());
            item.put("studentName", s.getStudentName());
            item.put("studentNo", s.getStudentNo());
            item.put("classId", s.getClassId());
            item.put("className", s.getClassName());
            item.put("totalPoints", s.getTotalPoints());

            if (s.getTotalPoints() != prevPoints) {
                rank = rank + sameRankCount + 1;
                sameRankCount = 0;
                prevPoints = s.getTotalPoints();
            } else {
                sameRankCount++;
            }

            item.put("rank", rank);
            result.add(item);
        }

        return result;
    }
}
