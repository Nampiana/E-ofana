package com.eofana.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class PublicController {

    private final JdbcTemplate jdbcTemplate;

    public PublicController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/stats")
    public Map<String, Object> getStatsAccueil() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("formations", count("SELECT COUNT(*) FROM eofana.formations"));
        stats.put("centres", count("SELECT COUNT(*) FROM eofana.centres"));
        stats.put("apprenants", count("SELECT COUNT(*) FROM eofana.utilisateurs"));
        stats.put("regions", countRegions());

        return stats;
    }

    @GetMapping("/categories")
    public List<String> getCategoriesAccueil() {
        try {
            return jdbcTemplate.queryForList(
                    "SELECT nom FROM eofana.categories ORDER BY nom",
                    String.class
            );
        } catch (Exception e) {
            return List.of();
        }
    }

    private Integer count(String sql) {
        try {
            Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
            return result == null ? 0 : result;
        } catch (Exception e) {
            return 0;
        }
    }

    private Integer countRegions() {
        try {
            Integer result = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT ville) FROM eofana.centres WHERE ville IS NOT NULL AND ville <> ''",
                    Integer.class
            );
            return result == null ? 0 : result;
        } catch (Exception e) {
            return 0;
        }
    }
}