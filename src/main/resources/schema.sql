-- 连音成歌数据库表结构

CREATE TABLE IF NOT EXISTS `songs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(255) NOT NULL COMMENT '歌名',
    `melody` VARCHAR(100) NOT NULL COMMENT '简谱旋律',
    `audio_data` MEDIUMBLOB NOT NULL COMMENT '音频二进制数据',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_melody` (`melody`) COMMENT '旋律索引，用于快速查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='歌曲表';
