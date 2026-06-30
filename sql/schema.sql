-- ============================================
-- 智能图像检测管理系统 - 数据库初始化脚本
-- ============================================

CREATE DATABASE IF NOT EXISTS smart_detection DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_detection;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `user_id`     BIGINT       NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(30)  NOT NULL,
    `password`    VARCHAR(64)  NOT NULL,
    `role`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0-普通用户, 1-管理员',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 检测记录表
CREATE TABLE IF NOT EXISTS `detection_record` (
    `record_id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`           BIGINT       NOT NULL,
    `original_filename` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_path`         VARCHAR(500) NOT NULL COMMENT '存储路径',
    `file_type`         VARCHAR(10)  NOT NULL COMMENT 'image/video',
    `detection_count`   INT          NOT NULL DEFAULT 0 COMMENT '检测目标数量',
    `class_counts`      TEXT         COMMENT '各类别计数JSON',
    `result_image_path` VARCHAR(500) COMMENT '结果图片路径',
    `detections_json`   LONGTEXT     COMMENT '完整检测结果JSON',
    `elapsed_seconds`   DOUBLE       DEFAULT 0 COMMENT '检测耗时(秒)',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`record_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_file_type` (`file_type`),
    CONSTRAINT `fk_record_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检测记录表';

-- 默认管理员 (密码: admin123)
INSERT INTO `user` (`username`, `password`, `role`) VALUES
('admin', '$2a$10$N.ZOn9G6w3Fz4nFHRXn5GOe9Th2jKZqK7TAKpXv4pG1wFkBmvUYCi', 1);
