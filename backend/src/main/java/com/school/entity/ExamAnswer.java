package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("exam_answer")
public class ExamAnswer {
    @TableId(type = IdType.AUTO)
    private Integer answerId;
    private Integer attemptId;
    private Integer questionId;
    private String questionText;
    private String questionType;
    private BigDecimal score;
    private String studentAnswers;
    private String correctAnswers;
    private String optionSnapshot;
    private String analysis;
    private Integer isCorrect;
    private BigDecimal actualScore;
}
