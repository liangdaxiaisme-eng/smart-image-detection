package com.detection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("detection_result")
public class DetectionResult {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long imageId;
    private Long userId;
    private String detections;
    private String statistics;
    private String resultImageUrl;
    private Double confidenceThreshold;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
