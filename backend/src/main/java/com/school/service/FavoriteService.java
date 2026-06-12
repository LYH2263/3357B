package com.school.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.school.entity.Course;
import com.school.entity.Experiment;
import com.school.entity.Favorite;
import com.school.mapper.CourseMapper;
import com.school.mapper.ExperimentMapper;
import com.school.mapper.FavoriteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FavoriteService extends ServiceImpl<FavoriteMapper, Favorite> {

    @Autowired private CourseMapper courseMapper;
    @Autowired private ExperimentMapper experimentMapper;

    @Transactional
    public boolean toggleFavorite(Integer studentId, String studentName, String resourceType, Integer resourceId) {
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId)
               .eq("resource_type", resourceType)
               .eq("resource_id", resourceId);
        Favorite existing = getOne(wrapper);

        if (existing != null) {
            return removeById(existing.getFavoriteId());
        } else {
            String resourceTitle = getResourceTitle(resourceType, resourceId);
            if (resourceTitle == null) {
                return false;
            }
            Favorite fav = new Favorite();
            fav.setStudentId(studentId);
            fav.setStudentName(studentName);
            fav.setResourceType(resourceType);
            fav.setResourceId(resourceId);
            fav.setResourceTitle(resourceTitle);
            return save(fav);
        }
    }

    public boolean isFavorited(Integer studentId, String resourceType, Integer resourceId) {
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId)
               .eq("resource_type", resourceType)
               .eq("resource_id", resourceId);
        return count(wrapper) > 0;
    }

    public List<Favorite> getStudentFavorites(Integer studentId, String resourceType) {
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId);
        if (resourceType != null && !resourceType.isEmpty()) {
            wrapper.eq("resource_type", resourceType);
        }
        wrapper.orderByDesc("created_at");
        return list(wrapper);
    }

    public Map<String, Object> getStudentFavoritesWithDetail(Integer studentId) {
        List<Favorite> allFavs = getStudentFavorites(studentId, null);

        List<Map<String, Object>> courseFavs = new ArrayList<>();
        List<Map<String, Object>> experimentFavs = new ArrayList<>();

        Set<Integer> courseIds = new HashSet<>();
        Set<Integer> experimentIds = new HashSet<>();

        for (Favorite fav : allFavs) {
            if ("COURSE".equals(fav.getResourceType())) {
                courseIds.add(fav.getResourceId());
            } else if ("EXPERIMENT".equals(fav.getResourceType())) {
                experimentIds.add(fav.getResourceId());
            }
        }

        Map<Integer, Course> courseMap = new HashMap<>();
        if (!courseIds.isEmpty()) {
            List<Course> courses = courseMapper.selectBatchIds(courseIds);
            for (Course c : courses) {
                courseMap.put(c.getCid(), c);
            }
        }

        Map<Integer, Experiment> experimentMap = new HashMap<>();
        if (!experimentIds.isEmpty()) {
            List<Experiment> experiments = experimentMapper.selectBatchIds(experimentIds);
            for (Experiment e : experiments) {
                experimentMap.put(e.getEid(), e);
            }
        }

        for (Favorite fav : allFavs) {
            Map<String, Object> item = new HashMap<>();
            item.put("favoriteId", fav.getFavoriteId());
            item.put("resourceType", fav.getResourceType());
            item.put("resourceId", fav.getResourceId());
            item.put("resourceTitle", fav.getResourceTitle());
            item.put("favoriteTime", fav.getCreatedAt());
            item.put("isDeleted", false);

            if ("COURSE".equals(fav.getResourceType())) {
                Course c = courseMap.get(fav.getResourceId());
                if (c != null) {
                    item.put("content", c.getCcontent());
                    item.put("file", c.getEfile());
                } else {
                    item.put("isDeleted", true);
                    item.put("content", "该资源已被删除");
                    item.put("file", null);
                }
                courseFavs.add(item);
            } else if ("EXPERIMENT".equals(fav.getResourceType())) {
                Experiment e = experimentMap.get(fav.getResourceId());
                if (e != null) {
                    item.put("content", e.getEcontent());
                    item.put("file", e.getEfile());
                } else {
                    item.put("isDeleted", true);
                    item.put("content", "该资源已被删除");
                    item.put("file", null);
                }
                experimentFavs.add(item);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("courses", courseFavs);
        result.put("experiments", experimentFavs);
        return result;
    }

    public Set<String> getStudentFavoriteSet(Integer studentId) {
        List<Map<String, Object>> list = baseMapper.getFavoriteIdsByStudent(studentId);
        return list.stream()
                .map(m -> m.get("resourceType") + ":" + m.get("resourceId"))
                .collect(Collectors.toSet());
    }

    public List<Map<String, Object>> getRanking(String resourceType, Integer limit) {
        if (limit == null || limit <= 0) limit = 10;
        return baseMapper.getFavoriteRanking(resourceType, limit);
    }

    private String getResourceTitle(String resourceType, Integer resourceId) {
        if ("COURSE".equals(resourceType)) {
            Course course = courseMapper.selectById(resourceId);
            return course != null ? course.getCtitle() : null;
        } else if ("EXPERIMENT".equals(resourceType)) {
            Experiment experiment = experimentMapper.selectById(resourceId);
            return experiment != null ? experiment.getEtitle() : null;
        }
        return null;
    }
}
