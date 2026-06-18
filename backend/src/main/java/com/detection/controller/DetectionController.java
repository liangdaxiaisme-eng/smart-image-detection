package com.detection.controller;

import com.detection.common.Result;
import com.detection.service.DetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/detect")
public class DetectionController {

    @Autowired
    private DetectionService detectionService;

    @PostMapping("/{imageId}")
    public Result<?> detect(@PathVariable Long imageId,
                            @RequestParam(defaultValue = "0.25") Double threshold) {
        try {
            Map<String, Object> result = detectionService.detect(imageId, threshold);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("检测失败：" + e.getMessage());
        }
    }

    @GetMapping("/results")
    public Result<List<Map<String, Object>>> results(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer limit) {
        return Result.success(detectionService.getResults(userId, limit));
    }

    @GetMapping("/{id}")
    public Result<?> getResult(@PathVariable Long id) {
        return Result.success(detectionService.getResultById(id));
    }
}
