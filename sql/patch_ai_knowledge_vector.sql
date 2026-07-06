-- AI 知识库：文件上传 + 向量索引扩展（执行于 star_pivot 库）
USE `star_pivot`;

-- 文档表扩展
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_knowledge_document' AND COLUMN_NAME = 'source_type'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `ai_knowledge_document`
     ADD COLUMN `source_type` varchar(16) NOT NULL DEFAULT ''TEXT'' COMMENT ''来源类型 TEXT/FILE'' AFTER `content`,
     ADD COLUMN `original_file_name` varchar(256) DEFAULT NULL COMMENT ''原始文件名'' AFTER `source_type`,
     ADD COLUMN `file_type` varchar(20) DEFAULT NULL COMMENT ''文件类型 PDF/DOCX/MD/TXT'' AFTER `original_file_name`,
     ADD COLUMN `file_size` bigint DEFAULT NULL COMMENT ''文件大小字节'' AFTER `file_type`,
     ADD COLUMN `object_name` varchar(512) DEFAULT NULL COMMENT ''OSS对象路径'' AFTER `file_size`,
     ADD COLUMN `index_status` varchar(20) NOT NULL DEFAULT ''DONE'' COMMENT ''索引状态 PENDING/PROCESSING/DONE/FAILED'' AFTER `chunk_count`,
     ADD COLUMN `error_msg` varchar(500) DEFAULT NULL COMMENT ''索引错误信息'' AFTER `index_status`,
     ADD COLUMN `doc_version` int NOT NULL DEFAULT 1 COMMENT ''文档版本号'' AFTER `error_msg`,
     ADD COLUMN `indexed_at` datetime DEFAULT NULL COMMENT ''最近索引完成时间'' AFTER `doc_version`,
     MODIFY COLUMN `content` mediumtext NOT NULL COMMENT ''文档正文（文本或解析后全文）''',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 分块表扩展
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_knowledge_chunk' AND COLUMN_NAME = 'embedding_json'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `ai_knowledge_chunk`
     ADD COLUMN `embedding_json` json DEFAULT NULL COMMENT ''向量嵌入 JSON 数组'' AFTER `content`,
     ADD COLUMN `page_num` int DEFAULT NULL COMMENT ''来源页码/节序号'' AFTER `embedding_json`,
     ADD COLUMN `section_title` varchar(256) DEFAULT NULL COMMENT ''章节标题'' AFTER `page_num`,
     ADD COLUMN `doc_version` int NOT NULL DEFAULT 1 COMMENT ''文档版本号'' AFTER `section_title`',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 异步索引任务
CREATE TABLE IF NOT EXISTS `ai_index_task` (
  `task_id`       bigint       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `doc_id`        bigint       NOT NULL COMMENT '文档ID',
  `task_type`     varchar(32)  NOT NULL DEFAULT 'INDEX_TEXT' COMMENT 'INDEX_TEXT / INDEX_FILE',
  `status`        varchar(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RUNNING/DONE/FAILED',
  `retry_count`   int          NOT NULL DEFAULT 0 COMMENT '重试次数',
  `error_msg`     varchar(500)          DEFAULT NULL COMMENT '错误信息',
  `started_at`    datetime              DEFAULT NULL COMMENT '开始时间',
  `finished_at`   datetime              DEFAULT NULL COMMENT '完成时间',
  `create_time`   datetime              DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`task_id`),
  KEY `idx_ai_index_task_doc` (`doc_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 知识库索引任务';
