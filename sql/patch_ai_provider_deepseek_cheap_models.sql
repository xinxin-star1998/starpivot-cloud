-- 把 DeepSeek 便宜模型加进已有供应商，并把默认对话改为 deepseek-chat。
-- 改完后重启后端（或在「模型供应商」里打开 DeepSeek 保存一次）使运行时配置生效。

UPDATE `ai_provider`
SET `default_chat_model` = 'deepseek-chat',
    `models_json` = '[{"id":"deepseek-chat","label":"DeepSeek Chat（便宜）","kind":"chat"},{"id":"deepseek-reasoner","label":"DeepSeek Reasoner（推理）","kind":"chat"},{"id":"deepseek-v4-flash","label":"DeepSeek V4 Flash","kind":"chat"},{"id":"deepseek-v4-pro","label":"DeepSeek V4 Pro（较贵）","kind":"chat"}]',
    `remark` = 'OpenAI 兼容。日常默认 deepseek-chat；推理用 deepseek-reasoner。V4 更贵。'
WHERE `provider_code` = 'deepseek';
