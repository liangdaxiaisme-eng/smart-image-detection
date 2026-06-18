package com.detection.controller;

import com.detection.common.Result;
import com.detection.service.DetectionService;
import com.detection.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    @Autowired
    private DetectionService detectionService;

    @Autowired
    private ImageService imageService;

    @GetMapping("/overview")
    public Result<?> overview() {
        Map<String, Object> detectStats = detectionService.getOverviewStats();
        Map<String, Object> imageStats = imageService.getStats();
        detectStats.putAll(imageStats);
        return Result.success(detectStats);
    }

    @GetMapping("/daily")
    public Result<List<Map<String, Object>>> daily() {
        return Result.success(detectionService.getDailyStats());
    }

    @GetMapping("/category")
    public Result<?> category() {
        Map<String, Object> stats = detectionService.getOverviewStats();
        return Result.success(stats.get("categoryStats"));
    }
}
