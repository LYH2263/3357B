package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.PointDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PointDetailMapper extends BaseMapper<PointDetail> {

    @Select("SELECT COALESCE(SUM(point_value), 0) FROM point_detail WHERE student_id = #{studentId} AND rule_code = #{ruleCode} AND DATE(created_at) = CURDATE()")
    Integer getTodayPointsByRule(@Param("studentId") Integer studentId, @Param("ruleCode") String ruleCode);
}
