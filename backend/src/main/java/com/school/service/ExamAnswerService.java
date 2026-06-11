package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.ExamAnswer;
import com.school.mapper.ExamAnswerMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamAnswerService extends ServiceImpl<ExamAnswerMapper, ExamAnswer> {

    public List<ExamAnswer> listByAttemptId(Integer attemptId) {
        return this.lambdaQuery()
                .eq(ExamAnswer::getAttemptId, attemptId)
                .list();
    }

    public boolean deleteByAttemptId(Integer attemptId) {
        return this.lambdaUpdate()
                .eq(ExamAnswer::getAttemptId, attemptId)
                .remove();
    }
}
