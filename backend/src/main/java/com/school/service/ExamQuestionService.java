package com.school.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.ExamOption;
import com.school.entity.ExamQuestion;
import com.school.mapper.ExamQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamQuestionService extends ServiceImpl<ExamQuestionMapper, ExamQuestion> {

    @Autowired
    private ExamOptionService optionService;

    public List<ExamQuestion> listByExamId(Integer examId, boolean withCorrectAnswer) {
        List<ExamQuestion> questions = this.lambdaQuery()
                .eq(ExamQuestion::getExamId, examId)
                .orderByAsc(ExamQuestion::getSortOrder)
                .list();

        for (ExamQuestion q : questions) {
            List<ExamOption> options = optionService.lambdaQuery()
                    .eq(ExamOption::getQuestionId, q.getQuestionId())
                    .orderByAsc(ExamOption::getSortOrder)
                    .list();
            q.setOptions(options);

            if (withCorrectAnswer) {
                List<String> correctLabels = options.stream()
                        .filter(o -> o.getIsCorrect() == 1)
                        .map(ExamOption::getOptionLabel)
                        .sorted()
                        .collect(Collectors.toList());
                q.setCorrectAnswerLabels(correctLabels);
            }
        }

        questions.sort(Comparator.comparingInt(ExamQuestion::getSortOrder));
        return questions;
    }

    public boolean deleteByExamId(Integer examId) {
        List<ExamQuestion> questions = this.lambdaQuery()
                .eq(ExamQuestion::getExamId, examId)
                .list();
        for (ExamQuestion q : questions) {
            optionService.lambdaUpdate()
                    .eq(ExamOption::getQuestionId, q.getQuestionId())
                    .remove();
        }
        return this.lambdaUpdate()
                .eq(ExamQuestion::getExamId, examId)
                .remove();
    }
}
