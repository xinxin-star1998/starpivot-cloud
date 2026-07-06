-- AI 基础配置表 + 菜单权限（执行于 star_pivot 库）
USE `star_pivot`;

-- ----------------------------
-- ai_config 表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `ai_config` (
  `config_id`      bigint       NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_name`    varchar(64)  NOT NULL COMMENT '配置名称',
  `bot_name`       varchar(64)  NOT NULL DEFAULT 'AI 助手' COMMENT '助手名称',
  `bot_avatar`     varchar(512)          DEFAULT NULL COMMENT '助手头像URL',
  `welcome_message` text                 DEFAULT NULL COMMENT '欢迎语模板，支持 {botName}',
  `system_prompt`  text                 DEFAULT NULL COMMENT '系统提示词',
  `default_model`  varchar(64)           DEFAULT 'deepseek-chat' COMMENT '默认模型',
  `default_temperature` decimal(3,2)    DEFAULT 0.70 COMMENT '默认温度',
  `max_memory_messages` int              DEFAULT 30 COMMENT '会话记忆条数',
  `models_json`    text                  DEFAULT NULL COMMENT '可选模型 JSON [{id,label}]',
  `is_default`     char(1)      NOT NULL DEFAULT '0' COMMENT '是否默认（0是 1否）',
  `status`         char(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark`         varchar(500)          DEFAULT NULL COMMENT '备注',
  `create_by`      varchar(64)           DEFAULT '' COMMENT '创建者',
  `create_time`    datetime              DEFAULT NULL COMMENT '创建时间',
  `update_by`      varchar(64)           DEFAULT '' COMMENT '更新者',
  `update_time`    datetime              DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_ai_config_name` (`config_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI 基础配置';

-- 默认配置（与 Nacos 初始值对齐）
INSERT INTO `ai_config` (
  `config_name`, `bot_name`, `welcome_message`, `system_prompt`,
  `default_model`, `default_temperature`, `max_memory_messages`, `models_json`,
  `is_default`, `status`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`
) VALUES (
  'default',
  'AI 助手',
  '你好！我是 **{botName}**，可以帮你写作、翻译、编程、分析等各类问题，有什么我可以帮你的吗？',
  '你是一个智能 AI 助手，能够进行自然、流畅的多轮对话。\n你可以帮助用户完成写作、翻译、编程、分析、头脑风暴、学习辅导等广泛任务。\n请根据用户问题选择合适的方式回答：简洁问题简洁答，复杂问题条理清晰。\n使用 Markdown 格式化代码、列表和重点内容；不确定时请诚实说明，不要编造。',
  'deepseek-chat',
  0.70,
  30,
  '[{"id":"deepseek-chat","label":"DeepSeek Chat"},{"id":"deepseek-reasoner","label":"DeepSeek Reasoner"}]',
  '0',
  '0',
  'admin',
  NOW(),
  'admin',
  NOW(),
  '系统默认 AI 配置'
) ON DUPLICATE KEY UPDATE `update_time` = NOW();

-- ----------------------------
-- AI 中心菜单（menu_id 334+）
-- ----------------------------
INSERT INTO `sys_menu` VALUES (334, 'AI 中心', 0, 8, '/ai', '', NULL, 'AiCenter', 1, 1, 'M', '0', '0', '', 'ri:robot-2-line', 'admin', NOW(), '', NULL, 'AI 智能对话管理')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (335, '基础配置', 334, 1, 'config', '/ai/config/index', NULL, 'AiConfig', 1, 1, 'C', '0', '0', 'ai:config:query', 'ep:setting', 'admin', NOW(), '', NULL, 'AI 助手基础配置')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (336, '配置查询', 335, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:config:query', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (337, '配置编辑', 335, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:config:edit', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (338, '配置删除', 335, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:config:delete', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- admin 角色绑定 AI 菜单
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 334), (1, 335), (1, 336), (1, 337), (1, 338);
