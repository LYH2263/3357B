package com.school.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.entity.LearningProgress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface LearningProgressMapper extends BaseMapper<LearningProgress> {

    @Select("SELECT COUNT(*) FROM course")
    int getTotalCourseCount();

    @Select("SELECT COUNT(*) FROM learning_progress WHERE student_id = #{studentId} AND is_completed = 1")
    int getCompletedCount(@Param("studentId") Integer studentId);

    @Select("SELECT lp.*, c.ctitle AS course_title FROM learning_progress lp " +
            "RIGHT JOIN course c ON lp.course_id = c.cid AND lp.student_id = #{studentId} " +
            "ORDER BY c.cid")
    List<Map<String, Object>> getStudentProgressWithCourses(@Param("studentId") Integer studentId);

    @Select("SELECT c.cid AS courseId, c.ctitle AS courseTitle, " +
            "COALESCE(lp.is_completed, 0) AS isCompleted, " +
            "lp.completed_at AS completedAt, " +
            "c.ccontent AS courseContent, c.efile AS efile " +
            "FROM course c " +
            "LEFT JOIN learning_progress lp ON c.cid = lp.course_id AND lp.student_id = #{studentId} " +
            "ORDER BY c.cid")
    List<Map<String, Object>> getAllCoursesWithProgress(@Param("studentId") Integer studentId);

    @Select("SELECT course_id, course_title, completed_at FROM learning_progress " +
            "WHERE student_id = #{studentId} AND is_completed = 1 " +
            "ORDER BY completed_at DESC")
    List<Map<String, Object>> getCompletedTimeline(@Param("studentId") Integer studentId);

    @Select("SELECT u.uid AS studentId, u.username AS studentName, u.userno AS studentNo, " +
            "u.class_id AS classId, u.classname AS className, " +
            "COALESCE(completed.cnt, 0) AS completedCount " +
            "FROM user u " +
            "LEFT JOIN (SELECT student_id, COUNT(*) AS cnt FROM learning_progress WHERE is_completed = 1 GROUP BY student_id) completed " +
            "ON u.uid = completed.student_id " +
            "WHERE u.class_id = #{classId} " +
            "ORDER BY completedCount DESC")
    List<Map<String, Object>> getClassStudentProgress(@Param("classId") Integer classId);

    @Select("SELECT c.cid AS courseId, c.ctitle AS courseTitle, " +
            "COALESCE(cnt.completed_count, 0) AS completedCount " +
            "FROM course c " +
            "LEFT JOIN (SELECT course_id, COUNT(*) AS completed_count FROM learning_progress WHERE is_completed = 1 GROUP BY course_id) cnt " +
            "ON c.cid = cnt.course_id " +
            "ORDER BY completedCount DESC, c.cid")
    List<Map<String, Object>> getCourseHeatRanking();

    @Select("SELECT u.class_id AS classId, u.classname AS className, " +
            "COUNT(DISTINCT u.uid) AS totalStudents, " +
            "COALESCE(SUM(CASE WHEN lp.is_completed = 1 THEN 1 ELSE 0 END), 0) AS totalCompleted " +
            "FROM user u " +
            "LEFT JOIN learning_progress lp ON u.uid = lp.student_id " +
            "WHERE u.class_id IS NOT NULL " +
            "GROUP BY u.class_id, u.classname " +
            "ORDER BY u.class_id")
    List<Map<String, Object>> getClassAverageProgress();
}
