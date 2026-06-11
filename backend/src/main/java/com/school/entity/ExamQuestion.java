package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("exam_question")
public class ExamQuestion {
    @TableId(type = IdType.AUTO)
    private Integer questionId;
    private Integer examId;
    private String questionType;
    private String questionText;
    private BigDecimal score;
    private Integer sortOrder;
    private String analysis;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private List<ExamOption> options;

    @TableField(exist = false)
    private List<String> correctAnswerLabels;

    @TableField(exist = false)
    private List<String> studentAnswers;

    @TableField(exist = false)
    private Boolean isCorrect;

    @TableField(exist = false)
    private BigDecimal actualScore;
}
