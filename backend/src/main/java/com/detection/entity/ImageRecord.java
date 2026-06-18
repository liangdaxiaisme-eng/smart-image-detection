package com.detection.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("image_record")
public class ImageRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String fileName;
    private String originalName;
    private Long fileSize;
    private String filePath;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime uploadTime;
}
