package com.detection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.detection.entity.ImageRecord;
import com.detection.entity.DetectionResult;
import com.detection.mapper.ImageRecordMapper;
import com.detection.mapper.DetectionResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ImageService extends ServiceImpl<ImageRecordMapper, ImageRecord> {

    @Value("${upload.path:${user.dir}/upload}")
    private String uploadPath;

    @Autowired
    private DetectionResultMapper detectionResultMapper;

    public Map<String, Object> upload(MultipartFile file, Long userId) {
        try {
            String originalName = file.getOriginalFilename();
            String ext = originalName.substring(originalName.lastIndexOf("."));
            String fileName = UUID.randomUUID() + ext;

            File dir = new File(uploadPath + "/images");
            if (!dir.exists()) dir.mkdirs();
            File dest = new File(dir, fileName);
            file.transferTo(dest);

            ImageRecord record = new ImageRecord();
            record.setUserId(userId);
            record.setFileName(fileName);
            record.setOriginalName(originalName);
            record.setFileSize(file.getSize());
            record.setFilePath("/upload/images/" + fileName);
            save(record);

            Map<String, Object> result = new HashMap<>();
            result.put("imageId", record.getId());
            result.put("fileName", fileName);
            result.put("url", "/upload/images/" + fileName);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    public List<ImageRecord> getHistory(Long userId) {
        LambdaQueryWrapper<ImageRecord> q = new LambdaQueryWrapper<>();
        q.eq(ImageRecord::getUserId, userId);
        q.orderByDesc(ImageRecord::getUploadTime);
        return list(q);
    }

    public void deleteRecord(Long id) {
        removeById(id);
    }

    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalImages = count();
        stats.put("totalImages", totalImages);

        // 今日上传
        LambdaQueryWrapper<ImageRecord> todayQ = new LambdaQueryWrapper<>();
        todayQ.apply("DATE(upload_time) = CURDATE()");
        stats.put("todayImages", count(todayQ));

        // 今日检测
        LambdaQueryWrapper<DetectionResult> todayDetect = new LambdaQueryWrapper<>();
        todayDetect.apply("DATE(create_time) = CURDATE()");
        stats.put("todayDetections", detectionResultMapper.selectCount(todayDetect));

        return stats;
    }
}
