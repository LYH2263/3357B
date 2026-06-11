package com.school.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("exam_option")
public class ExamOption {
    @TableId(type = IdType.AUTO)
    private Integer optionId;
    private Integer questionId;
    private String optionLabel;
    private String optionText;
    private Integer isCorrect;
    private Integer sortOrder;
}
