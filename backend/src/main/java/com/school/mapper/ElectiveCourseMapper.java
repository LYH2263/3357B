package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.ElectiveCourse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ElectiveCourseMapper extends BaseMapper<ElectiveCourse> {

    @Update("UPDATE elective_course SET enrolled_count = enrolled_count + 1 " +
            "WHERE course_id = #{courseId} AND enrolled_count < capacity AND status = 'OPEN'")
    int incrementEnrolledCount(@Param("courseId") Integer courseId);

    @Update("UPDATE elective_course SET enrolled_count = enrolled_count - 1 " +
            "WHERE course_id = #{courseId} AND enrolled_count > 0")
    int decrementEnrolledCount(@Param("courseId") Integer courseId);
}
