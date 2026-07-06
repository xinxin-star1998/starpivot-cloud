-- AI 会话与消息 MySQL 持久化 + 会话管理菜单（执行于 star_pivot 库）
USE `star_pivot`;

-- ----------------------------
-- ai_chat_session 会话表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_chat_session` (
  `session_id`       bigint       NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `conversation_id`  varchar(64)  NOT NULL COMMENT '会话标识 user-{userId}:{uuid}',
  `user_id`          bigint       NOT NULL COMMENT '用户ID',
  `title`            varchar(128) NOT NULL DEFAULT '新对话' COMMENT '会话标题',
  `message_count`    int          NOT NULL DEFAULT 0 COMMENT '消息条数',
  `del_flag`         char(1)      NOT NULL DEFAULT '0' COMMENT '删除标志（0存在 1删除）',
  `create_time`      datetime              DEFAULT NULL COMMENT '创建时间',
  `update_time`      datetime              DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`session_id`),
  UNIQUE KEY `uk_ai_chat_conversation` (`conversation_id`),
  KEY `idx_ai_chat_user_update` (`user_id`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 对话会话';

-- ----------------------------
-- ai_chat_message 消息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_chat_message` (
  `message_id`       bigint       NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `conversation_id`  varchar(64)  NOT NULL COMMENT '会话标识',
  `role`             varchar(20)  NOT NULL COMMENT 'USER / ASSISTANT',
  `content`          mediumtext   NOT NULL COMMENT '消息内容',
  `sort_order`       int          NOT NULL DEFAULT 0 COMMENT '排序序号',
  `create_time`      datetime              DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`message_id`),
  KEY `idx_ai_msg_conversation_sort` (`conversation_id`, `sort_order`),
  KEY `idx_ai_msg_conversation_time` (`conversation_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 对话消息';

-- ----------------------------
-- 会话管理菜单（menu_id 339+）
-- ----------------------------
INSERT INTO `sys_menu` VALUES (339, '会话管理', 334, 2, 'session', '/ai/session/index', NULL, 'AiSession', 1, 1, 'C', '0', '0', 'ai:session:query', 'ri:chat-history-line', 'admin', NOW(), '', NULL, 'AI 对话会话管理')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (340, '会话查询', 339, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:session:query', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (341, '会话删除', 339, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:session:delete', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 339), (1, 340), (1, 341);
