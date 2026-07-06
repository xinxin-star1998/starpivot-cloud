-- 开启 RAG（需 Nacos starpivot.ai.rag.enabled=true 同时生效）
-- HyDE / Reranker 由 Nacos starpivot-ai.yaml 控制
USE `star_pivot`;

UPDATE `ai_config`
SET `rag_enabled` = '0',
    `rag_top_k`   = 5,
    `update_time` = NOW(),
    `update_by`   = 'admin'
WHERE `config_name` = 'default'
  AND `status` = '0';
