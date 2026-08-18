-- AI 模型供应商：网页配置 DeepSeek / Kimi / 阿里百炼 等 API，不再只能改 YAML。
-- 可重复执行。不写入任何 API Key。
SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS `ai_provider` (
  `provider_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '供应商ID',
  `provider_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'deepseek/kimi/dashscope/openai/zhipu/siliconflow/custom',
  `provider_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '显示名称',
  `base_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OpenAI 兼容地址，不要带 /v1',
  `api_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'API Key',
  `completions_path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '对话路径，空则 /v1/chat/completions',
  `embeddings_path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '向量路径，空则 /v1/embeddings',
  `rerank_endpoint` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '重排完整地址（百炼等）',
  `chat_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '对话能力（0是 1否）',
  `embedding_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '向量能力（0是 1否）',
  `rerank_enabled` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '重排能力（0是 1否）',
  `default_chat_model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '默认对话模型',
  `default_embedding_model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '默认向量模型',
  `default_rerank_model` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '默认重排模型',
  `models_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '模型列表 JSON [{id,label,kind}]',
  `is_default_chat` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '默认对话供应商（0是 1否）',
  `is_default_embedding` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '默认向量供应商（0是 1否）',
  `is_default_rerank` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '1' COMMENT '默认重排供应商（0是 1否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`provider_id`) USING BTREE,
  UNIQUE INDEX `uk_ai_provider_name`(`provider_name`) USING BTREE,
  INDEX `idx_ai_provider_code`(`provider_code`) USING BTREE,
  INDEX `idx_ai_provider_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI 模型供应商' ROW_FORMAT = Dynamic;

-- 菜单 ID：348-351（sys_menu AUTO_INCREMENT=348）。勿用 140-143，那是商城「销售属性/SPU/发布商品/商品管理」。
-- 父菜单：334 = AI 中心。勿用 116，那是「文件夹编辑」。
-- 按权限标识清理，避免误删商城菜单。
DELETE rm FROM sys_role_menu rm
INNER JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.component = '/ai/provider/index'
   OR m.perms IN ('ai:provider:query', 'ai:provider:edit', 'ai:provider:delete');

DELETE i FROM sys_i18n i
INNER JOIN sys_menu m ON i.namespace = 'menu' AND i.resource_key = CAST(m.menu_id AS CHAR)
WHERE m.component = '/ai/provider/index'
   OR m.perms IN ('ai:provider:query', 'ai:provider:edit', 'ai:provider:delete');

DELETE FROM sys_menu
WHERE perms IN ('ai:provider:query', 'ai:provider:edit', 'ai:provider:delete');

-- 旧补丁若已覆盖 140-143，补回商城菜单（已存在则跳过）
INSERT INTO `sys_menu`
SELECT 140, '销售属性', 128, 3, 'sale', '/mall/pms/attr/sale/index', NULL, 'SalesAttributes', 1, 1, 'C', '0', '0', '', 'ep:present', 'admin', '2026-05-18 15:12:11', 'admin', '2026-06-23 21:14:16', ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 140);
INSERT INTO `sys_menu`
SELECT 141, 'spu管理', 129, 1, 'spu', '/mall/pms/product/spu/index', NULL, 'SpuManager', 1, 1, 'C', '0', '0', '', 'ri:shining-2-line', 'admin', '2026-06-23 16:40:46', 'admin', '2026-07-01 17:27:32', 'SPU 列表 + 分类树'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 141);
INSERT INTO `sys_menu`
SELECT 142, '发布商品', 129, 2, 'publish', '/mall/pms/product/publish/index', NULL, 'PublishSPU', 1, 1, 'C', '0', '0', '', 'heroicons-outline:annotation', 'admin', '2026-06-23 16:43:05', 'admin', '2026-07-01 17:27:32', '跳转 SPU 发布向导'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 142);
INSERT INTO `sys_menu`
SELECT 143, '商品管理', 129, 3, 'manager', '/mall/pms/product/manager/index', NULL, 'GoodsManager', 1, 1, 'C', '0', '0', 'mall:product:query', 'ep:apple', 'admin', '2026-06-23 16:44:11', 'admin', '2026-07-01 17:27:32', 'SKU 只读检索'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 143);

DELETE FROM sys_role_menu WHERE menu_id IN (348, 349, 350, 351);
DELETE FROM sys_i18n WHERE namespace = 'menu' AND resource_key IN ('348', '349', '350', '351');
DELETE FROM sys_menu WHERE menu_id IN (348, 349, 350, 351);

INSERT INTO `sys_menu` VALUES (348, '模型供应商', 334, 0, 'provider', '/ai/provider/index', NULL, 'AiProvider', 1, 1, 'C', '0', '0', 'ai:provider:query', 'ri:key-2-line', 'admin', NOW(), '', NULL, '网页配置 DeepSeek / Kimi / 百炼等 API');
INSERT INTO `sys_menu` VALUES (349, '供应商查询', 348, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:provider:query', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (350, '供应商编辑', 348, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:provider:edit', '#', 'admin', NOW(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (351, '供应商删除', 348, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:provider:delete', '#', 'admin', NOW(), '', NULL, '');

INSERT INTO `sys_i18n` (`namespace`, `resource_key`, `field_name`, `lang`, `content`, `create_by`, `create_time`) VALUES
('menu', '348', 'menu_name', 'zh', '模型供应商', 'admin', NOW()),
('menu', '348', 'menu_name', 'en', 'AI Providers', 'admin', NOW()),
('menu', '349', 'menu_name', 'zh', '供应商查询', 'admin', NOW()),
('menu', '349', 'menu_name', 'en', 'Provider Query', 'admin', NOW()),
('menu', '350', 'menu_name', 'zh', '供应商编辑', 'admin', NOW()),
('menu', '350', 'menu_name', 'en', 'Provider Edit', 'admin', NOW()),
('menu', '351', 'menu_name', 'zh', '供应商删除', 'admin', NOW()),
('menu', '351', 'menu_name', 'en', 'Provider Delete', 'admin', NOW());

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT rm.role_id, m.menu_id
FROM `sys_role_menu` rm
CROSS JOIN (SELECT 348 AS menu_id UNION ALL SELECT 349 UNION ALL SELECT 350 UNION ALL SELECT 351) m
WHERE rm.menu_id IN (335, 336, 337, 338)
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` exist
    WHERE exist.role_id = rm.role_id AND exist.menu_id = m.menu_id
  );

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, m.menu_id
FROM (SELECT 348 AS menu_id UNION ALL SELECT 349 UNION ALL SELECT 350 UNION ALL SELECT 351) m
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_menu` exist
    WHERE exist.role_id = 1 AND exist.menu_id = m.menu_id
);

-- 全局对话窗口 /ai/chat/* 需要 ai:chat:use。cloud 的「AI 中心」目录原先 perms 为空，已登录也会 403。
UPDATE `sys_menu`
SET `perms` = 'ai:chat:use',
    `remark` = 'AI 智能对话管理---使用全局 AI 对话窗口',
    `update_time` = NOW()
WHERE `menu_id` = 334;
