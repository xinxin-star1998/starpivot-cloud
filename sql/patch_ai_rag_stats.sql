-- AI 知识库 RAG + 用量统计（执行于 star_pivot 库）
USE `star_pivot`;

-- ----------------------------
-- 知识库
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_knowledge_base` (
  `kb_id`        bigint       NOT NULL AUTO_INCREMENT COMMENT '知识库ID',
  `kb_name`      varchar(128) NOT NULL COMMENT '知识库名称',
  `description`  varchar(500)          DEFAULT NULL COMMENT '描述',
  `top_k`        int          NOT NULL DEFAULT 5 COMMENT '检索返回条数',
  `chunk_size`   int          NOT NULL DEFAULT 600 COMMENT '分块大小（字符）',
  `chunk_overlap` int         NOT NULL DEFAULT 80 COMMENT '分块重叠（字符）',
  `status`       char(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag`     char(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_by`    varchar(64)           DEFAULT '' COMMENT '创建者',
  `create_time`  datetime              DEFAULT NULL COMMENT '创建时间',
  `update_by`    varchar(64)           DEFAULT '' COMMENT '更新者',
  `update_time`  datetime              DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`kb_id`),
  KEY `idx_ai_kb_status` (`status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 知识库';

CREATE TABLE IF NOT EXISTS `ai_knowledge_document` (
  `doc_id`       bigint       NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `kb_id`        bigint       NOT NULL COMMENT '知识库ID',
  `title`        varchar(256) NOT NULL COMMENT '文档标题',
  `content`      mediumtext   NOT NULL COMMENT '文档正文',
  `chunk_count`  int          NOT NULL DEFAULT 0 COMMENT '分块数量',
  `status`       char(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag`     char(1)      NOT NULL DEFAULT '0' COMMENT '删除标志',
  `create_by`    varchar(64)           DEFAULT '' COMMENT '创建者',
  `create_time`  datetime              DEFAULT NULL COMMENT '创建时间',
  `update_by`    varchar(64)           DEFAULT '' COMMENT '更新者',
  `update_time`  datetime              DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`doc_id`),
  KEY `idx_ai_doc_kb` (`kb_id`, `status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 知识库文档';

CREATE TABLE IF NOT EXISTS `ai_knowledge_chunk` (
  `chunk_id`     bigint       NOT NULL AUTO_INCREMENT COMMENT '分块ID',
  `doc_id`       bigint       NOT NULL COMMENT '文档ID',
  `kb_id`        bigint       NOT NULL COMMENT '知识库ID',
  `chunk_index`  int          NOT NULL DEFAULT 0 COMMENT '分块序号',
  `content`      text         NOT NULL COMMENT '分块内容',
  `create_time`  datetime              DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`chunk_id`),
  KEY `idx_ai_chunk_doc` (`doc_id`, `chunk_index`),
  KEY `idx_ai_chunk_kb` (`kb_id`),
  FULLTEXT KEY `ft_ai_chunk_content` (`content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 知识库分块';

-- ----------------------------
-- 用量统计
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_usage_log` (
  `log_id`            bigint       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id`           bigint                DEFAULT NULL COMMENT '用户ID',
  `conversation_id`   varchar(64)           DEFAULT NULL COMMENT '会话ID',
  `model`             varchar(64)           DEFAULT NULL COMMENT '模型',
  `request_type`      varchar(16)  NOT NULL COMMENT 'SEND / STREAM',
  `prompt_tokens`     int          NOT NULL DEFAULT 0 COMMENT '输入 tokens',
  `completion_tokens` int          NOT NULL DEFAULT 0 COMMENT '输出 tokens',
  `total_tokens`      int          NOT NULL DEFAULT 0 COMMENT '总 tokens',
  `latency_ms`        bigint       NOT NULL DEFAULT 0 COMMENT '耗时毫秒',
  `success`           char(1)      NOT NULL DEFAULT '0' COMMENT '是否成功（0是 1否）',
  `error_message`     varchar(500)          DEFAULT NULL COMMENT '错误信息',
  `create_time`       datetime              DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_ai_usage_user_time` (`user_id`, `create_time`),
  KEY `idx_ai_usage_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 调用用量日志';

-- 默认知识库（幂等：已存在则跳过）
INSERT INTO `ai_knowledge_base` (
  `kb_name`, `description`, `top_k`, `chunk_size`, `chunk_overlap`,
  `status`, `create_by`, `create_time`, `update_by`, `update_time`
)
SELECT
  '默认知识库',
  '系统默认知识库，可在后台维护文档并用于 RAG 检索',
  5, 600, 80, '0', 'admin', NOW(), 'admin', NOW()
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `ai_knowledge_base`
  WHERE `kb_name` = '默认知识库' AND `del_flag` = '0'
);

-- ai_config 增加 RAG 开关（列已存在则跳过）
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'ai_config' AND COLUMN_NAME = 'rag_enabled'
);
SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE `ai_config` ADD COLUMN `rag_enabled` char(1) NOT NULL DEFAULT ''1'' COMMENT ''是否启用RAG（0是 1否）'' AFTER `models_json`, ADD COLUMN `rag_top_k` int NOT NULL DEFAULT 5 COMMENT ''RAG检索条数'' AFTER `rag_enabled`',
  'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 默认关闭 RAG（DeepSeek 等对话模型通常不提供 Embedding API，需在 Nacos 与后台同时开启）
UPDATE `ai_config` SET `rag_enabled` = '1', `rag_top_k` = 5 WHERE `config_name` = 'default';

-- ----------------------------
-- 菜单（menu_id 342+）
-- ----------------------------
INSERT INTO `sys_menu` VALUES (342, '知识库', 334, 3, 'knowledge', '/ai/knowledge/index', NULL, 'AiKnowledge', 1, 1, 'C', '0', '0', 'ai:knowledge:query', 'ri:book-open-line', 'admin', NOW(), '', NULL, 'AI 知识库管理')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (343, '知识库查询', 342, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:knowledge:query', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (344, '知识库编辑', 342, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:knowledge:edit', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (345, '知识库删除', 342, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:knowledge:delete', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (346, '用量统计', 334, 4, 'statistics', '/ai/statistics/index', NULL, 'AiStatistics', 1, 1, 'C', '0', '0', 'ai:statistics:query', 'ri:bar-chart-box-line', 'admin', NOW(), '', NULL, 'AI 调用用量统计')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (347, '统计查询', 346, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:statistics:query', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 342), (1, 343), (1, 344), (1, 345), (1, 346), (1, 347);
