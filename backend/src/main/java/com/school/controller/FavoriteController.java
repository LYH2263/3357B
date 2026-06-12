package com.school.controller;

import com.school.entity.Favorite;
import com.school.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/favorite")
@CrossOrigin
public class FavoriteController {

    @Autowired private FavoriteService favoriteService;

    @PostMapping("/toggle")
    public Map<String, Object> toggle(@RequestBody Map<String, Object> params) {
        Integer studentId = (Integer) params.get("studentId");
        String studentName = (String) params.get("studentName");
        String resourceType = (String) params.get("resourceType");
        Integer resourceId = params.get("resourceId") != null ?
                ((Number) params.get("resourceId")).intValue() : null;

        boolean success = favoriteService.toggleFavorite(studentId, studentName, resourceType, resourceId);
        boolean isFavorited = favoriteService.isFavorited(studentId, resourceType, resourceId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("isFavorited", isFavorited);
        return result;
    }

    @GetMapping("/check")
    public Map<String, Object> check(
            @RequestParam Integer studentId,
            @RequestParam String resourceType,
            @RequestParam Integer resourceId) {
        boolean isFavorited = favoriteService.isFavorited(studentId, resourceType, resourceId);
        Map<String, Object> result = new HashMap<>();
        result.put("isFavorited", isFavorited);
        return result;
    }

    @GetMapping("/student/list")
    public List<Favorite> getStudentFavorites(
            @RequestParam Integer studentId,
            @RequestParam(required = false) String resourceType) {
        return favoriteService.getStudentFavorites(studentId, resourceType);
    }

    @GetMapping("/student/detail")
    public Map<String, Object> getStudentFavoritesWithDetail(@RequestParam Integer studentId) {
        return favoriteService.getStudentFavoritesWithDetail(studentId);
    }

    @GetMapping("/student/ids")
    public Set<String> getStudentFavoriteIds(@RequestParam Integer studentId) {
        return favoriteService.getStudentFavoriteSet(studentId);
    }

    @GetMapping("/ranking")
    public List<Map<String, Object>> getRanking(
            @RequestParam String resourceType,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return favoriteService.getRanking(resourceType, limit);
    }
}
