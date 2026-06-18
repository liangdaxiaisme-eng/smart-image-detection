package com.detection.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.detection.entity.DetectionResult;
import com.detection.entity.ImageRecord;
import com.detection.mapper.DetectionResultMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class DetectionService extends ServiceImpl<DetectionResultMapper, DetectionResult> {

    @Value("${ai.service.url:http://localhost:5000}")
    private String aiServiceUrl;

    @Autowired
    private ImageService imageService;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> detect(Long imageId, Double threshold) throws Exception {
        ImageRecord image = imageService.getById(imageId);
        if (image == null) throw new RuntimeException("图片不存在");

        String imagePath = System.getProperty("user.dir") + image.getFilePath();

        // 调用YOLO服务
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        FileSystemResource fileResource = new FileSystemResource(imagePath);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileResource);
        body.add("conf_threshold", String.valueOf(threshold != null ? threshold : 0.25));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                aiServiceUrl + "/detect",
                HttpMethod.POST,
                requestEntity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || !responseBody.containsKey("data")) {
            throw new RuntimeException("AI服务响应异常");
        }

        Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
        String resultImageUrl = (String) data.get("result_image_url");

        // 保存检测结果
        DetectionResult result = new DetectionResult();
        result.setImageId(imageId);
        result.setUserId(image.getUserId());
        result.setDetections(toJson(data.get("detections")));
        result.setStatistics(toJson(data.get("statistics")));
        result.setResultImageUrl(resultImageUrl);
        result.setConfidenceThreshold(threshold != null ? threshold : 0.25);
        save(result);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("resultId", result.getId());
        resultMap.put("detections", data.get("detections"));
        resultMap.put("statistics", data.get("statistics"));
        resultMap.put("resultImageUrl", resultImageUrl);
        return resultMap;
    }

    public List<Map<String, Object>> getResults(Long userId, Integer limit) {
        LambdaQueryWrapper<DetectionResult> q = new LambdaQueryWrapper<>();
        if (userId != null) q.eq(DetectionResult::getUserId, userId);
        q.orderByDesc(DetectionResult::getCreateTime);
        if (limit != null) q.last("LIMIT " + limit);

        List<DetectionResult> list = list(q);
        List<Map<String, Object>> result = new ArrayList<>();
        for (DetectionResult dr : list) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", dr.getId());
            m.put("imageId", dr.getImageId());
            m.put("resultImageUrl", dr.getResultImageUrl());
            m.put("statistics", parseJson(dr.getStatistics()));
            m.put("createTime", dr.getCreateTime());

            ImageRecord img = imageService.getById(dr.getImageId());
            m.put("originalName", img != null ? img.getOriginalName() : "未知");
            result.add(m);
        }
        return result;
    }

    public DetectionResult getResultById(Long id) {
        return getById(id);
    }

    public Map<String, Object> getOverviewStats() {
        Map<String, Object> stats = new HashMap<>();
        long totalDetections = count();
        stats.put("totalDetections", totalDetections);

        // 今日检测
        LambdaQueryWrapper<DetectionResult> todayQ = new LambdaQueryWrapper<>();
        todayQ.apply("DATE(create_time) = CURDATE()");
        stats.put("todayDetections", count(todayQ));

        // 累计识别目标数（统计所有statistics的total）
        List<DetectionResult> all = list();
        int totalObjects = 0;
        Map<String, Integer> categoryCount = new HashMap<>();
        for (DetectionResult dr : all) {
            Map<String, Object> stat = parseJson(dr.getStatistics());
            if (stat != null && stat.containsKey("total")) {
                totalObjects += ((Number) stat.get("total")).intValue();
            }
            if (stat != null && stat.containsKey("per_class")) {
                List<Map<String, Object>> perClass = (List<Map<String, Object>>) stat.get("per_class");
                if (perClass != null) {
                    for (Map<String, Object> pc : perClass) {
                        String name = (String) pc.get("class_name");
                        int count = ((Number) pc.get("count")).intValue();
                        categoryCount.merge(name, count, Integer::sum);
                    }
                }
            }
        }
        stats.put("totalObjects", totalObjects);

        List<Map<String, Object>> categoryList = new ArrayList<>();
        for (Map.Entry<String, Integer> e : categoryCount.entrySet()) {
            Map<String, Object> c = new HashMap<>();
            c.put("class_name", e.getKey());
            c.put("count", e.getValue());
            categoryList.add(c);
        }
        stats.put("categoryStats", categoryList);

        return stats;
    }

    public List<Map<String, Object>> getDailyStats() {
        List<Map<String, Object>> dailyStats = new ArrayList<>();
        // 最近7天
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            LambdaQueryWrapper<DetectionResult> q = new LambdaQueryWrapper<>();
            q.apply("DATE(create_time) = {0}", date);
            long count = count(q);

            Map<String, Object> day = new HashMap<>();
            day.put("date", date.format(DateTimeFormatter.ofPattern("MM-dd")));
            day.put("count", count);
            dailyStats.add(day);
        }
        return dailyStats;
    }

    private String toJson(Object obj) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, Object> parseJson(String json) {
        try {
            if (json == null) return new HashMap<>();
            return new com.fasterxml.jackson.databind.ObjectMapper().readValue(json, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
