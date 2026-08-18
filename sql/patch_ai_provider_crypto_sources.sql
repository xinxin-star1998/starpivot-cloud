-- AI：API Key 落库加密列宽 + 消息引用持久化
-- 若 sources_json 已存在可忽略对应报错后继续执行第二段。

ALTER TABLE `ai_chat_message`
  ADD COLUMN `sources_json` mediumtext NULL COMMENT '助手引用资料 JSON' AFTER `content`;

ALTER TABLE `ai_provider`
  MODIFY COLUMN `api_key` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT 'API Key（ENC:v1 密文或历史明文）';
