-- 全平台统一消息中心：用户收件箱 + 菜单
-- 用法：mysql -h127.0.0.1 -P3307 -uroot -proot star_pivot < sql/patch_sys_user_message.sql

USE star_pivot;

-- ----------------------------
-- Table structure for sys_user_message
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sys_user_message` (
  `message_id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `msg_type` varchar(32) NOT NULL COMMENT '消息类型（APPROVAL_TASK/APPROVAL_RESULT/SYSTEM/ORDER等）',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` varchar(500) DEFAULT NULL COMMENT '内容摘要',
  `biz_module` varchar(32) DEFAULT NULL COMMENT '业务模块（approval/mall/system）',
  `biz_type` varchar(32) DEFAULT NULL COMMENT '业务类型（purchase/task_assigned等）',
  `biz_key` varchar(128) DEFAULT NULL COMMENT '业务唯一键',
  `biz_id` bigint DEFAULT NULL COMMENT '业务主键（便于跳转）',
  `link_path` varchar(256) DEFAULT NULL COMMENT '前端路由路径',
  `read_flag` char(1) NOT NULL DEFAULT '0' COMMENT '0未读 1已读',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`message_id`) USING BTREE,
  KEY `idx_user_read_time` (`user_id`, `read_flag`, `create_time`) USING BTREE,
  KEY `idx_user_type_time` (`user_id`, `msg_type`, `create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户站内消息收件箱';

-- 菜单（消息中心）
INSERT INTO `sys_menu` VALUES (319, '消息中心', 1, 9, 'message', '/system/message/index', NULL, 'MessageCenter', 1, 0, 'C', '0', '0', 'system:message:list', 'ri:mail-line', 'admin', NOW(), '', NULL, '全平台站内消息收件箱')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name), component = VALUES(component), perms = VALUES(perms);

INSERT INTO `sys_menu` VALUES (320, '消息查询', 319, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:message:query', '#', 'admin', NOW(), '', NULL, '站内消息查询')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);

INSERT INTO `sys_menu` VALUES (321, '消息已读', 319, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:message:edit', '#', 'admin', NOW(), '', NULL, '站内消息标记已读')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);

INSERT INTO `sys_menu` VALUES (322, '消息群发', 319, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:message:send', '#', 'admin', NOW(), '', NULL, '管理员群发站内消息')
ON DUPLICATE KEY UPDATE perms = VALUES(perms);

-- 超级管理员角色绑定消息中心菜单（含群发按钮权限）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 319),
(1, 320),
(1, 321),
(1, 322);

-- 历史审批通知迁移见：sql/patch_migrate_ap_notification_to_sys_user_message.sql
-- 执行后若普通角色仍无群发权限，请在「角色管理」勾选「消息群发」并重新登录
