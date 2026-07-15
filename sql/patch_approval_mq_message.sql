-- 已有 star_pivot_approval 库增量：审批完结 Outbox 表
-- 在 star_pivot_approval 库执行

CREATE TABLE IF NOT EXISTS `mq_message` (
  `message_id` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `to_exchange` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标交换机',
  `routing_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由键',
  `class_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息类型',
  `message_status` tinyint(0) NULL DEFAULT 0 COMMENT '0-新建 1-已发送 2-错误 3-已抵达 4-投递中',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_mq_status_time`(`message_status`, `create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '审批MQ本地消息表' ROW_FORMAT = Dynamic;
