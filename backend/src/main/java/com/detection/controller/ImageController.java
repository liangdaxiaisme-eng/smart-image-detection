package com.detection.controller;

import com.detection.common.Result;
import com.detection.entity.ImageRecord;
import com.detection.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Autowired
    private ImageService imageService;

    @PostMapping("/upload")
    public Result<?> upload(@RequestParam("file") MultipartFile file,
                            @RequestParam("userId") Long userId) {
        if (file.isEmpty()) return Result.error("文件为空");
        Map<String, Object> result = imageService.upload(file, userId);
        return Result.success(result);
    }

    @GetMapping("/list")
    public Result<List<ImageRecord>> list(@RequestParam Long userId) {
        return Result.success(imageService.getHistory(userId));
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        imageService.deleteRecord(id);
        return Result.success("删除成功");
    }
}
