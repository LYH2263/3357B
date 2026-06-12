package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.PointSummary;
import com.school.mapper.PointSummaryMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PointSummaryService extends ServiceImpl<PointSummaryMapper, PointSummary> {

    public PointSummary getByStudentId(Integer studentId) {
        return this.lambdaQuery()
                .eq(PointSummary::getStudentId, studentId)
                .one();
    }

    public List<PointSummary> getClassRanking(Integer classId) {
        return this.lambdaQuery()
                .eq(PointSummary::getClassId, classId)
                .orderByDesc(PointSummary::getTotalPoints)
                .orderByAsc(PointSummary::getStudentId)
                .list();
    }

    public List<PointSummary> getSchoolRanking() {
        return this.lambdaQuery()
                .orderByDesc(PointSummary::getTotalPoints)
                .orderByAsc(PointSummary::getStudentId)
                .list();
    }

    public List<PointSummary> getClassRankingPage(Integer classId, Integer limit) {
        return this.lambdaQuery()
                .eq(PointSummary::getClassId, classId)
                .orderByDesc(PointSummary::getTotalPoints)
                .orderByAsc(PointSummary::getStudentId)
                .last("LIMIT " + limit)
                .list();
    }

    public List<PointSummary> getSchoolRankingPage(Integer limit) {
        return this.lambdaQuery()
                .orderByDesc(PointSummary::getTotalPoints)
                .orderByAsc(PointSummary::getStudentId)
                .last("LIMIT " + limit)
                .list();
    }
}
