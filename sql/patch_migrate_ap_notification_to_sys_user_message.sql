-- 历史审批通知 ap_notification → 统一消息中心 sys_user_message
--
-- 前置：已执行 sql/patch_sys_user_message.sql（创建 sys_user_message 表）
-- 数据源：star_pivot_approval.ap_notification（关联 ap_instance 补全业务跳转字段）
--
-- 说明：star_pivot 使用 utf8mb4_unicode_ci，审批库使用 utf8mb4_0900_ai_ci，
--       跨库字符串比较须显式 COLLATE，否则报错 1267。
--
-- 用法：
--   mysql -h127.0.0.1 -P3307 -uroot -proot < sql/patch_migrate_ap_notification_to_sys_user_message.sql

SET NAMES utf8mb4;

-- 确保目标表存在
CREATE TABLE IF NOT EXISTS `star_pivot`.`sys_user_message` (
  `message_id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `msg_type` varchar(32) NOT NULL COMMENT '消息类型',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` varchar(500) DEFAULT NULL COMMENT '内容摘要',
  `biz_module` varchar(32) DEFAULT NULL COMMENT '业务模块',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型',
  `biz_key` varchar(128) DEFAULT NULL COMMENT '业务唯一键',
  `biz_id` bigint DEFAULT NULL COMMENT '业务主键',
  `link_path` varchar(256) DEFAULT NULL COMMENT '前端路由',
  `read_flag` char(1) NOT NULL DEFAULT '0' COMMENT '0未读 1已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  KEY `idx_user_read_time` (`user_id`, `read_flag`, `create_time`) USING BTREE,
  KEY `idx_user_type_time` (`user_id`, `msg_type`, `create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户站内消息收件箱';

-- star_pivot_approval.ap_notification → star_pivot.sys_user_message
INSERT INTO `star_pivot`.`sys_user_message` (
  `user_id`,
  `msg_type`,
  `title`,
  `content`,
  `biz_module`,
  `biz_type`,
  `biz_key`,
  `biz_id`,
  `link_path`,
  `read_flag`,
  `create_time`
)
SELECT
  n.`user_id`,
  (CASE n.`notify_type` COLLATE utf8mb4_unicode_ci
    WHEN 'TASK_ASSIGNED' THEN 'APPROVAL_TASK'
    WHEN 'INSTANCE_FINISHED' THEN 'APPROVAL_RESULT'
    ELSE n.`notify_type`
  END) COLLATE utf8mb4_unicode_ci AS `msg_type`,
  LEFT(n.`title`, 200) COLLATE utf8mb4_unicode_ci AS `title`,
  LEFT(n.`content`, 500) COLLATE utf8mb4_unicode_ci AS `content`,
  COALESCE(i.`biz_module`, 'approval') COLLATE utf8mb4_unicode_ci AS `biz_module`,
  (COALESCE(i.`biz_type`, CASE n.`notify_type` COLLATE utf8mb4_unicode_ci
    WHEN 'TASK_ASSIGNED' THEN 'task_assigned'
    WHEN 'INSTANCE_FINISHED' THEN 'instance_finished'
    ELSE n.`notify_type`
  END)) COLLATE utf8mb4_unicode_ci AS `biz_type`,
  (COALESCE(i.`biz_key`, CONCAT('approval:instance:', n.`instance_id`))) COLLATE utf8mb4_unicode_ci AS `biz_key`,
  n.`instance_id` AS `biz_id`,
  (CASE n.`notify_type` COLLATE utf8mb4_unicode_ci
    WHEN 'TASK_ASSIGNED' THEN '/approval/todo'
    WHEN 'INSTANCE_FINISHED' THEN '/approval/mine'
    ELSE '/approval/todo'
  END) COLLATE utf8mb4_unicode_ci AS `link_path`,
  n.`read_flag` COLLATE utf8mb4_unicode_ci AS `read_flag`,
  n.`create_time`
FROM `star_pivot_approval`.`ap_notification` n
LEFT JOIN `star_pivot_approval`.`ap_instance` i ON i.`instance_id` = n.`instance_id`
WHERE NOT EXISTS (
  SELECT 1
  FROM `star_pivot`.`sys_user_message` m
  WHERE m.`user_id` = n.`user_id`
    AND m.`title` = LEFT(n.`title`, 200) COLLATE utf8mb4_unicode_ci
    AND (m.`create_time` <=> n.`create_time`)
    AND (m.`biz_id` <=> n.`instance_id`)
    AND m.`msg_type` = (CASE n.`notify_type` COLLATE utf8mb4_unicode_ci
      WHEN 'TASK_ASSIGNED' THEN 'APPROVAL_TASK'
      WHEN 'INSTANCE_FINISHED' THEN 'APPROVAL_RESULT'
      ELSE n.`notify_type`
    END) COLLATE utf8mb4_unicode_ci
);

-- 迁移结果（执行后查看）
SELECT
  (SELECT COUNT(*) FROM `star_pivot_approval`.`ap_notification`) AS ap_notification_total,
  (SELECT COUNT(*) FROM `star_pivot`.`sys_user_message`
   WHERE `msg_type` IN ('APPROVAL_TASK', 'APPROVAL_RESULT')) AS sys_user_message_approval_total,
  (SELECT COUNT(*) FROM `star_pivot_approval`.`ap_notification` n
   WHERE NOT EXISTS (
     SELECT 1 FROM `star_pivot`.`sys_user_message` m
     WHERE m.`user_id` = n.`user_id`
       AND m.`title` = LEFT(n.`title`, 200) COLLATE utf8mb4_unicode_ci
       AND (m.`create_time` <=> n.`create_time`)
       AND (m.`biz_id` <=> n.`instance_id`)
       AND m.`msg_type` = (CASE n.`notify_type` COLLATE utf8mb4_unicode_ci
         WHEN 'TASK_ASSIGNED' THEN 'APPROVAL_TASK'
         WHEN 'INSTANCE_FINISHED' THEN 'APPROVAL_RESULT'
         ELSE n.`notify_type`
       END) COLLATE utf8mb4_unicode_ci
   )) AS pending_unmigrated;
