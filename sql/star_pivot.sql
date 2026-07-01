/*
 Navicat Premium Data Transfer

 Source Server         : docker
 Source Server Type    : MySQL
 Source Server Version : 80046
 Source Host           : localhost:3307
 Source Schema         : star_pivot

 Target Server Type    : MySQL
 Target Server Version : 80046
 File Encoding         : 65001

 Date: 01/07/2026 16:58:43
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ap_instance
-- ----------------------------
DROP TABLE IF EXISTS `ap_instance`;
CREATE TABLE `ap_instance`  (
  `instance_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `template_id` bigint(0) NOT NULL,
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_module` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `starter_id` bigint(0) NOT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'RUNNING',
  `current_step_id` bigint(0) NULL DEFAULT NULL COMMENT '当前步骤',
  `context_json` json NULL COMMENT '审批上下文',
  `running_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci GENERATED ALWAYS AS (if((`status` = _utf8mb4'RUNNING'),_utf8mb4'1',NULL)) STORED NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `finish_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`instance_id`) USING BTREE,
  UNIQUE INDEX `uk_biz_running`(`biz_key`, `running_flag`) USING BTREE,
  INDEX `idx_starter`(`starter_id`) USING BTREE,
  INDEX `idx_biz`(`biz_module`, `biz_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批实例' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ap_instance
-- ----------------------------
INSERT INTO `ap_instance` VALUES (1, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:2', '采购单审批 #2', 1, 'WITHDRAWN', NULL, '{\"amount\": 629900.0, \"wareId\": 3}', DEFAULT, '2026-06-26 11:02:32', '2026-06-26 11:09:26');
INSERT INTO `ap_instance` VALUES (2, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:2', '采购单审批 #2', 1, 'WITHDRAWN', NULL, '{\"amount\": 629900.0, \"wareId\": 3}', DEFAULT, '2026-06-26 11:09:42', '2026-06-26 11:10:04');
INSERT INTO `ap_instance` VALUES (3, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:2', '采购单审批 #2', 1, 'WITHDRAWN', NULL, '{\"amount\": 629900.0, \"wareId\": 3}', DEFAULT, '2026-06-26 11:10:21', '2026-06-26 11:13:08');
INSERT INTO `ap_instance` VALUES (4, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:2', '采购单审批 #2', 1, 'APPROVED', 2, '{\"amount\": 629900.0, \"wareId\": 3}', DEFAULT, '2026-06-26 11:13:17', '2026-06-26 11:15:40');
INSERT INTO `ap_instance` VALUES (5, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:3', '采购单审批 #3', 1, 'APPROVED', 2, '{\"amount\": 8888.0, \"wareId\": 1}', DEFAULT, '2026-06-26 14:39:53', '2026-06-26 14:42:45');
INSERT INTO `ap_instance` VALUES (6, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:3', '采购单审批 #3', 1, 'APPROVED', 2, '{\"amount\": 8888.0, \"wareId\": 1}', DEFAULT, '2026-06-26 14:56:01', '2026-06-26 14:56:03');

-- ----------------------------
-- Table structure for ap_notification
-- ----------------------------
DROP TABLE IF EXISTS `ap_notification`;
CREATE TABLE `ap_notification`  (
  `notify_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(0) NOT NULL COMMENT '接收人',
  `notify_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'TASK_ASSIGNED/INSTANCE_FINISHED',
  `title` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `instance_id` bigint(0) NULL DEFAULT NULL,
  `task_id` bigint(0) NULL DEFAULT NULL,
  `read_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '0未读 1已读',
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`notify_id`) USING BTREE,
  INDEX `idx_user_read_time`(`user_id`, `read_flag`, `create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批站内通知' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ap_notification
-- ----------------------------

-- ----------------------------
-- Table structure for ap_record
-- ----------------------------
DROP TABLE IF EXISTS `ap_record`;
CREATE TABLE `ap_record`  (
  `record_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `instance_id` bigint(0) NOT NULL,
  `task_id` bigint(0) NULL DEFAULT NULL,
  `step_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `step_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `operator_id` bigint(0) NOT NULL,
  `action` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'SUBMIT/APPROVE/REJECT/WITHDRAW/SKIP',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NOT NULL,
  PRIMARY KEY (`record_id`) USING BTREE,
  INDEX `idx_instance`(`instance_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ap_record
-- ----------------------------
INSERT INTO `ap_record` VALUES (1, 1, NULL, 'START', '发起', 1, 'SUBMIT', '发起审批', '2026-06-26 11:02:32');
INSERT INTO `ap_record` VALUES (2, 1, NULL, 'WITHDRAW', '撤回', 1, 'WITHDRAW', '发起人撤回', '2026-06-26 11:09:26');
INSERT INTO `ap_record` VALUES (3, 2, NULL, 'START', '发起', 1, 'SUBMIT', '发起审批', '2026-06-26 11:09:42');
INSERT INTO `ap_record` VALUES (4, 2, NULL, 'WITHDRAW', '撤回', 1, 'WITHDRAW', '发起人撤回', '2026-06-26 11:10:04');
INSERT INTO `ap_record` VALUES (5, 3, NULL, 'START', '发起', 1, 'SUBMIT', '发起审批', '2026-06-26 11:10:21');
INSERT INTO `ap_record` VALUES (6, 3, NULL, 'WITHDRAW', '撤回', 1, 'WITHDRAW', '发起人撤回', '2026-06-26 11:13:08');
INSERT INTO `ap_record` VALUES (7, 4, NULL, 'START', '发起', 1, 'SUBMIT', '发起审批', '2026-06-26 11:13:17');
INSERT INTO `ap_record` VALUES (8, 4, 1, 'dept_leader', '部门负责人', 1, 'APPROVE', '通过', '2026-06-26 11:13:58');
INSERT INTO `ap_record` VALUES (9, 4, 2, 'finance', '财务审批', 1, 'APPROVE', '通过', '2026-06-26 11:15:40');
INSERT INTO `ap_record` VALUES (10, 5, NULL, 'START', '发起', 1, 'SUBMIT', '发起审批', '2026-06-26 14:39:53');
INSERT INTO `ap_record` VALUES (11, 5, 3, 'dept_leader', '部门负责人', 1, 'APPROVE', 'E2E pass round 1', '2026-06-26 14:42:44');
INSERT INTO `ap_record` VALUES (12, 5, 4, 'finance', '财务审批', 1, 'APPROVE', 'E2E pass round 2', '2026-06-26 14:42:45');
INSERT INTO `ap_record` VALUES (13, 6, NULL, 'START', '发起', 1, 'SUBMIT', '发起审批', '2026-06-26 14:56:01');
INSERT INTO `ap_record` VALUES (14, 6, 5, 'dept_leader', '部门负责人', 1, 'APPROVE', 'e2e-approval.ps1', '2026-06-26 14:56:03');
INSERT INTO `ap_record` VALUES (15, 6, 6, 'finance', '财务审批', 1, 'APPROVE', 'e2e-approval.ps1', '2026-06-26 14:56:03');

-- ----------------------------
-- Table structure for ap_task
-- ----------------------------
DROP TABLE IF EXISTS `ap_task`;
CREATE TABLE `ap_task`  (
  `task_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `instance_id` bigint(0) NOT NULL,
  `step_id` bigint(0) NOT NULL,
  `step_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `step_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `assignee_id` bigint(0) NOT NULL,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/DONE/CANCELLED',
  `action` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'APPROVE/REJECT',
  `comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `due_time` datetime(0) NULL DEFAULT NULL COMMENT '截止时间',
  `finish_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`task_id`) USING BTREE,
  INDEX `idx_assignee_status`(`assignee_id`, `status`) USING BTREE,
  INDEX `idx_instance`(`instance_id`) USING BTREE,
  INDEX `idx_status_due`(`status`, `due_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批待办' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ap_task
-- ----------------------------
INSERT INTO `ap_task` VALUES (1, 4, 1, 'dept_leader', '部门负责人', 1, 'DONE', 'APPROVE', '通过', '2026-06-26 11:13:17', NULL, '2026-06-26 11:13:58');
INSERT INTO `ap_task` VALUES (2, 4, 2, 'finance', '财务审批', 1, 'DONE', 'APPROVE', '通过', '2026-06-26 11:13:58', NULL, '2026-06-26 11:15:40');
INSERT INTO `ap_task` VALUES (3, 5, 1, 'dept_leader', '部门负责人', 1, 'DONE', 'APPROVE', 'E2E pass round 1', '2026-06-26 14:39:53', NULL, '2026-06-26 14:42:44');
INSERT INTO `ap_task` VALUES (4, 5, 2, 'finance', '财务审批', 1, 'DONE', 'APPROVE', 'E2E pass round 2', '2026-06-26 14:42:44', NULL, '2026-06-26 14:42:45');
INSERT INTO `ap_task` VALUES (5, 6, 1, 'dept_leader', '部门负责人', 1, 'DONE', 'APPROVE', 'e2e-approval.ps1', '2026-06-26 14:56:01', NULL, '2026-06-26 14:56:03');
INSERT INTO `ap_task` VALUES (6, 6, 2, 'finance', '财务审批', 1, 'DONE', 'APPROVE', 'e2e-approval.ps1', '2026-06-26 14:56:03', NULL, '2026-06-26 14:56:03');

-- ----------------------------
-- Table structure for ap_template
-- ----------------------------
DROP TABLE IF EXISTS `ap_template`;
CREATE TABLE `ap_template`  (
  `template_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板编码',
  `template_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_module` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'mall/system/…',
  `version` int(0) NOT NULL DEFAULT 1,
  `status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/DISABLED',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`template_id`) USING BTREE,
  UNIQUE INDEX `uk_code_version`(`template_code`, `version`) USING BTREE,
  INDEX `idx_module_status`(`biz_module`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批模板' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ap_template
-- ----------------------------
INSERT INTO `ap_template` VALUES (1, 'mall_purchase_default', '采购单默认审批', 'mall', 1, 'PUBLISHED', '部门负责人→财务', 'admin', '2026-06-26 10:49:49', NULL, NULL);
INSERT INTO `ap_template` VALUES (2, 'mall_return_default', '退货单默认审批', 'mall', 1, 'PUBLISHED', '部门负责人→财务', 'admin', '2026-06-26 10:51:32', NULL, NULL);
INSERT INTO `ap_template` VALUES (3, 'mall_coupon_default', '优惠券发布审批', 'mall', 1, 'PUBLISHED', '运营主管→财务', 'admin', '2026-06-30 17:34:37', NULL, NULL);
INSERT INTO `ap_template` VALUES (4, 'mall_spu_default', '商品上架审批', 'mall', 1, 'PUBLISHED', '品类负责人→质控', 'admin', '2026-06-30 17:34:37', NULL, NULL);

-- ----------------------------
-- Table structure for ap_template_bind
-- ----------------------------
DROP TABLE IF EXISTS `ap_template_bind`;
CREATE TABLE `ap_template_bind`  (
  `bind_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `biz_module` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'purchase/return/coupon…',
  `match_expr` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SpEL，空=总是匹配',
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '指向已发布模板',
  `priority` int(0) NOT NULL DEFAULT 0,
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0' COMMENT '0启用 1停用',
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`bind_id`) USING BTREE,
  INDEX `idx_biz`(`biz_module`, `biz_type`, `status`, `priority`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批模板业务绑定' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ap_template_bind
-- ----------------------------
INSERT INTO `ap_template_bind` VALUES (1, 'mall', 'purchase', NULL, 'mall_purchase_default', 0, '0', '2026-06-26 10:49:49');
INSERT INTO `ap_template_bind` VALUES (2, 'mall', 'return', NULL, 'mall_return_default', 0, '0', '2026-06-26 10:51:32');
INSERT INTO `ap_template_bind` VALUES (3, 'mall', 'coupon', NULL, 'mall_coupon_default', 0, '0', '2026-06-30 17:34:37');
INSERT INTO `ap_template_bind` VALUES (4, 'mall', 'spu', NULL, 'mall_spu_default', 0, '0', '2026-06-30 17:34:37');

-- ----------------------------
-- Table structure for ap_template_route
-- ----------------------------
DROP TABLE IF EXISTS `ap_template_route`;
CREATE TABLE `ap_template_route`  (
  `route_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `template_id` bigint(0) NOT NULL,
  `from_step_id` bigint(0) NOT NULL,
  `priority` int(0) NOT NULL DEFAULT 0 COMMENT '越小优先',
  `condition_expr` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SpEL，空或 default 表示默认分支',
  `to_step_id` bigint(0) NOT NULL,
  PRIMARY KEY (`route_id`) USING BTREE,
  INDEX `idx_tpl_from`(`template_id`, `from_step_id`, `priority`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批步骤路由' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ap_template_route
-- ----------------------------

-- ----------------------------
-- Table structure for ap_template_step
-- ----------------------------
DROP TABLE IF EXISTS `ap_template_step`;
CREATE TABLE `ap_template_step`  (
  `step_id` bigint(0) NOT NULL AUTO_INCREMENT,
  `template_id` bigint(0) NOT NULL,
  `step_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '步骤编码，模板内唯一',
  `step_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `step_order` int(0) NOT NULL COMMENT '排序，从 1 递增',
  `assignee_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `assignee_value` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approve_mode` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ANY' COMMENT 'ANY/ALL',
  `skip_expression` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SpEL，true 则跳过本步',
  `timeout_hours` int(0) NULL DEFAULT NULL COMMENT '超时小时数，空表示不超时',
  `timeout_action` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'AUTO_REJECT' COMMENT 'AUTO_REJECT/AUTO_APPROVE',
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`step_id`) USING BTREE,
  UNIQUE INDEX `uk_tpl_step_code`(`template_id`, `step_code`) USING BTREE,
  INDEX `idx_tpl_order`(`template_id`, `step_order`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批模板步骤' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ap_template_step
-- ----------------------------
INSERT INTO `ap_template_step` VALUES (1, 1, 'dept_leader', '部门负责人', 1, 'DEPT_LEADER', NULL, 'ANY', NULL, NULL, 'AUTO_REJECT', '2026-06-26 10:49:49');
INSERT INTO `ap_template_step` VALUES (2, 1, 'finance', '财务审批', 2, 'ROLE', 'finance', 'ANY', NULL, NULL, 'AUTO_REJECT', '2026-06-26 10:49:49');
INSERT INTO `ap_template_step` VALUES (3, 2, 'dept_leader', '部门负责人', 1, 'DEPT_LEADER', NULL, 'ANY', NULL, NULL, 'AUTO_REJECT', '2026-06-26 10:51:32');
INSERT INTO `ap_template_step` VALUES (4, 2, 'finance', '财务审批', 2, 'ROLE', 'finance', 'ANY', NULL, NULL, 'AUTO_REJECT', '2026-06-26 10:51:32');
INSERT INTO `ap_template_step` VALUES (5, 3, 'ops_leader', '运营主管', 1, 'DEPT_LEADER', NULL, 'ANY', NULL, NULL, 'AUTO_REJECT', '2026-06-30 17:34:37');
INSERT INTO `ap_template_step` VALUES (6, 3, 'finance', '财务审批', 2, 'ROLE', 'finance', 'ANY', NULL, NULL, 'AUTO_REJECT', '2026-06-30 17:34:37');
INSERT INTO `ap_template_step` VALUES (7, 4, 'category_leader', '品类负责人', 1, 'DEPT_LEADER', NULL, 'ANY', NULL, NULL, 'AUTO_REJECT', '2026-06-30 17:34:37');
INSERT INTO `ap_template_step` VALUES (8, 4, 'quality', '质控审批', 2, 'ROLE', 'finance', 'ANY', NULL, NULL, 'AUTO_REJECT', '2026-06-30 17:34:37');

-- ----------------------------
-- Table structure for gen_table
-- ----------------------------
DROP TABLE IF EXISTS `gen_table`;
CREATE TABLE `gen_table`  (
  `table_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '表名称',
  `table_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '表描述',
  `sub_table_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联子表的表名',
  `sub_table_fk_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '子表关联的外键名',
  `class_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '实体类名称',
  `tpl_category` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'crud' COMMENT '使用的模板（crud单表操作 tree树表操作）',
  `tpl_web_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '前端模板类型（element-ui模版 element-plus模版）',
  `package_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生成包路径',
  `module_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生成模块名',
  `business_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生成业务名',
  `function_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生成功能名',
  `function_author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '生成功能作者',
  `gen_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '生成代码方式（0zip压缩包 1自定义路径）',
  `gen_path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '/' COMMENT '生成路径（不填默认项目路径）',
  `options` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '其它生成选项',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`table_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '代码生成业务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table
-- ----------------------------
INSERT INTO `gen_table` VALUES (28, 'sys_dept', '部门管理', NULL, NULL, 'SysDept', 'tree', 'art-design-pro', 'com.star.pivot.system', 'system', 'dept', '部门管理', 'admin', '0', '/main/demo', '{\"treeCode\":\"id\",\"treeName\":\"name\",\"treeParentCode\":\"pid\",\"parentMenuId\":1}', 'admin', '2026-06-02 20:07:38', '', '2026-06-03 16:43:07', '导入自外部库 renren');
INSERT INTO `gen_table` VALUES (29, 'sys_user', '用户信息表', 'sys_user_role', 'user_id', 'SysUser', 'sub', 'element-ui', 'com.star.pivot.system', 'system', 'user', '用户信息', 'admin', '0', '/', '{\"treeCode\":\"\",\"treeName\":\"\",\"treeParentCode\":\"\",\"parentMenuId\":1}', 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:27', '');
INSERT INTO `gen_table` VALUES (30, 'sys_user_role', '用户与角色关联表', NULL, NULL, 'SysUserRole', 'crud', '', 'com.star.pivot.system', 'system', 'role', '用户与角色关联', 'admin', '0', '/', NULL, 'admin', '2026-06-03 17:12:31', '', NULL, NULL);

-- ----------------------------
-- Table structure for gen_table_column
-- ----------------------------
DROP TABLE IF EXISTS `gen_table_column`;
CREATE TABLE `gen_table_column`  (
  `column_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '编号',
  `table_id` bigint(0) NULL DEFAULT NULL COMMENT '归属表编号',
  `column_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '列名称',
  `column_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '列描述',
  `column_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '列类型',
  `java_type` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'JAVA类型',
  `java_field` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'JAVA字段名',
  `is_pk` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否主键（1是）',
  `is_increment` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否自增（1是）',
  `is_required` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否必填（1是）',
  `is_insert` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否为插入字段（1是）',
  `is_edit` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否编辑字段（1是）',
  `is_list` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否列表字段（1是）',
  `is_query` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否查询字段（1是）',
  `query_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'EQ' COMMENT '查询方式（等于、不等于、大于、小于、范围）',
  `html_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
  `dict_type` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '字典类型',
  `sort` int(0) NULL DEFAULT NULL COMMENT '排序',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`column_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 236 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '代码生成业务表字段' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gen_table_column
-- ----------------------------
INSERT INTO `gen_table_column` VALUES (205, 28, 'id', 'id', 'bigint(20)', 'Long', 'id', '1', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-06-02 20:07:38', '', '2026-06-03 16:43:07');
INSERT INTO `gen_table_column` VALUES (206, 28, 'pid', '上级ID', 'bigint(20)', 'Long', 'pid', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-06-02 20:07:38', '', '2026-06-03 16:43:07');
INSERT INTO `gen_table_column` VALUES (207, 28, 'pids', '所有上级ID，用逗号分开', 'varchar(500)', 'String', 'pids', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 3, 'admin', '2026-06-02 20:07:38', '', '2026-06-03 16:43:07');
INSERT INTO `gen_table_column` VALUES (208, 28, 'name', '部门名称', 'varchar(50)', 'String', 'name', '0', '0', '0', '1', '1', '1', '1', 'LIKE', 'input', '', 4, 'admin', '2026-06-02 20:07:38', '', '2026-06-03 16:43:07');
INSERT INTO `gen_table_column` VALUES (209, 28, 'sort', '排序', 'int(10) unsigned', 'Integer', 'sort', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 5, 'admin', '2026-06-02 20:07:38', '', '2026-06-03 16:43:07');
INSERT INTO `gen_table_column` VALUES (210, 28, 'creator', '创建者', 'bigint(20)', 'Long', 'creator', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2026-06-02 20:07:38', '', '2026-06-03 16:43:07');
INSERT INTO `gen_table_column` VALUES (211, 28, 'create_date', '创建时间', 'datetime', 'LocalDateTime', 'createDate', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'datetime', '', 7, 'admin', '2026-06-02 20:07:38', '', '2026-06-03 16:43:07');
INSERT INTO `gen_table_column` VALUES (212, 28, 'updater', '更新者', 'bigint(20)', 'Long', 'updater', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 8, 'admin', '2026-06-02 20:07:38', '', '2026-06-03 16:43:07');
INSERT INTO `gen_table_column` VALUES (213, 28, 'update_date', '更新时间', 'datetime', 'LocalDateTime', 'updateDate', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'datetime', '', 9, 'admin', '2026-06-02 20:07:38', '', '2026-06-03 16:43:07');
INSERT INTO `gen_table_column` VALUES (214, 29, 'user_id', '用户ID', 'bigint', 'Long', 'userId', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:27');
INSERT INTO `gen_table_column` VALUES (215, 29, 'dept_id', '部门ID', 'bigint', 'Long', 'deptId', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:27');
INSERT INTO `gen_table_column` VALUES (216, 29, 'user_name', '用户账号', 'varchar(30)', 'String', 'userName', '0', '0', '1', '1', '1', '1', '1', 'LIKE', 'input', '', 3, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:27');
INSERT INTO `gen_table_column` VALUES (217, 29, 'nick_name', '用户昵称', 'varchar(30)', 'String', 'nickName', '0', '0', '1', '1', '1', '1', '1', 'LIKE', 'input', '', 4, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:27');
INSERT INTO `gen_table_column` VALUES (218, 29, 'user_type', '用户类型（00系统用户）', 'varchar(2)', 'String', 'userType', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', '', 5, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:27');
INSERT INTO `gen_table_column` VALUES (219, 29, 'email', '用户邮箱', 'varchar(50)', 'String', 'email', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 6, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (220, 29, 'phonenumber', '手机号码', 'varchar(11)', 'String', 'phonenumber', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 7, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (221, 29, 'sex', '用户性别（0男 1女 2未知）', 'char(1)', 'String', 'sex', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'select', 'sys_user_sex', 8, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (222, 29, 'avatar', '头像地址', 'varchar(500)', 'String', 'avatar', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'textarea', '', 9, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (223, 29, 'password', '密码', 'varchar(100)', 'String', 'password', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 10, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (224, 29, 'status', '账号状态（0正常 1停用）', 'char(1)', 'String', 'status', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'radio', '', 11, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (225, 29, 'del_flag', '删除标志（0代表存在 2代表删除）', 'char(1)', 'String', 'delFlag', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 12, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (226, 29, 'login_ip', '最后登录IP', 'varchar(128)', 'String', 'loginIp', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'input', '', 13, 'admin', '2026-06-03 17:12:30', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (227, 29, 'login_date', '最后登录时间', 'datetime', 'LocalDateTime', 'loginDate', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'datetime', '', 14, 'admin', '2026-06-03 17:12:31', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (228, 29, 'pwd_update_date', '密码最后更新时间', 'datetime', 'LocalDateTime', 'pwdUpdateDate', '0', '0', '0', '1', '1', '1', '1', 'EQ', 'datetime', '', 15, 'admin', '2026-06-03 17:12:31', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (229, 29, 'create_by', '创建者', 'varchar(64)', 'String', 'createBy', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 16, 'admin', '2026-06-03 17:12:31', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (230, 29, 'create_time', '创建时间', 'datetime', 'LocalDateTime', 'createTime', '0', '0', '0', '1', NULL, NULL, NULL, 'EQ', 'datetime', '', 17, 'admin', '2026-06-03 17:12:31', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (231, 29, 'update_by', '更新者', 'varchar(64)', 'String', 'updateBy', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'input', '', 18, 'admin', '2026-06-03 17:12:31', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (232, 29, 'update_time', '更新时间', 'datetime', 'LocalDateTime', 'updateTime', '0', '0', '0', '1', '1', NULL, NULL, 'EQ', 'datetime', '', 19, 'admin', '2026-06-03 17:12:31', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (233, 29, 'remark', '备注', 'varchar(500)', 'String', 'remark', '0', '0', '0', '1', '1', '1', NULL, 'EQ', 'textarea', '', 20, 'admin', '2026-06-03 17:12:31', '', '2026-06-03 20:40:28');
INSERT INTO `gen_table_column` VALUES (234, 30, 'id', '主键ID', 'bigint', 'Long', 'id', '1', '1', '0', '1', NULL, NULL, NULL, 'EQ', 'input', '', 1, 'admin', '2026-06-03 17:12:31', '', NULL);
INSERT INTO `gen_table_column` VALUES (235, 30, 'user_id', '用户ID', 'bigint', 'Long', 'userId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 2, 'admin', '2026-06-03 17:12:31', '', NULL);
INSERT INTO `gen_table_column` VALUES (236, 30, 'role_id', '角色ID', 'bigint', 'Long', 'roleId', '0', '0', '1', '1', '1', '1', '1', 'EQ', 'input', '', 3, 'admin', '2026-06-03 17:12:31', '', NULL);

-- ----------------------------
-- Table structure for qrtz_blob_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_blob_triggers`;
CREATE TABLE `qrtz_blob_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `blob_data` blob NULL COMMENT '存放持久化Trigger对象',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_blob_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Blob类型的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_blob_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_calendars
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_calendars`;
CREATE TABLE `qrtz_calendars`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '日历名称',
  `calendar` blob NOT NULL COMMENT '存放持久化calendar对象',
  PRIMARY KEY (`sched_name`, `calendar_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '日历信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_calendars
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_cron_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_cron_triggers`;
CREATE TABLE `qrtz_cron_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `cron_expression` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'cron表达式',
  `time_zone_id` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '时区',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_cron_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Cron类型的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_cron_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_fired_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_fired_triggers`;
CREATE TABLE `qrtz_fired_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `entry_id` varchar(95) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器实例id',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度器实例名',
  `fired_time` bigint(0) NOT NULL COMMENT '触发的时间',
  `sched_time` bigint(0) NOT NULL COMMENT '定时器制定的时间',
  `priority` int(0) NOT NULL COMMENT '优先级',
  `state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '任务组名',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否并发',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否接受恢复执行',
  PRIMARY KEY (`sched_name`, `entry_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '已触发的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_fired_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_job_details
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_job_details`;
CREATE TABLE `qrtz_job_details`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务组名',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相关介绍',
  `job_class_name` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '执行任务类名称',
  `is_durable` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否持久化',
  `is_nonconcurrent` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否并发',
  `is_update_data` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否更新数据',
  `requests_recovery` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '是否接受恢复执行',
  `job_data` blob NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '任务详细信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_job_details
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_locks
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_locks`;
CREATE TABLE `qrtz_locks`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `lock_name` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '悲观锁名称',
  PRIMARY KEY (`sched_name`, `lock_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '存储的悲观锁信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_locks
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_paused_trigger_grps
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_paused_trigger_grps`;
CREATE TABLE `qrtz_paused_trigger_grps`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  PRIMARY KEY (`sched_name`, `trigger_group`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '暂停的触发器表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_paused_trigger_grps
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_scheduler_state
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_scheduler_state`;
CREATE TABLE `qrtz_scheduler_state`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `instance_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实例名称',
  `last_checkin_time` bigint(0) NOT NULL COMMENT '上次检查时间',
  `checkin_interval` bigint(0) NOT NULL COMMENT '检查间隔时间',
  PRIMARY KEY (`sched_name`, `instance_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '调度器状态表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_scheduler_state
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simple_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simple_triggers`;
CREATE TABLE `qrtz_simple_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `repeat_count` bigint(0) NOT NULL COMMENT '重复的次数统计',
  `repeat_interval` bigint(0) NOT NULL COMMENT '重复的间隔时间',
  `times_triggered` bigint(0) NOT NULL COMMENT '已经触发的次数',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simple_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '简单触发器的信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_simple_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_simprop_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_simprop_triggers`;
CREATE TABLE `qrtz_simprop_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_name的外键',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_triggers表trigger_group的外键',
  `str_prop_1` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第一个参数',
  `str_prop_2` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第二个参数',
  `str_prop_3` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'String类型的trigger的第三个参数',
  `int_prop_1` int(0) NULL DEFAULT NULL COMMENT 'int类型的trigger的第一个参数',
  `int_prop_2` int(0) NULL DEFAULT NULL COMMENT 'int类型的trigger的第二个参数',
  `long_prop_1` bigint(0) NULL DEFAULT NULL COMMENT 'long类型的trigger的第一个参数',
  `long_prop_2` bigint(0) NULL DEFAULT NULL COMMENT 'long类型的trigger的第二个参数',
  `dec_prop_1` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第一个参数',
  `dec_prop_2` decimal(13, 4) NULL DEFAULT NULL COMMENT 'decimal类型的trigger的第二个参数',
  `bool_prop_1` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第一个参数',
  `bool_prop_2` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Boolean类型的trigger的第二个参数',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  CONSTRAINT `qrtz_simprop_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `trigger_name`, `trigger_group`) REFERENCES `qrtz_triggers` (`sched_name`, `trigger_name`, `trigger_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '同步机制的行锁表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_simprop_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for qrtz_triggers
-- ----------------------------
DROP TABLE IF EXISTS `qrtz_triggers`;
CREATE TABLE `qrtz_triggers`  (
  `sched_name` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调度名称',
  `trigger_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器的名字',
  `trigger_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器所属组的名字',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_job_details表job_name的外键',
  `job_group` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'qrtz_job_details表job_group的外键',
  `description` varchar(250) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '相关介绍',
  `next_fire_time` bigint(0) NULL DEFAULT NULL COMMENT '上一次触发时间（毫秒）',
  `prev_fire_time` bigint(0) NULL DEFAULT NULL COMMENT '下一次触发时间（默认为-1表示不触发）',
  `priority` int(0) NULL DEFAULT NULL COMMENT '优先级',
  `trigger_state` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器状态',
  `trigger_type` varchar(8) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '触发器的类型',
  `start_time` bigint(0) NOT NULL COMMENT '开始时间',
  `end_time` bigint(0) NULL DEFAULT NULL COMMENT '结束时间',
  `calendar_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '日程表名称',
  `misfire_instr` smallint(0) NULL DEFAULT NULL COMMENT '补偿执行的策略',
  `job_data` blob NULL COMMENT '存放持久化job对象',
  PRIMARY KEY (`sched_name`, `trigger_name`, `trigger_group`) USING BTREE,
  INDEX `sched_name`(`sched_name`, `job_name`, `job_group`) USING BTREE,
  CONSTRAINT `qrtz_triggers_ibfk_1` FOREIGN KEY (`sched_name`, `job_name`, `job_group`) REFERENCES `qrtz_job_details` (`sched_name`, `job_name`, `job_group`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '触发器详细信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of qrtz_triggers
-- ----------------------------

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `config_id` int(0) NOT NULL AUTO_INCREMENT COMMENT '参数主键',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '参数名称',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '参数键名',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '参数键值',
  `config_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'N' COMMENT '系统内置（Y是 N否）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`config_id`) USING BTREE,
  INDEX `idx_config_key`(`config_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (2, '用户管理-账号初始密码', 'sys.user.initPassword', '123456', 'Y', 'admin', '2026-01-25 17:41:36', '', NULL, '初始化密码 123456');
INSERT INTO `sys_config` VALUES (4, '账号自助-验证码开关', 'sys.account.captchaEnabled', 'true', 'Y', 'admin', '2026-01-25 17:41:36', '', NULL, '是否开启验证码功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (5, '账号自助-是否开启用户注册功能', 'sys.account.registerUser', 'false', 'Y', 'admin', '2026-01-25 17:41:36', '', NULL, '是否开启注册用户功能（true开启，false关闭）');
INSERT INTO `sys_config` VALUES (6, '用户登录-黑名单列表', 'sys.login.blackIPList', '', 'Y', 'admin', '2026-01-25 17:41:36', '', NULL, '设置登录IP黑名单限制，多个匹配项以;分隔，支持匹配（*通配、网段）');
INSERT INTO `sys_config` VALUES (7, '用户管理-初始密码修改策略', 'sys.account.initPasswordModify', '1', 'Y', 'admin', '2026-01-25 17:41:36', '', NULL, '0：初始密码修改策略关闭，没有任何提示，1：提醒用户，如果未修改初始密码，则在登录时就会提醒修改密码对话框');
INSERT INTO `sys_config` VALUES (8, '用户管理-账号密码更新周期', 'sys.account.passwordValidateDays', '0', 'Y', 'admin', '2026-01-25 17:41:36', '', NULL, '密码更新周期（填写数字，数据初始化值为0不限制，若修改必须为大于0小于365的正整数），如果超过这个周期登录系统时，则在登录时就会提醒修改密码对话框');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '部门名称',
  `order_num` int(0) NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '部门状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`dept_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_del_flag_status`(`del_flag`, `status`) USING BTREE,
  INDEX `idx_order_num`(`order_num`) USING BTREE,
  INDEX `idx_dept_parent`(`parent_id`) USING BTREE,
  INDEX `idx_dept_ancestors`(`ancestors`) USING BTREE,
  INDEX `idx_dept_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 112 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '星枢科技', 0, 'admin', '18888888888', '18888888888@163.com', '0', '0', 'admin', '2025-12-28 13:46:34', 'admin', '2026-06-26 11:11:40');
INSERT INTO `sys_dept` VALUES (101, 100, '0,100', '深圳总公司', 1, 'admin', '18888888888', '18888888888@163.com', '0', '0', 'admin', '2025-12-28 13:46:34', 'admin', '2026-06-26 11:11:48');
INSERT INTO `sys_dept` VALUES (102, 100, '0,100', '长沙分公司', 2, 'admin', '18888888888', '18888888888@163.com', '0', '0', 'admin', '2025-12-28 13:46:34', 'admin', '2026-06-26 11:11:52');
INSERT INTO `sys_dept` VALUES (103, 101, '0,100,101', '研发部门', 1, 'admin', '18834581124', '18834581124@163.com', '0', '0', 'admin', '2025-12-28 13:46:34', 'admin', '2026-06-26 11:12:52');
INSERT INTO `sys_dept` VALUES (104, 101, '0,100,101', '市场部门', 2, 'admin', '18834581124', '	\r\n18834581124@163.com', '0', '0', 'admin', '2025-12-28 13:46:34', '', NULL);
INSERT INTO `sys_dept` VALUES (105, 101, '0,100,101', '测试部门', 3, 'admin', '18834581124', '	\r\n18834581124@163.com', '0', '0', 'admin', '2025-12-28 13:46:34', '', NULL);
INSERT INTO `sys_dept` VALUES (106, 101, '0,100,101', '财务部门', 4, 'admin', '18834581124', '	\r\n18834581124@163.com', '0', '0', 'admin', '2025-12-28 13:46:34', '', NULL);
INSERT INTO `sys_dept` VALUES (107, 101, '0,100,101', '运维部门', 5, 'admin', '18834581124', '	\r\n18834581124@163.com', '0', '0', 'admin', '2025-12-28 13:46:34', '', NULL);
INSERT INTO `sys_dept` VALUES (108, 102, '0,100,102', '市场部门', 1, 'admin', '18834581124', '18834581124@163.com', '0', '0', 'admin', '2025-12-28 13:46:34', '', NULL);
INSERT INTO `sys_dept` VALUES (109, 102, '0,100,102', '财务部门', 2, 'admin', '18834581124', '	\r\n18834581124@163.com', '0', '0', 'admin', '2025-12-28 13:46:34', '', NULL);
INSERT INTO `sys_dept` VALUES (110, 100, '0,100', '山西分公司', 3, 'admin', '18888888888', '18888888888@163.com', '0', '0', 'admin', '2026-01-20 12:52:02', 'admin', '2026-06-26 11:11:56');
INSERT INTO `sys_dept` VALUES (111, 110, '0,100,110', '人事部', 1, 'admin', '18834581124', '18834581124@163.com', '0', '0', 'admin', '2026-01-20 12:52:49', 'admin', '2026-01-20 12:53:15');

-- ----------------------------
-- Table structure for sys_dict_data
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_data`;
CREATE TABLE `sys_dict_data`  (
  `dict_code` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '字典编码',
  `dict_sort` int(0) NULL DEFAULT 0 COMMENT '字典排序',
  `dict_label` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '字典标签',
  `dict_value` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '字典键值',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '字典类型',
  `css_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '样式属性（其他样式扩展）',
  `list_class` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '表格回显样式',
  `is_default` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'N' COMMENT '是否默认（Y是 N否）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_code`) USING BTREE,
  INDEX `idx_dict_type`(`dict_type`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_type_sort`(`dict_type`, `dict_sort`) USING BTREE,
  INDEX `idx_type_value`(`dict_type`, `dict_value`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 101 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '字典数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_data
-- ----------------------------
INSERT INTO `sys_dict_data` VALUES (1, 1, '男', '0', 'sys_user_sex', '', '', 'Y', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '性别男');
INSERT INTO `sys_dict_data` VALUES (2, 2, '女', '1', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '性别女');
INSERT INTO `sys_dict_data` VALUES (3, 3, '未知', '2', 'sys_user_sex', '', '', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '性别未知');
INSERT INTO `sys_dict_data` VALUES (4, 1, '显示', '0', 'sys_show_hide', '', 'primary', 'Y', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '显示菜单');
INSERT INTO `sys_dict_data` VALUES (5, 2, '隐藏', '1', 'sys_show_hide', '', 'danger', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '隐藏菜单');
INSERT INTO `sys_dict_data` VALUES (6, 1, '正常', '0', 'sys_normal_disable', '', 'primary', 'Y', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (7, 2, '停用', '1', 'sys_normal_disable', '', 'danger', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (8, 1, '正常', '0', 'sys_job_status', '', 'primary', 'Y', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (9, 2, '暂停', '1', 'sys_job_status', '', 'danger', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (10, 1, '默认', 'DEFAULT', 'sys_job_group', '', '', 'Y', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '默认分组');
INSERT INTO `sys_dict_data` VALUES (11, 2, '系统', 'SYSTEM', 'sys_job_group', '', '', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '系统分组');
INSERT INTO `sys_dict_data` VALUES (12, 1, '是', 'Y', 'sys_yes_no', '', 'primary', 'Y', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '系统默认是');
INSERT INTO `sys_dict_data` VALUES (13, 2, '否', 'N', 'sys_yes_no', '', 'danger', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '系统默认否');
INSERT INTO `sys_dict_data` VALUES (14, 1, '通知', '1', 'sys_notice_type', '', 'warning', 'Y', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '通知');
INSERT INTO `sys_dict_data` VALUES (15, 2, '公告', '2', 'sys_notice_type', '', 'success', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '公告');
INSERT INTO `sys_dict_data` VALUES (16, 1, '正常', '0', 'sys_notice_status', '', 'primary', 'Y', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (17, 2, '关闭', '1', 'sys_notice_status', '', 'danger', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '关闭状态');
INSERT INTO `sys_dict_data` VALUES (18, 99, '其他', '0', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '其他操作');
INSERT INTO `sys_dict_data` VALUES (19, 1, '新增', '1', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '新增操作');
INSERT INTO `sys_dict_data` VALUES (20, 2, '修改', '2', 'sys_oper_type', '', 'info', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '修改操作');
INSERT INTO `sys_dict_data` VALUES (21, 3, '删除', '3', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '删除操作');
INSERT INTO `sys_dict_data` VALUES (22, 4, '授权', '4', 'sys_oper_type', '', 'primary', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '授权操作');
INSERT INTO `sys_dict_data` VALUES (23, 5, '导出', '5', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '导出操作');
INSERT INTO `sys_dict_data` VALUES (24, 6, '导入', '6', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '导入操作');
INSERT INTO `sys_dict_data` VALUES (25, 7, '强退', '7', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '强退操作');
INSERT INTO `sys_dict_data` VALUES (26, 8, '生成代码', '8', 'sys_oper_type', '', 'warning', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '生成操作');
INSERT INTO `sys_dict_data` VALUES (27, 9, '清空数据', '9', 'sys_oper_type', '', 'danger', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '清空操作');
INSERT INTO `sys_dict_data` VALUES (28, 1, '成功', '0', 'sys_common_status', '', 'primary', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '正常状态');
INSERT INTO `sys_dict_data` VALUES (29, 2, '失败', '1', 'sys_common_status', '', 'danger', 'N', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '停用状态');
INSERT INTO `sys_dict_data` VALUES (100, 1, '存在', '0', 'is_delete_status', 'success', 'success', 'N', '0', 'admin', '2026-01-12 15:59:08', '', NULL, '存在');
INSERT INTO `sys_dict_data` VALUES (101, 2, '删除', '2', 'is_delete_status', 'primary', 'default', 'N', '0', 'admin', '2026-01-12 16:02:57', '', NULL, '删除');

-- ----------------------------
-- Table structure for sys_dict_type
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict_type`;
CREATE TABLE `sys_dict_type`  (
  `dict_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '字典主键',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '字典名称',
  `dict_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '字典类型',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`dict_id`) USING BTREE,
  UNIQUE INDEX `dict_type`(`dict_type`) USING BTREE,
  UNIQUE INDEX `uk_dict_type`(`dict_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 101 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '字典类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict_type
-- ----------------------------
INSERT INTO `sys_dict_type` VALUES (1, '用户性别', 'sys_user_sex', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '用户性别列表');
INSERT INTO `sys_dict_type` VALUES (2, '菜单状态', 'sys_show_hide', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '菜单状态列表');
INSERT INTO `sys_dict_type` VALUES (3, '系统开关', 'sys_normal_disable', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '系统开关列表');
INSERT INTO `sys_dict_type` VALUES (4, '任务状态', 'sys_job_status', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '任务状态列表');
INSERT INTO `sys_dict_type` VALUES (5, '任务分组', 'sys_job_group', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '任务分组列表');
INSERT INTO `sys_dict_type` VALUES (6, '系统是否', 'sys_yes_no', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '系统是否列表');
INSERT INTO `sys_dict_type` VALUES (7, '通知类型', 'sys_notice_type', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '通知类型列表');
INSERT INTO `sys_dict_type` VALUES (8, '通知状态', 'sys_notice_status', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '通知状态列表');
INSERT INTO `sys_dict_type` VALUES (9, '操作类型', 'sys_oper_type', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '操作类型列表');
INSERT INTO `sys_dict_type` VALUES (10, '系统状态', 'sys_common_status', '0', 'admin', '2025-12-28 13:46:34', '', NULL, '登录状态列表');
INSERT INTO `sys_dict_type` VALUES (100, '删除标识', 'is_delete_status', '0', 'admin', '2026-01-12 15:57:49', '', NULL, '删除标识：0未删除 2 删除');

-- ----------------------------
-- Table structure for sys_file
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file`  (
  `file_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `folder_id` bigint(0) NOT NULL COMMENT '所属文件夹ID',
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务分类（冗余，便于检索）',
  `media_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'OTHER' COMMENT '媒体类型 IMAGE/VIDEO/DOCUMENT/AUDIO/OTHER',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '原始文件名',
  `file_ext` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '扩展名',
  `content_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'MIME类型',
  `file_size` bigint(0) NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `object_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'OSS/本地对象路径',
  `file_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SHA256（可选，去重用）',
  `storage_provider` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'OSS' COMMENT '存储驱动 OSS/LOCAL',
  `biz_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务类型 notice/contract 等',
  `biz_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务主键',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '删除标志（0正常 2已删除）',
  `delete_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '删除者',
  `delete_time` datetime(0) NULL DEFAULT NULL COMMENT '删除时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`file_id`) USING BTREE,
  UNIQUE INDEX `uk_object_name`(`object_name`) USING BTREE,
  INDEX `idx_folder_del`(`folder_id`, `del_flag`) USING BTREE,
  INDEX `idx_media_type`(`media_type`, `del_flag`) USING BTREE,
  INDEX `idx_category_time`(`category`, `create_time`) USING BTREE,
  INDEX `idx_biz`(`biz_type`, `biz_id`) USING BTREE,
  INDEX `idx_recycle`(`del_flag`, `delete_time`) USING BTREE,
  INDEX `idx_sys_file_object_name`(`object_name`(191)) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 56 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件中心-文件元数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_file
-- ----------------------------
INSERT INTO `sys_file` VALUES (1, 11, 'SYSTEM', 'IMAGE', 'wgS1Q5B8i97zWP7.thumb.1000_0.jpeg', 'jpeg', 'image/jpeg', 116206, 'file/system/1/2026/06/22/fd2dde26-3f2f-4dbe-a9c7-407af9dc676f.jpeg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-22 20:24:52', 'admin', '2026-06-22 20:56:24', NULL);
INSERT INTO `sys_file` VALUES (2, 11, 'SYSTEM', 'IMAGE', 'avatar.webp', 'webp', 'image/webp', 954, 'file/system/11/2026/06/23/d89af27a-f7ec-4976-bcb5-6f8eb56012b0.webp', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-23 10:21:32', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (3, 11, 'SYSTEM', 'IMAGE', 'avatar1.webp', 'webp', 'image/webp', 2296, 'file/system/11/2026/06/23/843c0465-9dcd-4a2b-9114-c47f54b7c6a6.webp', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-23 10:21:32', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (4, 11, 'SYSTEM', 'IMAGE', 'avatar2.webp', 'webp', 'image/webp', 1214, 'file/system/11/2026/06/23/486ea4df-b73a-461e-a3a2-cb2afa5d9d02.webp', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-23 10:21:32', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (5, 11, 'SYSTEM', 'IMAGE', 'avatar3.webp', 'webp', 'image/webp', 726, 'file/system/11/2026/06/23/95828f45-88c8-425f-9318-410ccf11aa7e.webp', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-23 10:21:32', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (6, 11, 'SYSTEM', 'IMAGE', 'avatar4.webp', 'webp', 'image/webp', 944, 'file/system/11/2026/06/23/c2f0f90b-9280-4c0f-8362-5f767e343b6d.webp', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-23 10:21:32', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (7, 11, 'SYSTEM', 'IMAGE', 'avatar5.webp', 'webp', 'image/webp', 2272, 'file/system/11/2026/06/23/d66bd088-75af-457b-8b05-2ab455ec5110.webp', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-23 10:21:32', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (8, 11, 'SYSTEM', 'IMAGE', 'avatar6.webp', 'webp', 'image/webp', 810, 'file/system/11/2026/06/23/1fa6bcaf-14fd-401d-8d26-adefad2743dd.webp', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-23 10:21:32', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (9, 11, 'SYSTEM', 'IMAGE', '111.webp', 'webp', 'image/webp', 2712, 'file/system/11/2026/06/23/6dd6d4d7-efd7-40c7-929e-0a200020f744.webp', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-23 10:21:33', 'admin', '2026-06-29 18:33:54', NULL);
INSERT INTO `sys_file` VALUES (10, 11, 'SYSTEM', 'IMAGE', 'avatar8.webp', 'webp', 'image/webp', 3946, 'file/system/11/2026/06/23/1918023b-56a8-4b0c-9c8d-ffbdf9bfaa9d.webp', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-23 10:21:33', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (11, 11, 'SYSTEM', 'IMAGE', 'avatar9.webp', 'webp', 'image/webp', 1680, 'file/system/11/2026/06/23/7ac4e22d-bb50-4cbc-bae9-4c9683107a95.webp', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-23 10:21:33', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (12, 11, 'SYSTEM', 'IMAGE', 'avatar10.webp', 'webp', 'image/webp', 1410, 'file/system/11/2026/06/23/fa28a03b-91de-4895-8153-61fe0cb2006a.webp', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-23 10:21:33', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (13, 7, 'GOODS', 'IMAGE', '0d40c24b264aa511.jpg', 'jpg', 'image/jpeg', 471413, 'file/goods/7/2026/06/24/b170c25a-1635-4629-b095-c454a1c50894.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:12', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (14, 7, 'GOODS', 'IMAGE', '1f15cdbcf9e1273c.jpg', 'jpg', 'image/jpeg', 354165, 'file/goods/7/2026/06/24/433270a0-8431-49a5-8ffd-4b342847f04c.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:13', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (15, 7, 'GOODS', 'IMAGE', '2b1837c6c50add30.jpg', 'jpg', 'image/jpeg', 62706, 'file/goods/7/2026/06/24/9aede9a0-2c83-4764-9e30-e5c67d59fa33.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:19', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (16, 7, 'GOODS', 'IMAGE', '3c24f9cd69534030.jpg', 'jpg', 'image/jpeg', 80270, 'file/goods/7/2026/06/24/4a3576b4-0bc7-4e7c-b91a-c07ba16c7c59.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:19', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (17, 7, 'GOODS', 'IMAGE', '5b5e74d0978360a1.jpg', 'jpg', 'image/jpeg', 94770, 'file/goods/7/2026/06/24/5d79391d-4e02-4111-a780-369792415d04.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:20', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (18, 7, 'GOODS', 'IMAGE', '6a1b2703a9ed8737.jpg', 'jpg', 'image/jpeg', 208285, 'file/goods/7/2026/06/24/ec5d97d0-480f-4d70-b0b6-12d85cf03922.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:21', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (19, 7, 'GOODS', 'IMAGE', '7ae0120ec27dc3a7.jpg', 'jpg', 'image/jpeg', 93322, 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:21', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (20, 7, 'GOODS', 'IMAGE', '8bf441260bffa42f.jpg', 'jpg', 'image/jpeg', 330594, 'file/goods/7/2026/06/24/51d09e50-ba36-4b64-8841-09be05494012.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:21', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (21, 7, 'GOODS', 'IMAGE', '23cd65077f12f7f5.jpg', 'jpg', 'image/jpeg', 74976, 'file/goods/7/2026/06/24/14decc80-a9ef-4c51-b7a4-31719040a9f2.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:22', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (22, 7, 'GOODS', 'IMAGE', '23d9fbb256ea5d4a.jpg', 'jpg', 'image/jpeg', 204180, 'file/goods/7/2026/06/24/66c02c8f-18e8-4bc2-a52a-bd99f393371b.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:22', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (23, 7, 'GOODS', 'IMAGE', '28f296629cca865e.jpg', 'jpg', 'image/jpeg', 206140, 'file/goods/7/2026/06/24/72aeef80-6332-413d-be60-66ec87c9fdd3.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:23', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (24, 7, 'GOODS', 'IMAGE', '63e862164165f483.jpg', 'jpg', 'image/jpeg', 217921, 'file/goods/7/2026/06/24/c73e27d0-c641-4c65-be76-0e6e7e2d95cd.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:24', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (25, 7, 'GOODS', 'IMAGE', '73ab4d2e818d2211.jpg', 'jpg', 'image/jpeg', 189204, 'file/goods/7/2026/06/24/4c5dfd9a-4ee1-4d47-bb43-d9e360cb3b46.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:24', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (26, 7, 'GOODS', 'IMAGE', '335b2c690e43a8f8.jpg', 'jpg', 'image/jpeg', 167396, 'file/goods/7/2026/06/24/f817b594-3fe0-432f-958c-c6b90225f42a.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:24', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (27, 7, 'GOODS', 'IMAGE', '749d8efdff062fb0.jpg', 'jpg', 'image/jpeg', 81690, 'file/goods/7/2026/06/24/b9f7db28-cdde-4754-b0d5-4900449c5f55.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:56', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (28, 7, 'GOODS', 'IMAGE', '919c850652e98031.jpg', 'jpg', 'image/jpeg', 236342, 'file/goods/7/2026/06/24/991b7c54-9d76-4000-89f3-01b71867ffe6.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:56', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (29, 7, 'GOODS', 'IMAGE', '73366cc235d68202.jpg', 'jpg', 'image/jpeg', 201802, 'file/goods/7/2026/06/24/2d916e79-007c-4948-af12-85471e862cfc.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:41:57', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (30, 7, 'GOODS', 'IMAGE', 'a0b8774404ae8a2c.jpg', 'jpg', 'image/jpeg', 389564, 'file/goods/7/2026/06/24/24f6ff4a-335f-4182-89f0-c133354d8430.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:05', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (31, 7, 'GOODS', 'IMAGE', 'a2c208410ae84d1f.jpg', 'jpg', 'image/jpeg', 80068, 'file/goods/7/2026/06/24/09a6cad1-de3d-4d0c-b742-9fa15e307a23.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:15', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (32, 7, 'GOODS', 'IMAGE', 'a64c5d2d08c1e498.jpg', 'jpg', 'image/jpeg', 380784, 'file/goods/7/2026/06/24/27a95333-8dcb-45cc-b0cc-22c4a04cc371.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:15', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (33, 7, 'GOODS', 'IMAGE', 'a83bf5250e14caf2.jpg', 'jpg', 'image/jpeg', 353222, 'file/goods/7/2026/06/24/0850a4f2-071d-47cb-ae0f-b49693048a89.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:15', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (34, 7, 'GOODS', 'IMAGE', 'apple.png', 'png', 'image/png', 4399, 'file/goods/7/2026/06/24/82724967-2603-42fc-967b-70190d46b8bb.png', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:17', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (35, 7, 'GOODS', 'IMAGE', 'b5c6b23d01dcdf81.jpg', 'jpg', 'image/jpeg', 253111, 'file/goods/7/2026/06/24/b43dfa24-3102-41c0-9110-0e1d8154bd14.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:17', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (36, 7, 'GOODS', 'IMAGE', 'b44cb3818522310d.jpg', 'jpg', 'image/jpeg', 281816, 'file/goods/7/2026/06/24/e615c28a-6dce-4a5f-ad54-34edbd7a9155.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:17', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (37, 7, 'GOODS', 'IMAGE', 'b8494bf281991f94.jpg', 'jpg', 'image/jpeg', 75318, 'file/goods/7/2026/06/24/f083e666-22f9-40c9-bacb-27a450274f69.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:18', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (38, 7, 'GOODS', 'IMAGE', 'b9840ff8da7762ae.jpg', 'jpg', 'image/jpeg', 194127, 'file/goods/7/2026/06/24/868290e1-9f5c-4106-855b-d892c10dd626.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:21', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (39, 7, 'GOODS', 'IMAGE', 'b094601548ddcb1b.jpg', 'jpg', 'image/jpeg', 237096, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:21', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (40, 7, 'GOODS', 'IMAGE', 'ccd1077b985c7150.jpg', 'jpg', 'image/jpeg', 97051, 'file/goods/7/2026/06/24/5ccb5bb1-0c8d-4e3e-bd26-15a8b7e9b673.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:21', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (41, 7, 'GOODS', 'IMAGE', 'd08df10ef411470b.jpg', 'jpg', 'image/jpeg', 290527, 'file/goods/7/2026/06/24/47cc59cd-6839-4120-9cc4-6e84b6ba60c7.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:22', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (42, 7, 'GOODS', 'IMAGE', 'd511faab82abb34b.jpg', 'jpg', 'image/jpeg', 253404, 'file/goods/7/2026/06/24/2fce83b3-d30d-4d2b-8ea0-9b470c3e9aba.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:42:22', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (43, 7, 'GOODS', 'IMAGE', 'e07b540657023162.jpg', 'jpg', 'image/jpeg', 93322, 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:44:17', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (44, 7, 'GOODS', 'IMAGE', 'e3284f319e256a5d.jpg', 'jpg', 'image/jpeg', 116452, 'file/goods/7/2026/06/24/64c4ac0c-539d-4e98-a406-364908c2e436.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:44:18', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (45, 7, 'GOODS', 'IMAGE', 'f205d9c99a2b4b01.jpg', 'jpg', 'image/jpeg', 335252, 'file/goods/7/2026/06/24/f53f9236-93cd-48a0-b683-8f947c56103d.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:44:18', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (46, 7, 'GOODS', 'IMAGE', 'f753fc5c13707e7e.jpg', 'jpg', 'image/jpeg', 475311, 'file/goods/7/2026/06/24/25f2770c-1ece-4830-aeff-df7b6f3495b5.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:44:18', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (47, 7, 'GOODS', 'IMAGE', 'f6982a3217eb2fa3.jpg', 'jpg', 'image/jpeg', 71475, 'file/goods/7/2026/06/24/d5d51f3e-8636-442c-af3e-a67357ea09bb.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:44:18', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (48, 7, 'GOODS', 'IMAGE', 'fe215589ed6500f4.jpg', 'jpg', 'image/jpeg', 164177, 'file/goods/7/2026/06/24/0150dfe7-42d3-49a2-9f22-2bbdf09f3746.jpg', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:44:19', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (49, 7, 'GOODS', 'IMAGE', 'huawei.png', 'png', 'image/png', 4643, 'file/goods/7/2026/06/24/2ef306e3-6133-4563-8a36-50698ffc629f.png', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:44:19', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (50, 7, 'GOODS', 'IMAGE', 'oppo.png', 'png', 'image/png', 4484, 'file/goods/7/2026/06/24/c82cea38-1086-4024-84ca-9a6e1b7b503f.png', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:44:19', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (51, 7, 'GOODS', 'IMAGE', 'xiaomi.png', 'png', 'image/png', 4934, 'file/goods/7/2026/06/24/83a59e75-95c9-4821-b0f7-b6681b3db3a9.png', NULL, 'OSS', NULL, NULL, '0', NULL, NULL, 'admin', '2026-06-24 15:44:19', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (52, 7, 'GOODS', 'IMAGE', 'huawei.png', 'png', 'image/png', 4643, 'file/goods/7/2026/06/24/8d0c6883-6e19-4b23-ac40-4d4634e3f738.png', NULL, 'OSS', 'mall', NULL, '0', NULL, NULL, 'admin', '2026-06-24 18:44:34', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (53, 7, 'GOODS', 'IMAGE', 'xiaomi.png', 'png', 'image/png', 4934, 'file/goods/7/2026/06/24/50c9014f-3529-44ff-b493-e10d4436c310.png', NULL, 'OSS', 'mall', NULL, '0', NULL, NULL, 'admin', '2026-06-24 18:44:42', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (54, 7, 'GOODS', 'IMAGE', 'oppo.png', 'png', 'image/png', 4484, 'file/goods/7/2026/06/24/92528f97-af90-4483-838f-bf4c013ad490.png', NULL, 'OSS', 'mall', NULL, '0', NULL, NULL, 'admin', '2026-06-24 18:44:50', '', NULL, NULL);
INSERT INTO `sys_file` VALUES (55, 7, 'GOODS', 'IMAGE', 'apple.png', 'png', 'image/png', 4399, 'file/goods/7/2026/06/24/c438169e-e441-47c0-9730-1d126298f5bb.png', NULL, 'OSS', 'mall', NULL, '0', NULL, NULL, 'admin', '2026-06-24 18:44:59', '', NULL, NULL);

-- ----------------------------
-- Table structure for sys_file_folder
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_folder`;
CREATE TABLE `sys_file_folder`  (
  `folder_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',
  `category` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务分类 SYSTEM/OA/CONTRACT/CERT/PROJECT/CUSTOMER/GOODS/FINANCE/HR/OTHER',
  `folder_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文件夹名称',
  `parent_id` bigint(0) NOT NULL DEFAULT 0 COMMENT '父文件夹ID（双层结构固定为0）',
  `order_num` int(0) NULL DEFAULT 0 COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`folder_id`) USING BTREE,
  UNIQUE INDEX `uk_category_folder`(`category`, `folder_name`, `del_flag`) USING BTREE,
  INDEX `idx_category_del`(`category`, `del_flag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件中心-文件夹' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_file_folder
-- ----------------------------
INSERT INTO `sys_file_folder` VALUES (1, 'SYSTEM', '默认', 0, 1, '0', '0', 'admin', '2026-06-22 19:45:35', '', NULL, '系统通用-默认文件夹');
INSERT INTO `sys_file_folder` VALUES (2, 'OA', '默认', 0, 1, '0', '0', 'admin', '2026-06-22 19:45:35', '', NULL, '办公审批-默认文件夹');
INSERT INTO `sys_file_folder` VALUES (3, 'CONTRACT', '默认', 0, 1, '0', '0', 'admin', '2026-06-22 19:45:35', '', NULL, '合同档案-默认文件夹');
INSERT INTO `sys_file_folder` VALUES (4, 'CERT', '默认', 0, 1, '0', '0', 'admin', '2026-06-22 19:45:35', '', NULL, '资质证件-默认文件夹');
INSERT INTO `sys_file_folder` VALUES (5, 'PROJECT', '默认', 0, 1, '0', '0', 'admin', '2026-06-22 19:45:35', '', NULL, '项目资料-默认文件夹');
INSERT INTO `sys_file_folder` VALUES (6, 'CUSTOMER', '默认', 0, 1, '0', '0', 'admin', '2026-06-22 19:45:35', '', NULL, '客户资料-默认文件夹');
INSERT INTO `sys_file_folder` VALUES (7, 'GOODS', '默认', 0, 1, '0', '0', 'admin', '2026-06-22 19:45:35', '', NULL, '商品素材-默认文件夹');
INSERT INTO `sys_file_folder` VALUES (8, 'FINANCE', '默认', 0, 1, '0', '0', 'admin', '2026-06-22 19:45:35', '', NULL, '财务单据-默认文件夹');
INSERT INTO `sys_file_folder` VALUES (9, 'HR', '默认', 0, 1, '0', '0', 'admin', '2026-06-22 19:45:35', '', NULL, '人事档案-默认文件夹');
INSERT INTO `sys_file_folder` VALUES (10, 'OTHER', '默认', 0, 1, '0', '0', 'admin', '2026-06-22 19:45:35', '', NULL, '其他附件-默认文件夹');
INSERT INTO `sys_file_folder` VALUES (11, 'SYSTEM', '头像', 0, 1, '0', '0', 'admin', '2026-06-22 20:40:56', '', NULL, '存储用户头像');

-- ----------------------------
-- Table structure for sys_file_ref
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_ref`;
CREATE TABLE `sys_file_ref`  (
  `ref_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '引用ID',
  `file_id` bigint(0) NOT NULL COMMENT '文件ID',
  `biz_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型',
  `biz_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务主键',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`ref_id`) USING BTREE,
  UNIQUE INDEX `uk_file_biz`(`file_id`, `biz_type`, `biz_id`) USING BTREE,
  INDEX `idx_file_id`(`file_id`) USING BTREE,
  INDEX `idx_biz`(`biz_type`, `biz_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件业务引用计数' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_file_ref
-- ----------------------------
INSERT INTO `sys_file_ref` VALUES (1, 52, 'mall_brand', '9', 'admin', '2026-06-29 18:46:55');

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
  `job_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT 'cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '1' COMMENT '是否并发执行（0允许 1禁止）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '备注信息',
  PRIMARY KEY (`job_id`, `job_name`, `job_group`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '定时任务调度表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job
-- ----------------------------
INSERT INTO `sys_job` VALUES (1, '定时清空操作日志', 'DEFAULT', 'com.star.pivot.quartz.task.CleanOperLogTask.cleanOperLog()', '0 0 2 ? * MON', '3', '1', '0', 'admin', '2026-02-06 20:41:50', 'admin', '2026-05-18 23:48:38', '每天凌晨2点清空操作日志表 sys_oper_log');

-- ----------------------------
-- Table structure for sys_job_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_job_log`;
CREATE TABLE `sys_job_log`  (
  `job_log_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '日志信息',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '执行状态（0正常 1失败）',
  `exception_info` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '异常信息',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`job_log_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 59 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '定时任务调度日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_job_log
-- ----------------------------
INSERT INTO `sys_job_log` VALUES (52, '定时清空操作日志', 'DEFAULT', 'com.star.pivot.quartz.task.CleanOperLogTask.cleanOperLog()', '执行成功', '0', '', '2026-04-23 20:12:00');
INSERT INTO `sys_job_log` VALUES (53, '定时清空操作日志', 'DEFAULT', 'com.star.pivot.quartz.task.CleanOperLogTask.cleanOperLog()', '执行成功', '0', '', '2026-04-27 02:00:00');
INSERT INTO `sys_job_log` VALUES (54, '定时清空操作日志', 'DEFAULT', 'com.star.pivot.quartz.task.CleanOperLogTask.cleanOperLog()', '执行成功', '0', '', '2026-05-18 23:48:33');
INSERT INTO `sys_job_log` VALUES (55, '定时清空操作日志', 'DEFAULT', 'com.star.pivot.quartz.task.CleanOperLogTask.cleanOperLog()', '执行成功', '0', '', '2026-05-25 02:00:00');
INSERT INTO `sys_job_log` VALUES (56, '定时清空操作日志', 'DEFAULT', 'com.star.pivot.quartz.task.CleanOperLogTask.cleanOperLog()', '执行成功', '0', '', '2026-06-01 02:00:00');
INSERT INTO `sys_job_log` VALUES (57, '定时清空操作日志', 'DEFAULT', 'com.star.pivot.quartz.task.CleanOperLogTask.cleanOperLog()', '执行成功', '0', '', '2026-06-02 12:34:21');
INSERT INTO `sys_job_log` VALUES (58, '定时清空操作日志', 'DEFAULT', 'com.star.pivot.quartz.task.CleanOperLogTask.cleanOperLog()', '执行成功', '0', '', '2026-06-08 02:00:00');
INSERT INTO `sys_job_log` VALUES (59, '定时清空操作日志', 'DEFAULT', 'com.star.pivot.quartz.task.CleanOperLogTask.cleanOperLog()', '执行成功', '0', '', '2026-06-15 02:00:00');

-- ----------------------------
-- Table structure for sys_logininfor
-- ----------------------------
DROP TABLE IF EXISTS `sys_logininfor`;
CREATE TABLE `sys_logininfor`  (
  `info_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '访问ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '用户账号',
  `ipaddr` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '登录状态（0成功 1失败）',
  `msg` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '提示消息',
  `login_time` datetime(0) NULL DEFAULT NULL COMMENT '访问时间',
  PRIMARY KEY (`info_id`) USING BTREE,
  INDEX `idx_sys_logininfor_s`(`status`) USING BTREE,
  INDEX `idx_login_time`(`login_time`) USING BTREE,
  INDEX `idx_user_name`(`user_name`) USING BTREE,
  INDEX `idx_user_time`(`user_name`, `login_time`) USING BTREE,
  INDEX `idx_ipaddr`(`ipaddr`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_logininfor_time`(`login_time`) USING BTREE,
  INDEX `idx_logininfor_user`(`user_name`) USING BTREE,
  INDEX `idx_logininfor_ip`(`ipaddr`) USING BTREE,
  INDEX `idx_login_user_time`(`user_name`, `login_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 507 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统访问记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_logininfor
-- ----------------------------
INSERT INTO `sys_logininfor` VALUES (466, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-22 17:29:56');
INSERT INTO `sys_logininfor` VALUES (467, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-22 19:45:48');
INSERT INTO `sys_logininfor` VALUES (468, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-22 19:46:01');
INSERT INTO `sys_logininfor` VALUES (469, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-23 10:10:51');
INSERT INTO `sys_logininfor` VALUES (470, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-23 10:11:29');
INSERT INTO `sys_logininfor` VALUES (471, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-23 14:48:34');
INSERT INTO `sys_logininfor` VALUES (472, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-23 15:53:55');
INSERT INTO `sys_logininfor` VALUES (473, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-23 19:26:45');
INSERT INTO `sys_logininfor` VALUES (474, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-23 19:27:03');
INSERT INTO `sys_logininfor` VALUES (475, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '1', '密码错误', '2026-06-23 19:30:19');
INSERT INTO `sys_logininfor` VALUES (476, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '1', '密码错误', '2026-06-23 19:31:13');
INSERT INTO `sys_logininfor` VALUES (477, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '1', '密码错误', '2026-06-23 19:32:08');
INSERT INTO `sys_logininfor` VALUES (478, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-23 21:27:16');
INSERT INTO `sys_logininfor` VALUES (479, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-24 12:43:02');
INSERT INTO `sys_logininfor` VALUES (480, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-24 15:45:48');
INSERT INTO `sys_logininfor` VALUES (481, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-24 16:03:27');
INSERT INTO `sys_logininfor` VALUES (482, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-26 13:00:39');
INSERT INTO `sys_logininfor` VALUES (483, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '1', '密码错误', '2026-06-26 14:23:51');
INSERT INTO `sys_logininfor` VALUES (484, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '1', '密码错误', '2026-06-26 14:28:13');
INSERT INTO `sys_logininfor` VALUES (485, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '1', '密码错误', '2026-06-26 14:29:06');
INSERT INTO `sys_logininfor` VALUES (486, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '1', '密码错误', '2026-06-26 14:29:41');
INSERT INTO `sys_logininfor` VALUES (487, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '0', '登录成功', '2026-06-26 14:32:42');
INSERT INTO `sys_logininfor` VALUES (488, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '0', '登录成功', '2026-06-26 14:35:03');
INSERT INTO `sys_logininfor` VALUES (489, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '0', '登录成功', '2026-06-26 14:36:16');
INSERT INTO `sys_logininfor` VALUES (490, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '0', '登录成功', '2026-06-26 14:39:52');
INSERT INTO `sys_logininfor` VALUES (491, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '0', '登录成功', '2026-06-26 14:40:29');
INSERT INTO `sys_logininfor` VALUES (492, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '0', '登录成功', '2026-06-26 14:41:16');
INSERT INTO `sys_logininfor` VALUES (493, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '0', '登录成功', '2026-06-26 14:41:26');
INSERT INTO `sys_logininfor` VALUES (494, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '0', '登录成功', '2026-06-26 14:42:44');
INSERT INTO `sys_logininfor` VALUES (495, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Unknown', 'Windows 10', '0', '登录成功', '2026-06-26 14:56:01');
INSERT INTO `sys_logininfor` VALUES (496, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-26 18:33:45');
INSERT INTO `sys_logininfor` VALUES (497, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-26 18:58:00');
INSERT INTO `sys_logininfor` VALUES (498, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-29 17:06:41');
INSERT INTO `sys_logininfor` VALUES (499, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-29 18:19:41');
INSERT INTO `sys_logininfor` VALUES (500, 'admin', '172.19.0.1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-30 06:04:46');
INSERT INTO `sys_logininfor` VALUES (501, 'admin', '172.19.0.1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-30 06:05:51');
INSERT INTO `sys_logininfor` VALUES (502, 'admin', '172.19.0.1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-30 06:13:27');
INSERT INTO `sys_logininfor` VALUES (503, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-30 15:31:23');
INSERT INTO `sys_logininfor` VALUES (504, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-30 15:44:50');
INSERT INTO `sys_logininfor` VALUES (505, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-30 16:13:48');
INSERT INTO `sys_logininfor` VALUES (506, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-06-30 18:09:14');
INSERT INTO `sys_logininfor` VALUES (507, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-07-01 11:13:07');
INSERT INTO `sys_logininfor` VALUES (508, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-07-01 11:30:11');
INSERT INTO `sys_logininfor` VALUES (509, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-07-01 16:18:22');
INSERT INTO `sys_logininfor` VALUES (510, 'admin', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome', 'Windows 10', '0', '登录成功', '2026-07-01 16:40:28');

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int(0) NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '路由名称',
  `is_frame` int(0) NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int(0) NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 271 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, '/system', '', NULL, 'System', 1, 1, 'M', '0', '0', '', 'ep:setting', 'system', '2025-12-31 16:34:16', 'admin', '2026-06-29 18:23:27', '系统管理模块111');
INSERT INTO `sys_menu` VALUES (2, '系统工具', 0, 2, '/tools', '', NULL, 'SystemTools', 1, 1, 'M', '0', '0', '', 'clarity:tools-line', 'admin', '2026-01-20 13:08:43', 'admin', '2026-04-19 19:09:23', '系统工具');
INSERT INTO `sys_menu` VALUES (3, '系统监控', 0, 3, '/monitor', '', NULL, 'Monitor', 1, 1, 'M', '0', '0', '', 'material-symbols:monitor-outline', 'admin', '2026-01-25 18:00:43', 'admin', '2026-05-18 15:42:45', '系统监控模块');
INSERT INTO `sys_menu` VALUES (4, '文件中心', 0, 4, '/file', '', NULL, 'FileCenter', 1, 1, 'M', '0', '0', '', 'ep:folder-opened', 'admin', '2026-06-22 19:45:35', '', NULL, '文件中心模块');
INSERT INTO `sys_menu` VALUES (5, '商城系统', 0, 5, '/mall', '', NULL, 'MallSystem', 1, 1, 'M', '0', '0', '', 'ep:goods', 'admin', '2026-04-23 20:23:16', 'admin', '2026-04-23 20:26:06', '商城系统');
INSERT INTO `sys_menu` VALUES (6, '审批中心', 0, 6, '/approval', '', NULL, 'ApprovalCenter', 1, 1, 'M', '0', '0', '', 'mdi:clipboard-check-outline', 'admin', '2026-06-26 10:49:49', '', NULL, 'SAS 审批中台');
INSERT INTO `sys_menu` VALUES (7, '星枢项目', 0, 99, 'https://gitee.com/xin1998/StarPivot', '', NULL, '', 0, 1, 'M', '0', '0', '', 'ri:gitee-fill', 'admin', '2026-04-21 12:48:19', 'admin', '2026-05-18 22:44:22', '星枢项目');
INSERT INTO `sys_menu` VALUES (8, 'art-design-pro', 0, 100, 'https://gitee.com/lingchen163/art-design-pro', '', NULL, '', 0, 1, 'M', '0', '0', '', 'ri:guide-fill', 'admin', '2026-04-19 19:07:54', 'admin', '2026-04-23 20:26:22', '');
INSERT INTO `sys_menu` VALUES (9, '菜单管理', 1, 1, 'menu', '/system/menu', NULL, 'SystemMenu', 1, 1, 'C', '0', '0', 'system:menu:list', 'ep:menu', 'system', '2025-12-31 16:34:16', '', '2026-01-02 21:12:33', '菜单管理模块');
INSERT INTO `sys_menu` VALUES (10, '角色管理', 1, 2, 'role', '/system/role', NULL, 'SystemRole', 1, 1, 'C', '0', '0', 'system:role:list', 'oui:app-users-roles', 'system', '2025-12-31 16:34:16', 'admin', '2026-06-08 16:46:55', '角色管理模块');
INSERT INTO `sys_menu` VALUES (11, '用户管理', 1, 3, 'user', '/system/user', NULL, 'SystemUser', 1, 1, 'C', '0', '0', 'system:user:list', 'mdi:user', 'system', '2025-12-31 16:34:16', '', '2026-01-02 21:31:51', '用户管理模块');
INSERT INTO `sys_menu` VALUES (12, '部门管理', 1, 4, 'dept', '/system/dept', NULL, 'SystemDept', 1, 1, 'C', '0', '0', 'system:dept:list', 'ri:organization-chart', '', '2026-01-02 21:04:34', '', '2026-01-02 21:36:43', '部门管理模块');
INSERT INTO `sys_menu` VALUES (13, '岗位管理', 1, 5, 'post', '/system/post/index', NULL, 'PostManage', 1, 1, 'C', '0', '0', 'system:post:list', 'picon:post', 'xinxin', '2026-01-04 19:23:51', 'xinxin', '2026-01-04 19:25:02', '岗位管理模块');
INSERT INTO `sys_menu` VALUES (14, '字典管理', 1, 6, 'dict', '/system/dict/index', NULL, 'DictManage', 1, 1, 'C', '0', '0', 'system:type:list', 'arcticons:zdict', 'admin', '2026-01-05 12:28:54', 'admin', '2026-01-19 21:37:20', '字典管理模块。有：字典数据   字典类型');
INSERT INTO `sys_menu` VALUES (15, '日志管理', 1, 7, 'log', '', NULL, 'LogManager', 1, 1, 'M', '0', '0', '', 'mdi:math-log', 'admin', '2026-01-23 13:37:05', 'admin', '2026-05-15 09:09:47', '');
INSERT INTO `sys_menu` VALUES (16, '通知公告', 1, 8, 'notice', '/system/notice/index', NULL, 'NoticeManage', 1, 0, 'C', '0', '0', 'system:notice:list', 'fe:notice-active', 'admin', '2026-02-05 17:38:35', 'admin', '2026-03-31 22:03:49', '通知公告菜单');
INSERT INTO `sys_menu` VALUES (17, '参数配置', 1, 9, 'config', '/system/config/index', NULL, 'ConfigManage', 1, 1, 'C', '0', '0', 'system:config:list', 'mynaui:config', 'admin', '2026-03-31 22:03:28', 'admin', '2026-03-31 22:05:20', '参数配置菜单');
INSERT INTO `sys_menu` VALUES (18, '操作日志', 15, 1, 'oper', '/system/log/oper/index', NULL, 'OperLog', 1, 1, 'C', '0', '0', 'system:log:list', 'icon-park-solid:log', 'admin', '2026-01-23 13:40:41', '', NULL, '操作日志');
INSERT INTO `sys_menu` VALUES (19, '登录日志', 15, 2, 'login', '/system/log/login/index', NULL, 'LoginInfoLog', 1, 1, 'C', '0', '0', 'system:login:list', 'icon-park-solid:log', 'admin', '2026-01-23 13:51:37', '', NULL, '登录日志');
INSERT INTO `sys_menu` VALUES (20, '代码生成', 2, 1, 'generator', '/tools/generator/index', NULL, 'GenerateTools', 1, 1, 'C', '0', '0', 'tools:generator:list', 'mdi:generator-mobile', 'admin', '2026-01-20 13:15:59', 'admin', '2026-01-20 13:25:42', '代码生成');
INSERT INTO `sys_menu` VALUES (21, '外部库代码生成', 2, 2, 'external', '/tools/generator-external/index', NULL, '', 1, 1, 'C', '0', '0', '', 'ep:document-add', 'admin', '2026-06-02 18:13:11', 'admin', '2026-06-02 18:30:43', '');
INSERT INTO `sys_menu` VALUES (22, '服务器监控', 3, 1, 'server', '/monitor/server/index', NULL, 'ServerMonitor', 1, 0, 'C', '0', '0', 'monitor:server:query', 'ri:server-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-01-25 18:33:45', '服务器信息监控');
INSERT INTO `sys_menu` VALUES (23, 'Druid监控', 3, 2, 'druid', '/monitor/druid/index', NULL, 'DruidMonitor', 1, 0, 'C', '0', '0', 'monitor:druid:query', 'ri:database-2-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-01-25 18:32:03', 'Druid数据库连接池监控');
INSERT INTO `sys_menu` VALUES (24, 'Redis缓存', 3, 3, 'redis', '/monitor/redis/index', NULL, 'RedisMonitor', 1, 0, 'C', '0', '0', 'monitor:redis:query', 'ri:database-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-03-01 13:37:51', 'Redis缓存监控');
INSERT INTO `sys_menu` VALUES (25, '在线用户', 3, 4, 'online', '/monitor/online/index', NULL, 'OnlineUser', 1, 0, 'C', '0', '0', 'monitor:online:query', 'ri:user-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-01-25 18:32:45', '在线用户监控');
INSERT INTO `sys_menu` VALUES (26, '定时任务', 3, 5, 'job', '/monitor/job/index', NULL, 'MonitorJob', 1, 1, 'C', '0', '0', 'monitor:job:query', 'ri:time-line', 'admin', '2026-02-06 19:58:43', 'admin', '2026-02-06 20:34:11', '定时任务调度');
INSERT INTO `sys_menu` VALUES (27, '文件管理', 4, 1, 'index', '/file/index', NULL, 'FileManage', 1, 1, 'C', '0', '0', 'file:resource:list', 'ep:document', 'admin', '2026-06-22 19:45:35', '', NULL, '文件管理页面');
INSERT INTO `sys_menu` VALUES (28, 'C端商城', 5, 0, '/portal', '', NULL, 'MallPortal', 1, 1, 'C', '0', '0', '', 'ep:shopping-bag', 'admin', '2026-06-24 13:11:09', '', NULL, '打开 C 端商城前台');
INSERT INTO `sys_menu` VALUES (29, '商品管理', 5, 1, 'pms', '', NULL, 'GoodManager', 1, 1, 'M', '0', '0', '', 'ep:goods-filled', 'admin', '2026-05-15 10:38:29', 'admin', '2026-06-23 19:58:02', '');
INSERT INTO `sys_menu` VALUES (30, '仓库系统', 5, 1, 'wms', '', NULL, 'WmsManager', 1, 1, 'M', '0', '0', '', 'mdi:warehouse', 'admin', '2026-05-19 15:26:07', 'admin', '2026-06-23 16:45:46', '仓储服务');
INSERT INTO `sys_menu` VALUES (31, '优惠营销', 5, 1, 'sms', '', NULL, 'PromotionMarketing', 1, 1, 'M', '0', '0', '', 'ep:promotion', 'admin', '2026-06-23 16:55:30', 'admin', '2026-06-23 19:58:27', '');
INSERT INTO `sys_menu` VALUES (32, '订单系统', 5, 4, 'oms', '', NULL, 'OrderManager', 1, 1, 'M', '0', '0', '', 'mdi:order-bool-descending-variant', 'admin', '2026-06-23 17:07:36', 'admin', '2026-06-23 19:59:00', '');
INSERT INTO `sys_menu` VALUES (33, '用户系统', 5, 5, 'ums', '', NULL, 'MemberSystem', 1, 1, 'M', '0', '0', '', 'mdi:wallet-membership', 'admin', '2026-06-23 17:12:59', 'admin', '2026-06-23 19:59:22', '');
INSERT INTO `sys_menu` VALUES (34, '内容管理', 5, 6, 'cms', '', NULL, 'ContentManage', 1, 1, 'M', '0', '0', '', 'mdi:content-paste', 'admin', '2026-06-23 17:17:02', 'admin', '2026-06-23 19:59:30', '');
INSERT INTO `sys_menu` VALUES (35, '审批模板', 6, 1, 'template', '/approval/template/index', NULL, 'ApprovalTemplate', 1, 1, 'C', '0', '0', 'approval:template:query', 'ep:document', 'admin', '2026-06-26 10:49:49', '', NULL, '审批模板配置');
INSERT INTO `sys_menu` VALUES (36, '业务绑定', 6, 2, 'bind', '/approval/bind/index', NULL, 'ApprovalBind', 1, 1, 'C', '0', '0', 'approval:bind:edit', 'ep:link', 'admin', '2026-06-26 10:49:49', '', NULL, '业务类型绑定规则');
INSERT INTO `sys_menu` VALUES (37, '待办审批', 6, 3, 'todo', '/approval/todo/index', NULL, 'ApprovalTodo', 1, 1, 'C', '0', '0', 'approval:task:query', 'ri:bell-line', 'admin', '2026-06-26 10:49:49', '', NULL, '待办与已办');
INSERT INTO `sys_menu` VALUES (38, '我发起的', 6, 4, 'mine', '/approval/mine/index', NULL, 'ApprovalMine', 1, 1, 'C', '0', '0', 'approval:instance:query', 'mdi:user-arrow-right', 'admin', '2026-06-26 10:49:49', '', NULL, '我发起的审批');
INSERT INTO `sys_menu` VALUES (39, '菜单查询', 9, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2026-01-12 17:39:26', '', NULL, '菜单查询');
INSERT INTO `sys_menu` VALUES (40, '菜单添加', 9, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2026-01-12 17:39:50', '', NULL, '菜单添加');
INSERT INTO `sys_menu` VALUES (41, '菜单修改', 9, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2026-01-12 17:40:05', '', NULL, '菜单修改');
INSERT INTO `sys_menu` VALUES (42, '菜单删除', 9, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:menu:delete', '#', 'admin', '2026-01-12 17:40:27', '', NULL, '菜单删除');
INSERT INTO `sys_menu` VALUES (43, '新增角色', 10, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:add', '#', 'admin', '2026-01-12 17:32:13', '', NULL, '新增角色按钮');
INSERT INTO `sys_menu` VALUES (44, '修改角色', 10, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2026-01-12 17:32:45', '', NULL, '修改角色按钮');
INSERT INTO `sys_menu` VALUES (45, '删除角色', 10, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:delete', '#', 'admin', '2026-01-12 17:33:14', '', NULL, '删除角色按钮');
INSERT INTO `sys_menu` VALUES (46, '角色查询', 10, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:query', '#', 'admin', '2026-01-12 17:34:57', '', NULL, '角色查询 按钮');
INSERT INTO `sys_menu` VALUES (47, '分配数据权限', 10, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:assignDataScope', '#', 'admin', '2026-01-16 19:04:22', 'admin', '2026-01-23 20:57:15', '分配角色');
INSERT INTO `sys_menu` VALUES (48, '已分配用户', 10, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:allocatedList', '#', 'admin', '2026-01-23 20:55:10', 'admin', '2026-01-23 22:41:41', '已分配用户');
INSERT INTO `sys_menu` VALUES (49, '未分配用户', 10, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:unallocatedList', '#', 'admin', '2026-01-23 22:41:08', '', NULL, '未分配用户');
INSERT INTO `sys_menu` VALUES (50, '分配用户', 10, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:assignUser', '#', 'admin', '2026-01-23 23:18:49', '', NULL, '分配用户：添加用户角色关联表');
INSERT INTO `sys_menu` VALUES (51, '取消授权', 10, 9, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:cancelUser', '#', 'admin', '2026-01-23 23:26:23', '', NULL, '取消授权');
INSERT INTO `sys_menu` VALUES (52, '新增用户', 11, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:add', '#', 'admin', '2026-01-12 16:42:30', 'admin', '2026-01-16 20:42:35', '新增用户按钮');
INSERT INTO `sys_menu` VALUES (53, '修改状态', 11, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:changeStatus', '#', 'admin', '2026-01-16 18:11:22', 'admin', '2026-01-16 20:42:31', '修改状态');
INSERT INTO `sys_menu` VALUES (54, '修改用户', 11, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2026-01-12 17:30:37', 'admin', '2026-01-16 20:42:26', '修改用户  按钮');
INSERT INTO `sys_menu` VALUES (55, '删除用户', 11, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:delete', '#', 'admin', '2026-01-12 17:31:11', 'admin', '2026-01-16 20:42:39', '删除用户按钮');
INSERT INTO `sys_menu` VALUES (56, '用户查询', 11, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:query', '#', 'admin', '2026-01-12 17:35:27', 'admin', '2026-01-16 20:42:43', '用户查询按钮');
INSERT INTO `sys_menu` VALUES (57, '修改密码', 11, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2026-01-16 18:10:55', 'admin', '2026-01-16 20:42:48', '修改密码');
INSERT INTO `sys_menu` VALUES (58, '解除锁定', 11, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:unLock', '#', 'admin', '2026-01-23 18:49:56', '', NULL, '解除锁定');
INSERT INTO `sys_menu` VALUES (59, '用户导入', 11, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:import', '#', 'admin', '2026-01-24 21:03:18', '', NULL, '用户导入');
INSERT INTO `sys_menu` VALUES (60, '用户导出', 11, 9, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:export', '#', 'admin', '2026-01-24 21:03:52', '', NULL, '用户导出');
INSERT INTO `sys_menu` VALUES (61, '部门查询', 12, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2026-01-12 17:35:58', '', NULL, '部门查询按钮');
INSERT INTO `sys_menu` VALUES (62, '部门新增', 12, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2026-01-12 17:36:28', '', NULL, '部门新增');
INSERT INTO `sys_menu` VALUES (63, '部门修改', 12, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2026-01-12 17:36:49', '', NULL, '部门修改');
INSERT INTO `sys_menu` VALUES (64, '部门删除', 12, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:dept:delete', '#', 'admin', '2026-01-12 17:37:21', '', NULL, '部门删除');
INSERT INTO `sys_menu` VALUES (65, '岗位查询', 13, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:post:query', '#', 'admin', '2026-01-12 17:37:47', '', NULL, '岗位查询');
INSERT INTO `sys_menu` VALUES (66, '岗位新增', 13, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:post:add', '#', 'admin', '2026-01-12 17:38:09', '', NULL, '岗位新增');
INSERT INTO `sys_menu` VALUES (67, '岗位修改', 13, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2026-01-12 17:38:46', '', NULL, '岗位修改');
INSERT INTO `sys_menu` VALUES (68, '岗位删除', 13, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:post:delete', '#', 'admin', '2026-01-12 17:39:04', '', NULL, '岗位删除');
INSERT INTO `sys_menu` VALUES (69, '字典类型添加', 14, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:type:add', '#', 'admin', '2026-01-16 19:08:40', 'admin', '2026-01-16 19:33:35', '字典类型添加');
INSERT INTO `sys_menu` VALUES (70, '字典类型修改', 14, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:type:edit', '#', 'admin', '2026-01-16 19:09:04', 'admin', '2026-01-16 19:33:43', '字典类型修改');
INSERT INTO `sys_menu` VALUES (71, '字典类型删除', 14, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:type:delete', '#', 'admin', '2026-01-16 19:09:27', 'admin', '2026-01-16 19:33:48', '字典类型删除');
INSERT INTO `sys_menu` VALUES (72, '字典类型查询', 14, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:type:query', '#', 'admin', '2026-01-19 21:33:21', '', NULL, '字典类型查询');
INSERT INTO `sys_menu` VALUES (73, '字典数据添加', 14, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:data:add', '#', 'admin', '2026-01-16 19:31:42', '', NULL, '字典数据添加');
INSERT INTO `sys_menu` VALUES (74, '字典数据修改', 14, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:data:edit', '#', 'admin', '2026-01-16 19:32:19', '', NULL, '字典数据修改');
INSERT INTO `sys_menu` VALUES (75, '字典数据删除', 14, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:data:delete', '#', 'admin', '2026-01-16 19:32:51', '', NULL, '字典数据删除');
INSERT INTO `sys_menu` VALUES (76, '字典数据查询', 14, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:data:query', '#', 'admin', '2026-01-19 21:33:59', '', NULL, '字典数据查询');
INSERT INTO `sys_menu` VALUES (77, '日志查询', 18, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:operlog:query', '#', 'admin', '2026-01-23 13:58:02', '', NULL, '日志查询');
INSERT INTO `sys_menu` VALUES (78, '清空日志', 18, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:operlog:delete', '#', 'admin', '2026-01-23 13:57:43', '', NULL, '清空日志');
INSERT INTO `sys_menu` VALUES (79, '日志查询', 19, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:logininfor:query', '#', 'admin', '2026-01-23 14:24:26', '', NULL, '日志查询');
INSERT INTO `sys_menu` VALUES (80, '日志删除', 19, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:logininfor:delete', '#', 'admin', '2026-01-23 14:24:41', '', NULL, '日志删除');
INSERT INTO `sys_menu` VALUES (81, '通知公告查询', 16, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (82, '通知公告新增', 16, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (83, '通知公告修改', 16, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (84, '通知公告删除', 16, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:delete', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (85, '通知公告导出', 16, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:export', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (86, '参数配置查询', 17, 1, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:query', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:23', '');
INSERT INTO `sys_menu` VALUES (87, '参数配置新增', 17, 2, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:add', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:27', '');
INSERT INTO `sys_menu` VALUES (88, '参数配置修改', 17, 3, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:31', '');
INSERT INTO `sys_menu` VALUES (89, '参数配置删除', 17, 4, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:delete', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:36', '');
INSERT INTO `sys_menu` VALUES (90, '参数配置导出', 17, 5, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:export', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:39', '');
INSERT INTO `sys_menu` VALUES (91, '添加', 20, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:add', '#', 'admin', '2026-01-23 15:56:17', 'admin', '2026-01-23 19:01:31', '添加');
INSERT INTO `sys_menu` VALUES (92, '列表查询', 20, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2026-01-20 14:56:43', '', NULL, '列表查询');
INSERT INTO `sys_menu` VALUES (93, '预览', 20, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2026-01-23 15:53:25', 'admin', '2026-01-23 19:01:40', '预览');
INSERT INTO `sys_menu` VALUES (94, '编辑', 20, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2026-01-23 15:53:47', 'admin', '2026-01-23 19:01:44', '');
INSERT INTO `sys_menu` VALUES (95, '删除', 20, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:delete', '#', 'admin', '2026-01-23 15:54:05', 'admin', '2026-01-23 19:01:48', '');
INSERT INTO `sys_menu` VALUES (96, '同步数据库', 20, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:sync', '#', 'admin', '2026-01-23 15:55:18', 'admin', '2026-01-23 19:01:52', '同步数据库');
INSERT INTO `sys_menu` VALUES (97, '生成代码', 20, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:create', '#', 'admin', '2026-01-23 15:55:59', 'admin', '2026-01-23 19:01:56', '生成代码');
INSERT INTO `sys_menu` VALUES (98, '导入', 20, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:import', '#', 'xinxin', '2026-01-24 00:08:41', '', NULL, '导入');
INSERT INTO `sys_menu` VALUES (99, '查询', 21, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:external:query', '#', 'admin', '2026-06-02 19:14:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (100, '预览', 21, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:external:preview', '#', 'admin', '2026-06-02 19:14:43', '', NULL, '');
INSERT INTO `sys_menu` VALUES (101, '生成', 21, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:external:create', '#', 'admin', '2026-06-02 19:15:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (102, '删除缓存', 24, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:cache:remove', '#', 'admin', '2026-01-25 22:19:38', '', NULL, 'monitor:cache:remove');
INSERT INTO `sys_menu` VALUES (103, '清空缓存', 24, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:cache:clear', '#', 'admin', '2026-01-25 22:20:00', '', NULL, '清空缓存');
INSERT INTO `sys_menu` VALUES (104, '缓存列表', 24, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:cache:query', '#', 'admin', '2026-01-27 18:21:55', '', NULL, '缓存列表');
INSERT INTO `sys_menu` VALUES (105, '强制下线', 25, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:online:force-logout', '', 'admin', '2026-01-25 18:00:43', 'admin', '2026-01-25 18:30:55', '强制用户下线');
INSERT INTO `sys_menu` VALUES (106, '任务查询', 26, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2026-02-06 20:01:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (107, '任务新增', 26, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2026-02-06 20:01:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (108, '任务修改', 26, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2026-02-06 20:01:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (109, '任务删除', 26, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:job:delete', '#', 'admin', '2026-02-06 20:01:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (110, '文件查询', 27, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:query', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '文件列表/详情/预览');
INSERT INTO `sys_menu` VALUES (111, '文件上传', 27, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:add', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '上传文件');
INSERT INTO `sys_menu` VALUES (112, '文件删除', 27, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:delete', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '逻辑删除');
INSERT INTO `sys_menu` VALUES (113, '文件恢复', 27, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:restore', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '回收站恢复');
INSERT INTO `sys_menu` VALUES (114, '文件夹查询', 27, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:query', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (115, '文件夹新增', 27, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:add', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (116, '文件夹编辑', 27, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:edit', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (117, '文件夹删除', 27, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:delete', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (118, '文件迁移', 27, 9, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:move', '#', 'admin', '2026-06-22 20:54:50', '', NULL, '迁移到其他文件夹');
INSERT INTO `sys_menu` VALUES (119, '文件重命名', 27, 10, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:edit', '#', 'admin', '2026-06-26 13:05:02', 'admin', '2026-06-26 13:05:12', '');
INSERT INTO `sys_menu` VALUES (120, '永久删除', 27, 11, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:purge', '#', 'admin', '2026-06-29 18:18:07', '', NULL, '回收站永久删除');
INSERT INTO `sys_menu` VALUES (121, '全部分类权限', 27, 12, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:category:all', '#', 'admin', '2026-06-29 18:18:07', '', NULL, '可访问全部文件分类');
INSERT INTO `sys_menu` VALUES (122, '人事档案分类', 27, 13, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:category:HR', '#', 'admin', '2026-06-29 18:18:07', '', NULL, 'HR 分类数据权限');
INSERT INTO `sys_menu` VALUES (123, '财务单据分类', 27, 14, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:category:FINANCE', '#', 'admin', '2026-06-29 18:18:07', '', NULL, 'FINANCE 分类数据权限');
INSERT INTO `sys_menu` VALUES (124, '合同档案分类', 27, 15, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:category:CONTRACT', '#', 'admin', '2026-06-29 18:18:07', '', NULL, 'CONTRACT 分类数据权限');
INSERT INTO `sys_menu` VALUES (125, '资质证件分类', 27, 16, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:category:CERT', '#', 'admin', '2026-06-29 18:18:07', '', NULL, 'CERT 分类数据权限');
INSERT INTO `sys_menu` VALUES (126, '分类维护', 29, 1, 'category', '/mall/pms/category/index', NULL, 'CategoryManager', 1, 1, 'C', '0', '0', 'mall:category:list', 'ri:node-tree', 'admin', '2026-05-15 10:43:03', 'admin', '2026-06-23 21:13:33', '');
INSERT INTO `sys_menu` VALUES (127, '品牌列表', 29, 2, 'brand', '/mall/pms/brand/index', NULL, 'BrandManager', 1, 1, 'C', '0', '0', 'mall:brand:list', 'ep:shop', 'admin', '2026-04-23 20:25:38', 'admin', '2026-06-23 21:13:40', '品牌管理');
INSERT INTO `sys_menu` VALUES (128, '平台属性', 29, 3, 'platform-attributes', '', NULL, 'PlatformAttributes', 1, 1, 'M', '0', '0', '', 'mdi:tools', 'admin', '2026-05-18 14:43:45', 'admin', '2026-05-18 15:04:12', '');
INSERT INTO `sys_menu` VALUES (129, '商品维护', 29, 4, 'maintain', '', NULL, 'GoodsMaintain', 1, 1, 'M', '0', '0', 'mall:product:list', 'ep:goods-filled', 'admin', '2026-04-23 20:27:38', 'admin', '2026-06-24 13:11:09', '商品管理');
INSERT INTO `sys_menu` VALUES (130, '分类维护查询', 126, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:category:query', '#', 'admin', '2026-05-18 22:50:11', '', NULL, '分类维护查询');
INSERT INTO `sys_menu` VALUES (131, '分类维护添加', 126, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:category:add', '#', 'admin', '2026-05-18 22:50:41', '', NULL, '分类维护添加');
INSERT INTO `sys_menu` VALUES (132, '分类维护删除', 126, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:category:delete', '#', 'admin', '2026-05-18 22:51:06', '', NULL, '分类维护删除');
INSERT INTO `sys_menu` VALUES (133, '分类维护修改', 126, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:category:edit', '#', 'admin', '2026-05-18 22:51:30', '', NULL, '分类维护修改');
INSERT INTO `sys_menu` VALUES (134, '品牌列表查询', 127, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:brand:query', '#', 'admin', '2026-05-18 22:46:48', '', NULL, '品牌列表查询');
INSERT INTO `sys_menu` VALUES (135, '品牌添加', 127, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:brand:add', '#', 'admin', '2026-05-18 22:47:16', '', NULL, '品牌添加');
INSERT INTO `sys_menu` VALUES (136, '品牌修改', 127, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:brand:edit', '#', 'admin', '2026-05-18 22:47:44', '', NULL, '品牌修改');
INSERT INTO `sys_menu` VALUES (137, '品牌删除', 127, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:brand:delete', '#', 'admin', '2026-05-18 22:48:06', '', NULL, '品牌删除');
INSERT INTO `sys_menu` VALUES (138, '属性分组', 128, 1, 'group', '/mall/pms/group/index', NULL, 'AttrgroupManager', 1, 1, 'C', '0', '0', '', 'ep:histogram', 'admin', '2026-05-18 14:59:27', 'admin', '2026-06-23 21:13:54', '');
INSERT INTO `sys_menu` VALUES (139, '规格参数', 128, 2, 'base', '/mall/pms/attr/base/index', NULL, 'BaseParam', 1, 1, 'C', '0', '0', '', 'ep:document', 'admin', '2026-05-18 15:06:50', 'admin', '2026-06-23 21:14:05', '');
INSERT INTO `sys_menu` VALUES (140, '销售属性', 128, 3, 'sale', '/mall/pms/attr/sale/index', NULL, 'SalesAttributes', 1, 1, 'C', '0', '0', '', 'ep:present', 'admin', '2026-05-18 15:12:11', 'admin', '2026-06-23 21:14:16', '');
INSERT INTO `sys_menu` VALUES (141, 'spu管理', 129, 1, 'spu', '/mall/pms/product/spu/index', NULL, 'SpuManager', 1, 1, 'C', '0', '0', '', 'ri:shining-2-line', 'admin', '2026-06-23 16:40:46', 'admin', '2026-06-23 21:23:25', '');
INSERT INTO `sys_menu` VALUES (142, '发布商品', 129, 2, 'publish', '/mall/pms/product/publish/index', NULL, 'PublishSPU', 1, 1, 'C', '0', '0', '', 'heroicons-outline:annotation', 'admin', '2026-06-23 16:43:05', 'admin', '2026-06-23 21:24:37', '');
INSERT INTO `sys_menu` VALUES (143, '商品管理', 129, 3, 'manager', '/mall/pms/product/manager/index', NULL, 'GoodsManager', 1, 1, 'C', '0', '0', 'mall:product:query', 'ep:apple', 'admin', '2026-06-23 16:44:11', 'admin', '2026-06-23 21:25:22', 'SKU 只读检索（对齐谷粒商品管理）');
INSERT INTO `sys_menu` VALUES (144, 'SKU 管理', 129, 4, 'sku', '/mall/pms/sku/index', NULL, 'PmsSkuManager', 1, 1, 'C', '1', '0', 'mall:product:list', 'mdi:barcode', 'admin', '2026-06-24 13:11:09', '', NULL, '已合并至「商品管理」');
INSERT INTO `sys_menu` VALUES (145, '属性分组查询', 138, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:query', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu` VALUES (146, '属性分组新增', 138, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:add', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu` VALUES (147, '属性分组修改', 138, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:edit', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu` VALUES (148, '属性分组删除', 138, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:delete', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu` VALUES (149, '属性分组导出', 138, 5, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:export', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu` VALUES (150, '属性分组导入', 138, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:group:import', '#', 'admin', '2026-05-18 19:46:08', '', NULL, '属性分组导入');
INSERT INTO `sys_menu` VALUES (151, '基本属性查询', 139, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:query', '#', 'admin', '2026-05-18 16:29:55', 'admin', '2026-05-18 16:45:18', '基本属性查询');
INSERT INTO `sys_menu` VALUES (152, '基础属性添加', 139, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:add', '#', 'admin', '2026-05-18 16:45:44', '', NULL, '基础属性添加');
INSERT INTO `sys_menu` VALUES (153, '基础属性删除', 139, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:delete', '#', 'admin', '2026-05-18 16:46:07', '', NULL, '基础属性删除');
INSERT INTO `sys_menu` VALUES (154, '基础属性修改', 139, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:edit', '#', 'admin', '2026-05-18 16:46:27', 'admin', '2026-05-18 16:55:39', '基础属性修改');
INSERT INTO `sys_menu` VALUES (155, '基本规格导入', 139, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:import', '#', 'admin', '2026-05-18 19:47:10', '', NULL, '基本规格导入');
INSERT INTO `sys_menu` VALUES (156, '基本规格导出', 139, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:export', '#', 'admin', '2026-05-18 19:47:35', '', NULL, '基本规格导出');
INSERT INTO `sys_menu` VALUES (157, '基本属性查询', 140, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:query', '#', 'admin', '2026-05-18 16:30:37', 'admin', '2026-05-18 16:46:53', '基本属性查询');
INSERT INTO `sys_menu` VALUES (158, '基本属性添加', 140, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:add', '#', 'admin', '2026-05-18 16:47:23', '', NULL, '基本属性添加');
INSERT INTO `sys_menu` VALUES (159, '基本属性删除', 140, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:delete', '#', 'admin', '2026-05-18 16:47:47', '', NULL, '基本属性删除');
INSERT INTO `sys_menu` VALUES (160, '基本属性修改', 140, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:edit', '#', 'admin', '2026-05-18 16:48:12', 'admin', '2026-05-18 16:55:45', '基本属性修改');
INSERT INTO `sys_menu` VALUES (161, '销售属性导入', 140, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:import', '#', 'admin', '2026-05-18 19:48:01', '', NULL, '销售属性导入');
INSERT INTO `sys_menu` VALUES (162, '销售属性导出', 140, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:export', '#', 'admin', '2026-05-18 19:48:22', '', NULL, '销售属性导出');
INSERT INTO `sys_menu` VALUES (163, '商品查询', 141, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (164, '商品新增', 141, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:add', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (165, '商品修改', 141, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (166, '商品删除', 141, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:delete', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (306, '商品查询', 143, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, 'SKU 只读检索');
INSERT INTO `sys_menu` VALUES (167, '仓库维护', 30, 1, 'warehouse', '/mall/wms/warehouse/index', NULL, 'WareHouse', 1, 1, 'C', '0', '0', '', 'ep:office-building', 'admin', '2026-05-19 15:29:10', 'admin', '2026-06-23 16:45:58', '仓库管理');
INSERT INTO `sys_menu` VALUES (168, '地区管理', 30, 2, 'address', '/mall/wms/address/index', NULL, 'AddressManager', 1, 1, 'C', '0', '0', '', 'ep:position', 'admin', '2026-05-19 15:53:01', 'admin', '2026-05-22 17:39:15', '地区管理');
INSERT INTO `sys_menu` VALUES (169, '商品库存', 30, 3, 'sku', '/mall/wms/sku/index', NULL, '', 1, 0, 'C', '0', '0', 'mall:sku:list', 'mdi:alpha-s-box-outline', 'admin', '2026-05-22 17:38:35', 'admin', '2026-05-22 17:41:05', '商品库存菜单');
INSERT INTO `sys_menu` VALUES (170, '库存工作单', 30, 4, 'task', '/mall/wms/task/index', NULL, 'TaskManager', 1, 1, 'C', '0', '0', 'mall:task:list', 'ri:task-fill', 'admin', '2026-06-23 16:47:13', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (171, '采购单维护', 30, 5, 'PurchaseOrder', '', NULL, '', 1, 1, 'M', '0', '0', '', '#', 'admin', '2026-06-23 16:48:04', 'admin', '2026-06-23 16:50:35', '');
INSERT INTO `sys_menu` VALUES (172, '仓库信息查询', 167, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:query', '#', 'admin', '2026-05-19 17:10:59', 'admin', '2026-05-19 17:13:03', '');
INSERT INTO `sys_menu` VALUES (173, '仓库信息新增', 167, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:add', '#', 'admin', '2026-05-19 17:11:00', 'admin', '2026-05-19 17:13:10', '');
INSERT INTO `sys_menu` VALUES (174, '仓库信息修改', 167, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:edit', '#', 'admin', '2026-05-19 17:11:00', 'admin', '2026-05-19 17:13:16', '');
INSERT INTO `sys_menu` VALUES (175, '仓库信息删除', 167, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:delete', '#', 'admin', '2026-05-19 17:11:00', 'admin', '2026-05-19 17:13:22', '');
INSERT INTO `sys_menu` VALUES (176, '仓库信息导出', 167, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:export', '#', 'admin', '2026-05-19 17:11:00', 'admin', '2026-05-19 17:13:29', '');
INSERT INTO `sys_menu` VALUES (177, 'address查询', 168, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:query', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (178, 'address新增', 168, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:add', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (179, 'address修改', 168, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:edit', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (180, 'address删除', 168, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:delete', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (181, 'address导出', 168, 5, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:export', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (182, '商品库存查询', 169, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:query', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (183, '商品库存新增', 169, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:add', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (184, '商品库存修改', 169, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:edit', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (185, '商品库存删除', 169, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:delete', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (186, '商品库存导出', 169, 5, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:export', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (187, '工作单处理', 170, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:task:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (188, '工作单列表', 170, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:task:list', '#', 'admin', '2026-06-24 13:16:20', '', NULL, '');
INSERT INTO `sys_menu` VALUES (189, '采购需求', 171, 1, 'purchaseitem', '/mall/wms/purchaseitem/index', NULL, '', 1, 1, 'C', '0', '0', 'mall:purchase:item', '#', 'admin', '2026-06-23 16:49:05', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (190, '采购单', 171, 2, 'purchaseorder', '/mall/wms/purchaseorder/index', NULL, '', 1, 1, 'C', '0', '0', 'mall:purchase:list', '#', 'admin', '2026-06-23 16:52:05', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (191, '采购单查询', 190, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:purchase:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (192, '采购单处理', 190, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:purchase:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (193, '优惠券管理', 31, 1, 'coupon', '/mall/sms/coupon/coupon/index', NULL, 'CouponManager', 1, 1, 'C', '0', '0', 'mall:coupon:list', 'mdi:candy', 'admin', '2026-06-23 16:56:59', 'admin', '2026-06-24 15:00:55', '');
INSERT INTO `sys_menu` VALUES (194, '发放记录', 31, 2, 'history', '/mall/sms/coupon/history/index', NULL, 'HistoryManager', 1, 1, 'C', '0', '0', 'mall:coupon:history', 'ri:coupon-2-line', 'admin', '2026-06-23 16:57:58', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (195, '专题活动', 31, 3, 'subject', '/mall/sms/coupon/subject/index', NULL, 'SubjectManager', 1, 1, 'C', '0', '0', 'mall:subject:list', 'ep:aim', 'admin', '2026-06-23 16:59:42', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (196, '秒杀活动', 31, 4, 'seckill', '/mall/sms/coupon/seckill/index', NULL, 'SeckillManager', 1, 1, 'C', '0', '0', 'mall:seckill:list', '#', 'admin', '2026-06-23 17:00:55', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (197, '积分维护', 31, 5, 'bounds', '/mall/sms/coupon/bounds/index', NULL, 'BoundsManager', 1, 1, 'C', '0', '0', 'mall:bounds:list', '#', 'admin', '2026-06-23 17:02:00', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (198, '满减折扣', 31, 6, 'full', '/mall/sms/coupon/full/index', NULL, 'FullManager', 1, 1, 'C', '0', '0', 'mall:reduction:list', '#', 'admin', '2026-06-23 17:02:43', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (199, '会员价格', 31, 7, 'memberprice', '/mall/sms/coupon/memberprice/index', NULL, 'Memberprice', 1, 1, 'C', '0', '0', 'mall:memberprice:list', '#', 'admin', '2026-06-23 17:03:36', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (200, '每日秒杀', 31, 8, 'seckillsession', '/mall/sms/coupon/seckillsession/index', NULL, 'Seckillsession', 1, 1, 'C', '0', '0', 'mall:seckill:session', '#', 'admin', '2026-06-23 17:05:50', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (201, '优惠券新增', 193, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:coupon:add', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (202, '优惠券修改', 193, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:coupon:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (203, '优惠券删除', 193, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:coupon:delete', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (204, '优惠券查询', 193, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:coupon:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (205, '专题查询', 195, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:subject:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (206, '专题新增', 195, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:subject:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (207, '专题修改', 195, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:subject:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (208, '专题删除', 195, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:subject:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (209, '秒杀查询', 196, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (210, '秒杀新增', 196, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (211, '秒杀修改', 196, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (212, '秒杀删除', 196, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (213, '积分新增', 197, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bounds:add', '#', 'admin', '2026-06-24 15:36:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (214, '积分修改', 197, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bounds:edit', '#', 'admin', '2026-06-24 15:36:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (215, '积分删除', 197, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bounds:delete', '#', 'admin', '2026-06-24 15:36:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (216, '满减查询', 198, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:reduction:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (217, '满减新增', 198, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:reduction:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (218, '满减修改', 198, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:reduction:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (219, '满减删除', 198, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:reduction:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (220, '会员价查询', 199, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:memberprice:query', '#', 'admin', '2026-06-24 15:36:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (221, '会员价新增', 199, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:memberprice:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (222, '会员价修改', 199, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:memberprice:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (223, '会员价删除', 199, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:memberprice:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (224, '场次新增', 200, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:session:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (225, '场次修改', 200, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:session:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (226, '场次删除', 200, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:session:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (227, '订单查询', 32, 1, 'order', '/mall/oms/order/order/index', NULL, 'OrderQuery', 1, 1, 'C', '0', '0', 'mall:order:list', '#', 'admin', '2026-06-23 17:08:34', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (228, '退货单处理', 32, 2, 'return', '/mall/oms/order/return/index', NULL, 'OrderReturn', 1, 1, 'C', '0', '0', 'mall:return:list', '#', 'admin', '2026-06-23 17:09:12', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (229, '等级规则', 32, 3, 'level', '/mall/oms/order/level/index', NULL, 'LevelSettings', 1, 1, 'C', '0', '0', 'mall:order:setting', '#', 'admin', '2026-06-23 17:10:11', 'admin', '2026-06-24 15:36:48', '');
INSERT INTO `sys_menu` VALUES (230, '支付流水查询', 32, 4, 'payment', '/mall/oms/order/payment/index', NULL, 'PaymentLog', 1, 1, 'C', '0', '0', 'mall:payment:list', '#', 'admin', '2026-06-23 17:11:01', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (231, '退款流水查询', 32, 5, 'refund', '/mall/oms/order/refund/index', NULL, 'RefundLog', 1, 1, 'C', '0', '0', 'mall:refund:list', '#', 'admin', '2026-06-23 17:11:42', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (232, '订单查询', 227, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:order:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (233, '订单发货', 227, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:order:deliver', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (234, '关闭订单', 227, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:order:close', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (235, '退货审核', 228, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:return:audit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (236, '退货查询', 228, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:return:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (237, '支付流水查询', 230, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:payment:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (238, '退款流水查询', 231, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:refund:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (239, '会员列表', 33, 1, 'member', '/mall/ums/member/member/index', NULL, 'MemberList', 1, 1, 'C', '0', '0', 'mall:member:list', '#', 'admin', '2026-06-23 17:13:52', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (240, '会员等级', 33, 2, 'level', '/mall/ums/member/level/index', NULL, 'MemberLevel', 1, 1, 'C', '0', '0', 'mall:member:level', '#', 'admin', '2026-06-23 17:14:34', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (241, '积分变化', 33, 3, 'growth', '/mall/ums/member/growth/index', NULL, 'GrowthRecord', 1, 1, 'C', '0', '0', 'mall:member:growth', '#', 'admin', '2026-06-23 17:15:27', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (242, '统计信息', 33, 4, 'statistics', '/mall/ums/member/statistics/index', NULL, 'MemberStatistics', 1, 1, 'C', '0', '0', 'mall:member:statistics', '#', 'admin', '2026-06-23 17:16:07', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (243, '会员查询', 239, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (244, '会员修改', 239, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (245, '等级新增', 240, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:level:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (246, '等级修改', 240, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:level:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (247, '等级删除', 240, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:level:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (248, '首页推荐', 34, 1, 'home', '/mall/cms/content/home/index', NULL, 'HomeRecommend', 1, 1, 'C', '0', '0', 'mall:adv:list', '#', 'admin', '2026-06-23 17:18:06', 'admin', '2026-06-24 15:36:48', '');
INSERT INTO `sys_menu` VALUES (249, '分类热门', 34, 2, 'category', '/mall/cms/content/category/index', NULL, 'CategoryHot', 1, 1, 'C', '0', '0', 'mall:categoryHot:list', '#', 'admin', '2026-06-23 17:18:49', 'admin', '2026-06-24 15:36:48', '首页分类热门配置');
INSERT INTO `sys_menu` VALUES (250, '评论管理', 34, 3, 'comments', '/mall/cms/content/comments/index', NULL, 'CommentManage', 1, 1, 'C', '0', '0', 'mall:comment:list', '#', 'admin', '2026-06-23 17:19:35', 'admin', '2026-06-24 15:36:48', '');
INSERT INTO `sys_menu` VALUES (251, '轮播新增', 248, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:adv:add', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (252, '轮播修改', 248, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:adv:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (253, '轮播删除', 248, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:adv:delete', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (254, '评论查询', 250, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:comment:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (255, '评论修改', 250, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:comment:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (256, '评论删除', 250, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:comment:delete', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (257, '模板编辑', 35, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:template:edit', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (258, '模板发布', 35, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:template:publish', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (259, '绑定编辑', 36, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:bind:edit', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (260, '待办查询', 37, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:task:query', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (261, '审批操作', 37, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:task:action', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (262, '提交审批', 37, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:instance:submit', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (263, '发起查询', 38, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:instance:query', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (264, '撤回审批', 38, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:instance:withdraw', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (270, '审批统计', 6, 5, 'statistics', '/approval/statistics/index', NULL, 'ApprovalStatistics', 1, 1, 'C', '0', '0', 'approval:statistics:query', 'mdi:chart-bar', 'admin', '2026-06-30 18:04:12', '', NULL, '审批数据看板');
INSERT INTO `sys_menu` VALUES (271, '登录日志', 33, 5, 'login-log', '/mall/ums/member/login-log/index', NULL, 'MemberLoginLog', 1, 1, 'C', '0', '0', 'mall:member:query', '#', 'admin', '2026-07-01 16:14:36', '', NULL, 'C端会员登录审计');
INSERT INTO `sys_menu` VALUES (272, '会员收藏', 33, 6, 'collect', '/mall/ums/member/collect/index', NULL, 'MemberCollect', 1, 1, 'C', '0', '0', 'mall:member:query', '#', 'admin', '2026-07-01 16:14:36', '', NULL, '商品/专题收藏查询');

-- ----------------------------
-- Table structure for sys_menu_bak
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu_bak`;
CREATE TABLE `sys_menu_bak`  (
  `menu_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int(0) NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '路由名称',
  `is_frame` int(0) NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int(0) NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE,
  INDEX `idx_parent_id`(`parent_id`) USING BTREE,
  INDEX `idx_status_visible`(`status`, `visible`) USING BTREE,
  INDEX `idx_order_num`(`order_num`) USING BTREE,
  INDEX `idx_parent_sort`(`parent_id`, `order_num`) USING BTREE,
  INDEX `idx_visible_status`(`visible`, `status`) USING BTREE,
  INDEX `idx_menu_type_status`(`menu_type`, `status`) USING BTREE,
  INDEX `idx_menu_parent`(`parent_id`) USING BTREE,
  INDEX `idx_menu_visible_status`(`visible`, `status`) USING BTREE,
  INDEX `idx_menu_order`(`order_num`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 346 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu_bak
-- ----------------------------
INSERT INTO `sys_menu_bak` VALUES (1, '系统管理', 0, 1, '/system', '', NULL, 'System', 1, 1, 'M', '0', '0', '', 'ep:setting', 'system', '2025-12-31 16:34:16', 'admin', '2026-06-22 12:35:04', '系统管理模块111');
INSERT INTO `sys_menu_bak` VALUES (2, '系统工具', 0, 2, '/tools', '', NULL, 'SystemTools', 1, 1, 'M', '0', '0', '', 'clarity:tools-line', 'admin', '2026-01-20 13:08:43', 'admin', '2026-04-19 19:09:23', '系统工具');
INSERT INTO `sys_menu_bak` VALUES (3, '系统监控', 0, 3, '/monitor', '', NULL, 'Monitor', 1, 1, 'M', '0', '0', '', 'material-symbols:monitor-outline', 'admin', '2026-01-25 18:00:43', 'admin', '2026-05-18 15:42:45', '系统监控模块');
INSERT INTO `sys_menu_bak` VALUES (4, '商城系统', 0, 5, '/mall', '', NULL, 'MallSystem', 1, 1, 'M', '0', '0', '', 'ep:goods', 'admin', '2026-04-23 20:23:16', 'admin', '2026-04-23 20:26:06', '商城系统');
INSERT INTO `sys_menu_bak` VALUES (5, '星枢项目', 0, 99, 'https://gitee.com/xin1998/StarPivot', '', NULL, '', 0, 1, 'M', '0', '0', '', 'ri:gitee-fill', 'admin', '2026-04-21 12:48:19', 'admin', '2026-05-18 22:44:22', '星枢项目');
INSERT INTO `sys_menu_bak` VALUES (6, 'art-design-pro', 0, 100, 'https://gitee.com/lingchen163/art-design-pro', '', NULL, '', 0, 1, 'M', '0', '0', '', 'ri:guide-fill', 'admin', '2026-04-19 19:07:54', 'admin', '2026-04-23 20:26:22', '');
INSERT INTO `sys_menu_bak` VALUES (7, '菜单管理', 1, 1, 'menu', '/system/menu', NULL, 'SystemMenu', 1, 1, 'C', '0', '0', 'system:menu:list', 'ep:menu', 'system', '2025-12-31 16:34:16', '', '2026-01-02 21:12:33', '菜单管理模块');
INSERT INTO `sys_menu_bak` VALUES (8, '角色管理', 1, 2, 'role', '/system/role', NULL, 'SystemRole', 1, 1, 'C', '0', '0', 'system:role:list', 'oui:app-users-roles', 'system', '2025-12-31 16:34:16', 'admin', '2026-06-08 16:46:55', '角色管理模块');
INSERT INTO `sys_menu_bak` VALUES (9, '用户管理', 1, 3, 'user', '/system/user', NULL, 'SystemUser', 1, 1, 'C', '0', '0', 'system:user:list', 'mdi:user', 'system', '2025-12-31 16:34:16', '', '2026-01-02 21:31:51', '用户管理模块');
INSERT INTO `sys_menu_bak` VALUES (10, '部门管理', 1, 4, 'dept', '/system/dept', NULL, 'SystemDept', 1, 1, 'C', '0', '0', 'system:dept:list', 'ri:organization-chart', '', '2026-01-02 21:04:34', '', '2026-01-02 21:36:43', '部门管理模块');
INSERT INTO `sys_menu_bak` VALUES (11, '岗位管理', 1, 5, 'post', '/system/post/index', NULL, 'PostManage', 1, 1, 'C', '0', '0', 'system:post:list', 'picon:post', 'xinxin', '2026-01-04 19:23:51', 'xinxin', '2026-01-04 19:25:02', '岗位管理模块');
INSERT INTO `sys_menu_bak` VALUES (12, '字典管理', 1, 6, 'dict', '/system/dict/index', NULL, 'DictManage', 1, 1, 'C', '0', '0', 'system:type:list', 'arcticons:zdict', 'admin', '2026-01-05 12:28:54', 'admin', '2026-01-19 21:37:20', '字典管理模块。有：字典数据   字典类型');
INSERT INTO `sys_menu_bak` VALUES (13, '日志管理', 1, 7, 'log', '', NULL, 'LogManager', 1, 1, 'M', '0', '0', '', 'mdi:math-log', 'admin', '2026-01-23 13:37:05', 'admin', '2026-05-15 09:09:47', '');
INSERT INTO `sys_menu_bak` VALUES (14, '通知公告', 1, 8, 'notice', '/system/notice/index', NULL, 'NoticeManage', 1, 0, 'C', '0', '0', 'system:notice:list', 'fe:notice-active', 'admin', '2026-02-05 17:38:35', 'admin', '2026-03-31 22:03:49', '通知公告菜单');
INSERT INTO `sys_menu_bak` VALUES (15, '参数配置', 1, 9, 'config', '/system/config/index', NULL, 'ConfigManage', 1, 1, 'C', '0', '0', 'system:config:list', 'mynaui:config', 'admin', '2026-03-31 22:03:28', 'admin', '2026-03-31 22:05:20', '参数配置菜单');
INSERT INTO `sys_menu_bak` VALUES (16, '操作日志', 13, 1, 'oper', '/system/log/oper/index', NULL, 'OperLog', 1, 1, 'C', '0', '0', 'system:log:list', 'icon-park-solid:log', 'admin', '2026-01-23 13:40:41', '', NULL, '操作日志');
INSERT INTO `sys_menu_bak` VALUES (17, '登录日志', 13, 2, 'login', '/system/log/login/index', NULL, 'LoginInfoLog', 1, 1, 'C', '0', '0', 'system:login:list', 'icon-park-solid:log', 'admin', '2026-01-23 13:51:37', '', NULL, '登录日志');
INSERT INTO `sys_menu_bak` VALUES (18, '代码生成', 2, 1, 'generator', '/tools/generator/index', NULL, 'GenerateTools', 1, 1, 'C', '0', '0', 'tools:generator:list', 'mdi:generator-mobile', 'admin', '2026-01-20 13:15:59', 'admin', '2026-01-20 13:25:42', '代码生成');
INSERT INTO `sys_menu_bak` VALUES (19, '服务器监控', 3, 1, 'server', '/monitor/server/index', NULL, 'ServerMonitor', 1, 0, 'C', '0', '0', 'monitor:server:query', 'ri:server-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-01-25 18:33:45', '服务器信息监控');
INSERT INTO `sys_menu_bak` VALUES (20, 'Druid监控', 3, 2, 'druid', '/monitor/druid/index', NULL, 'DruidMonitor', 1, 0, 'C', '0', '0', 'monitor:druid:query', 'ri:database-2-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-01-25 18:32:03', 'Druid数据库连接池监控');
INSERT INTO `sys_menu_bak` VALUES (21, 'Redis缓存', 3, 3, 'redis', '/monitor/redis/index', NULL, 'RedisMonitor', 1, 0, 'C', '0', '0', 'monitor:redis:query', 'ri:database-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-03-01 13:37:51', 'Redis缓存监控');
INSERT INTO `sys_menu_bak` VALUES (22, '在线用户', 3, 4, 'online', '/monitor/online/index', NULL, 'OnlineUser', 1, 0, 'C', '0', '0', 'monitor:online:query', 'ri:user-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-01-25 18:32:45', '在线用户监控');
INSERT INTO `sys_menu_bak` VALUES (23, '定时任务', 3, 5, 'job', '/monitor/job/index', NULL, 'MonitorJob', 1, 1, 'C', '0', '0', 'monitor:job:query', 'ri:time-line', 'admin', '2026-02-06 19:58:43', 'admin', '2026-02-06 20:34:11', '定时任务调度');
INSERT INTO `sys_menu_bak` VALUES (24, '商品管理', 4, 1, 'pms', '', NULL, 'GoodManager', 1, 1, 'M', '0', '0', '', 'ep:goods-filled', 'admin', '2026-05-15 10:38:29', 'admin', '2026-06-23 19:58:02', '');
INSERT INTO `sys_menu_bak` VALUES (25, '分类维护', 24, 1, 'category', '/mall/pms/category/index', NULL, 'CategoryManager', 1, 1, 'C', '0', '0', 'mall:category:list', 'ri:node-tree', 'admin', '2026-05-15 10:43:03', 'admin', '2026-06-23 21:13:33', '');
INSERT INTO `sys_menu_bak` VALUES (26, '品牌列表', 24, 2, 'brand', '/mall/pms/brand/index', NULL, 'BrandManager', 1, 1, 'C', '0', '0', 'mall:brand:list', 'ep:shop', 'admin', '2026-04-23 20:25:38', 'admin', '2026-06-23 21:13:40', '品牌管理');
INSERT INTO `sys_menu_bak` VALUES (27, '平台属性', 24, 3, 'platform-attributes', '', NULL, 'PlatformAttributes', 1, 1, 'M', '0', '0', '', 'mdi:tools', 'admin', '2026-05-18 14:43:45', 'admin', '2026-05-18 15:04:12', '');
INSERT INTO `sys_menu_bak` VALUES (28, '商品维护', 24, 4, 'maintain', '', NULL, 'GoodsMaintain', 1, 1, 'M', '0', '0', 'mall:product:list', 'ep:goods-filled', 'admin', '2026-04-23 20:27:38', 'admin', '2026-06-24 13:11:09', '商品管理');
INSERT INTO `sys_menu_bak` VALUES (29, '属性分组', 27, 1, 'group', '/mall/pms/group/index', NULL, 'AttrgroupManager', 1, 1, 'C', '0', '0', '', 'ep:histogram', 'admin', '2026-05-18 14:59:27', 'admin', '2026-06-23 21:13:54', '');
INSERT INTO `sys_menu_bak` VALUES (30, '规格参数', 27, 2, 'base', '/mall/pms/attr/base/index', NULL, 'BaseParam', 1, 1, 'C', '0', '0', '', 'ep:document', 'admin', '2026-05-18 15:06:50', 'admin', '2026-06-23 21:14:05', '');
INSERT INTO `sys_menu_bak` VALUES (31, '销售属性', 27, 3, 'sale', '/mall/pms/attr/sale/index', NULL, 'SalesAttributes', 1, 1, 'C', '0', '0', '', 'ep:present', 'admin', '2026-05-18 15:12:11', 'admin', '2026-06-23 21:14:16', '');
INSERT INTO `sys_menu_bak` VALUES (32, '菜单查询', 7, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2026-01-12 17:39:26', '', NULL, '菜单查询');
INSERT INTO `sys_menu_bak` VALUES (33, '菜单添加', 7, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2026-01-12 17:39:50', '', NULL, '菜单添加');
INSERT INTO `sys_menu_bak` VALUES (34, '菜单修改', 7, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2026-01-12 17:40:05', '', NULL, '菜单修改');
INSERT INTO `sys_menu_bak` VALUES (35, '菜单删除', 7, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:menu:delete', '#', 'admin', '2026-01-12 17:40:27', '', NULL, '菜单删除');
INSERT INTO `sys_menu_bak` VALUES (36, '新增角色', 8, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:add', '#', 'admin', '2026-01-12 17:32:13', '', NULL, '新增角色按钮');
INSERT INTO `sys_menu_bak` VALUES (37, '修改角色', 8, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2026-01-12 17:32:45', '', NULL, '修改角色按钮');
INSERT INTO `sys_menu_bak` VALUES (38, '删除角色', 8, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:delete', '#', 'admin', '2026-01-12 17:33:14', '', NULL, '删除角色按钮');
INSERT INTO `sys_menu_bak` VALUES (39, '角色查询', 8, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:query', '#', 'admin', '2026-01-12 17:34:57', '', NULL, '角色查询 按钮');
INSERT INTO `sys_menu_bak` VALUES (40, '分配数据权限', 8, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:assignDataScope', '#', 'admin', '2026-01-16 19:04:22', 'admin', '2026-01-23 20:57:15', '分配角色');
INSERT INTO `sys_menu_bak` VALUES (41, '已分配用户', 8, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:allocatedList', '#', 'admin', '2026-01-23 20:55:10', 'admin', '2026-01-23 22:41:41', '已分配用户');
INSERT INTO `sys_menu_bak` VALUES (42, '未分配用户', 8, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:unallocatedList', '#', 'admin', '2026-01-23 22:41:08', '', NULL, '未分配用户');
INSERT INTO `sys_menu_bak` VALUES (43, '分配用户', 8, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:assignUser', '#', 'admin', '2026-01-23 23:18:49', '', NULL, '分配用户：添加用户角色关联表');
INSERT INTO `sys_menu_bak` VALUES (44, '取消授权', 8, 9, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:cancelUser', '#', 'admin', '2026-01-23 23:26:23', '', NULL, '取消授权');
INSERT INTO `sys_menu_bak` VALUES (45, '新增用户', 9, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:add', '#', 'admin', '2026-01-12 16:42:30', 'admin', '2026-01-16 20:42:35', '新增用户按钮');
INSERT INTO `sys_menu_bak` VALUES (46, '修改状态', 9, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:changeStatus', '#', 'admin', '2026-01-16 18:11:22', 'admin', '2026-01-16 20:42:31', '修改状态');
INSERT INTO `sys_menu_bak` VALUES (47, '修改用户', 9, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2026-01-12 17:30:37', 'admin', '2026-01-16 20:42:26', '修改用户  按钮');
INSERT INTO `sys_menu_bak` VALUES (48, '删除用户', 9, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:delete', '#', 'admin', '2026-01-12 17:31:11', 'admin', '2026-01-16 20:42:39', '删除用户按钮');
INSERT INTO `sys_menu_bak` VALUES (49, '用户查询', 9, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:query', '#', 'admin', '2026-01-12 17:35:27', 'admin', '2026-01-16 20:42:43', '用户查询按钮');
INSERT INTO `sys_menu_bak` VALUES (50, '修改密码', 9, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2026-01-16 18:10:55', 'admin', '2026-01-16 20:42:48', '修改密码');
INSERT INTO `sys_menu_bak` VALUES (51, '解除锁定', 9, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:unLock', '#', 'admin', '2026-01-23 18:49:56', '', NULL, '解除锁定');
INSERT INTO `sys_menu_bak` VALUES (52, '用户导入', 9, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:import', '#', 'admin', '2026-01-24 21:03:18', '', NULL, '用户导入');
INSERT INTO `sys_menu_bak` VALUES (53, '用户导出', 9, 9, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:export', '#', 'admin', '2026-01-24 21:03:52', '', NULL, '用户导出');
INSERT INTO `sys_menu_bak` VALUES (54, '部门查询', 10, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2026-01-12 17:35:58', '', NULL, '部门查询按钮');
INSERT INTO `sys_menu_bak` VALUES (55, '部门新增', 10, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2026-01-12 17:36:28', '', NULL, '部门新增');
INSERT INTO `sys_menu_bak` VALUES (56, '部门修改', 10, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2026-01-12 17:36:49', '', NULL, '部门修改');
INSERT INTO `sys_menu_bak` VALUES (57, '部门删除', 10, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:dept:delete', '#', 'admin', '2026-01-12 17:37:21', '', NULL, '部门删除');
INSERT INTO `sys_menu_bak` VALUES (58, '岗位查询', 11, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:post:query', '#', 'admin', '2026-01-12 17:37:47', '', NULL, '岗位查询');
INSERT INTO `sys_menu_bak` VALUES (59, '岗位新增', 11, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:post:add', '#', 'admin', '2026-01-12 17:38:09', '', NULL, '岗位新增');
INSERT INTO `sys_menu_bak` VALUES (60, '岗位修改', 11, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2026-01-12 17:38:46', '', NULL, '岗位修改');
INSERT INTO `sys_menu_bak` VALUES (61, '岗位删除', 11, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:post:delete', '#', 'admin', '2026-01-12 17:39:04', '', NULL, '岗位删除');
INSERT INTO `sys_menu_bak` VALUES (62, '字典类型添加', 12, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:type:add', '#', 'admin', '2026-01-16 19:08:40', 'admin', '2026-01-16 19:33:35', '字典类型添加');
INSERT INTO `sys_menu_bak` VALUES (63, '字典类型修改', 12, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:type:edit', '#', 'admin', '2026-01-16 19:09:04', 'admin', '2026-01-16 19:33:43', '字典类型修改');
INSERT INTO `sys_menu_bak` VALUES (64, '字典类型删除', 12, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:type:delete', '#', 'admin', '2026-01-16 19:09:27', 'admin', '2026-01-16 19:33:48', '字典类型删除');
INSERT INTO `sys_menu_bak` VALUES (65, '字典类型查询', 12, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:type:query', '#', 'admin', '2026-01-19 21:33:21', '', NULL, '字典类型查询');
INSERT INTO `sys_menu_bak` VALUES (66, '字典数据添加', 12, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:data:add', '#', 'admin', '2026-01-16 19:31:42', '', NULL, '字典数据添加');
INSERT INTO `sys_menu_bak` VALUES (67, '字典数据修改', 12, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:data:edit', '#', 'admin', '2026-01-16 19:32:19', '', NULL, '字典数据修改');
INSERT INTO `sys_menu_bak` VALUES (68, '字典数据删除', 12, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:data:delete', '#', 'admin', '2026-01-16 19:32:51', '', NULL, '字典数据删除');
INSERT INTO `sys_menu_bak` VALUES (69, '字典数据查询', 12, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:data:query', '#', 'admin', '2026-01-19 21:33:59', '', NULL, '字典数据查询');
INSERT INTO `sys_menu_bak` VALUES (70, '日志查询', 16, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:operlog:query', '#', 'admin', '2026-01-23 13:58:02', '', NULL, '日志查询');
INSERT INTO `sys_menu_bak` VALUES (71, '清空日志', 16, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:operlog:delete', '#', 'admin', '2026-01-23 13:57:43', '', NULL, '清空日志');
INSERT INTO `sys_menu_bak` VALUES (72, '日志查询', 17, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:logininfor:query', '#', 'admin', '2026-01-23 14:24:26', '', NULL, '日志查询');
INSERT INTO `sys_menu_bak` VALUES (73, '日志删除', 17, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:logininfor:delete', '#', 'admin', '2026-01-23 14:24:41', '', NULL, '日志删除');
INSERT INTO `sys_menu_bak` VALUES (74, '通知公告查询', 14, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (75, '通知公告新增', 14, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (76, '通知公告修改', 14, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (77, '通知公告删除', 14, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:delete', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (78, '通知公告导出', 14, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:export', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (79, '参数配置查询', 15, 1, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:query', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:23', '');
INSERT INTO `sys_menu_bak` VALUES (80, '参数配置新增', 15, 2, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:add', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:27', '');
INSERT INTO `sys_menu_bak` VALUES (81, '参数配置修改', 15, 3, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:31', '');
INSERT INTO `sys_menu_bak` VALUES (82, '参数配置删除', 15, 4, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:delete', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:36', '');
INSERT INTO `sys_menu_bak` VALUES (83, '参数配置导出', 15, 5, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:export', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:39', '');
INSERT INTO `sys_menu_bak` VALUES (84, '添加', 18, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:add', '#', 'admin', '2026-01-23 15:56:17', 'admin', '2026-01-23 19:01:31', '添加');
INSERT INTO `sys_menu_bak` VALUES (85, '列表查询', 18, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2026-01-20 14:56:43', '', NULL, '列表查询');
INSERT INTO `sys_menu_bak` VALUES (86, '预览', 18, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2026-01-23 15:53:25', 'admin', '2026-01-23 19:01:40', '预览');
INSERT INTO `sys_menu_bak` VALUES (87, '编辑', 18, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2026-01-23 15:53:47', 'admin', '2026-01-23 19:01:44', '');
INSERT INTO `sys_menu_bak` VALUES (88, '删除', 18, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:delete', '#', 'admin', '2026-01-23 15:54:05', 'admin', '2026-01-23 19:01:48', '');
INSERT INTO `sys_menu_bak` VALUES (89, '同步数据库', 18, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:sync', '#', 'admin', '2026-01-23 15:55:18', 'admin', '2026-01-23 19:01:52', '同步数据库');
INSERT INTO `sys_menu_bak` VALUES (90, '生成代码', 18, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:create', '#', 'admin', '2026-01-23 15:55:59', 'admin', '2026-01-23 19:01:56', '生成代码');
INSERT INTO `sys_menu_bak` VALUES (91, '导入', 18, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:import', '#', 'xinxin', '2026-01-24 00:08:41', '', NULL, '导入');
INSERT INTO `sys_menu_bak` VALUES (92, '强制下线', 22, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:online:force-logout', '', 'admin', '2026-01-25 18:00:43', 'admin', '2026-01-25 18:30:55', '强制用户下线');
INSERT INTO `sys_menu_bak` VALUES (93, '删除缓存', 21, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:cache:remove', '#', 'admin', '2026-01-25 22:19:38', '', NULL, 'monitor:cache:remove');
INSERT INTO `sys_menu_bak` VALUES (94, '清空缓存', 21, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:cache:clear', '#', 'admin', '2026-01-25 22:20:00', '', NULL, '清空缓存');
INSERT INTO `sys_menu_bak` VALUES (95, '缓存列表', 21, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:cache:query', '#', 'admin', '2026-01-27 18:21:55', '', NULL, '缓存列表');
INSERT INTO `sys_menu_bak` VALUES (96, '任务查询', 23, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2026-02-06 20:01:06', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (97, '任务新增', 23, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2026-02-06 20:01:06', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (98, '任务修改', 23, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2026-02-06 20:01:06', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (99, '任务删除', 23, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:job:delete', '#', 'admin', '2026-02-06 20:01:06', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (100, '属性分组查询', 29, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:query', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (101, '属性分组新增', 29, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:add', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (102, '属性分组修改', 29, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:edit', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (103, '属性分组删除', 29, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:delete', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (104, '属性分组导出', 29, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:export', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (105, '属性分组导入', 29, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:group:import', '#', 'admin', '2026-05-18 19:46:08', '', NULL, '属性分组导入');
INSERT INTO `sys_menu_bak` VALUES (106, '基本属性查询', 30, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:query', '#', 'admin', '2026-05-18 16:29:55', 'admin', '2026-05-18 16:45:18', '基本属性查询');
INSERT INTO `sys_menu_bak` VALUES (107, '基础属性添加', 30, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:add', '#', 'admin', '2026-05-18 16:45:44', '', NULL, '基础属性添加');
INSERT INTO `sys_menu_bak` VALUES (108, '基础属性删除', 30, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:delete', '#', 'admin', '2026-05-18 16:46:07', '', NULL, '基础属性删除');
INSERT INTO `sys_menu_bak` VALUES (109, '基础属性修改', 30, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:edit', '#', 'admin', '2026-05-18 16:46:27', 'admin', '2026-05-18 16:55:39', '基础属性修改');
INSERT INTO `sys_menu_bak` VALUES (110, '基本规格导入', 30, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:import', '#', 'admin', '2026-05-18 19:47:10', '', NULL, '基本规格导入');
INSERT INTO `sys_menu_bak` VALUES (111, '基本规格导出', 30, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:export', '#', 'admin', '2026-05-18 19:47:35', '', NULL, '基本规格导出');
INSERT INTO `sys_menu_bak` VALUES (112, '基本属性查询', 31, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:query', '#', 'admin', '2026-05-18 16:30:37', 'admin', '2026-05-18 16:46:53', '基本属性查询');
INSERT INTO `sys_menu_bak` VALUES (113, '基本属性添加', 31, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:add', '#', 'admin', '2026-05-18 16:47:23', '', NULL, '基本属性添加');
INSERT INTO `sys_menu_bak` VALUES (114, '基本属性删除', 31, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:delete', '#', 'admin', '2026-05-18 16:47:47', '', NULL, '基本属性删除');
INSERT INTO `sys_menu_bak` VALUES (115, '基本属性修改', 31, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:edit', '#', 'admin', '2026-05-18 16:48:12', 'admin', '2026-05-18 16:55:45', '基本属性修改');
INSERT INTO `sys_menu_bak` VALUES (116, '销售属性导入', 31, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:import', '#', 'admin', '2026-05-18 19:48:01', '', NULL, '销售属性导入');
INSERT INTO `sys_menu_bak` VALUES (117, '销售属性导出', 31, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:export', '#', 'admin', '2026-05-18 19:48:22', '', NULL, '销售属性导出');
INSERT INTO `sys_menu_bak` VALUES (118, '品牌列表查询', 26, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:brand:query', '#', 'admin', '2026-05-18 22:46:48', '', NULL, '品牌列表查询');
INSERT INTO `sys_menu_bak` VALUES (119, '品牌添加', 26, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:brand:add', '#', 'admin', '2026-05-18 22:47:16', '', NULL, '品牌添加');
INSERT INTO `sys_menu_bak` VALUES (120, '品牌修改', 26, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:brand:edit', '#', 'admin', '2026-05-18 22:47:44', '', NULL, '品牌修改');
INSERT INTO `sys_menu_bak` VALUES (121, '品牌删除', 26, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:brand:delete', '#', 'admin', '2026-05-18 22:48:06', '', NULL, '品牌删除');
INSERT INTO `sys_menu_bak` VALUES (122, '分类维护查询', 25, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:category:query', '#', 'admin', '2026-05-18 22:50:11', '', NULL, '分类维护查询');
INSERT INTO `sys_menu_bak` VALUES (123, '分类维护添加', 25, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:category:add', '#', 'admin', '2026-05-18 22:50:41', '', NULL, '分类维护添加');
INSERT INTO `sys_menu_bak` VALUES (124, '分类维护删除', 25, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:category:delete', '#', 'admin', '2026-05-18 22:51:06', '', NULL, '分类维护删除');
INSERT INTO `sys_menu_bak` VALUES (125, '分类维护修改', 25, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:category:edit', '#', 'admin', '2026-05-18 22:51:30', '', NULL, '分类维护修改');
INSERT INTO `sys_menu_bak` VALUES (131, '仓库系统', 4, 1, 'wms', '', NULL, 'WmsManager', 1, 1, 'M', '0', '0', '', 'mdi:warehouse', 'admin', '2026-05-19 15:26:07', 'admin', '2026-06-23 16:45:46', '仓储服务');
INSERT INTO `sys_menu_bak` VALUES (132, '仓库维护', 131, 1, 'warehouse', '/mall/wms/warehouse/index', NULL, 'WareHouse', 1, 1, 'C', '0', '0', '', 'ep:office-building', 'admin', '2026-05-19 15:29:10', 'admin', '2026-06-23 16:45:58', '仓库管理');
INSERT INTO `sys_menu_bak` VALUES (133, '地区管理', 131, 2, 'address', '/mall/wms/address/index', NULL, 'AddressManager', 1, 1, 'C', '0', '0', '', 'ep:position', 'admin', '2026-05-19 15:53:01', 'admin', '2026-05-22 17:39:15', '地区管理');
INSERT INTO `sys_menu_bak` VALUES (134, '仓库信息查询', 132, 1, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:query', '#', 'admin', '2026-05-19 17:10:59', 'admin', '2026-05-19 17:13:03', '');
INSERT INTO `sys_menu_bak` VALUES (135, '仓库信息新增', 132, 2, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:add', '#', 'admin', '2026-05-19 17:11:00', 'admin', '2026-05-19 17:13:10', '');
INSERT INTO `sys_menu_bak` VALUES (136, '仓库信息修改', 132, 3, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:edit', '#', 'admin', '2026-05-19 17:11:00', 'admin', '2026-05-19 17:13:16', '');
INSERT INTO `sys_menu_bak` VALUES (137, '仓库信息删除', 132, 4, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:delete', '#', 'admin', '2026-05-19 17:11:00', 'admin', '2026-05-19 17:13:22', '');
INSERT INTO `sys_menu_bak` VALUES (138, '仓库信息导出', 132, 5, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:export', '#', 'admin', '2026-05-19 17:11:00', 'admin', '2026-05-19 17:13:29', '');
INSERT INTO `sys_menu_bak` VALUES (139, 'address查询', 133, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:query', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (140, 'address新增', 133, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:add', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (141, 'address修改', 133, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:edit', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (142, 'address删除', 133, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:delete', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (143, 'address导出', 133, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:export', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (144, '商品库存', 131, 3, 'sku', '/mall/wms/sku/index', NULL, '', 1, 0, 'C', '0', '0', 'mall:sku:list', 'mdi:alpha-s-box-outline', 'admin', '2026-05-22 17:38:35', 'admin', '2026-05-22 17:41:05', '商品库存菜单');
INSERT INTO `sys_menu_bak` VALUES (145, '商品库存查询', 144, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:query', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (146, '商品库存新增', 144, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:add', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (147, '商品库存修改', 144, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:edit', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (148, '商品库存删除', 144, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:delete', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (149, '商品库存导出', 144, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:export', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (150, '外部库代码生成', 2, 2, 'external', '/tools/generator-external/index', NULL, '', 1, 1, 'C', '0', '0', '', 'ep:document-add', 'admin', '2026-06-02 18:13:11', 'admin', '2026-06-02 18:30:43', '');
INSERT INTO `sys_menu_bak` VALUES (151, '查询', 150, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:external:query', '#', 'admin', '2026-06-02 19:14:07', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (152, '预览', 150, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:external:preview', '#', 'admin', '2026-06-02 19:14:43', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (153, '生成', 150, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:external:create', '#', 'admin', '2026-06-02 19:15:16', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (169, '文件中心', 0, 4, '/file', '', NULL, 'FileCenter', 1, 1, 'M', '0', '0', '', 'ep:folder-opened', 'admin', '2026-06-22 19:45:35', '', NULL, '文件中心模块');
INSERT INTO `sys_menu_bak` VALUES (170, '文件管理', 169, 1, 'index', '/file/index', NULL, 'FileManage', 1, 1, 'C', '0', '0', 'file:resource:list', 'ep:document', 'admin', '2026-06-22 19:45:35', '', NULL, '文件管理页面');
INSERT INTO `sys_menu_bak` VALUES (171, '文件查询', 170, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:query', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '文件列表/详情/预览');
INSERT INTO `sys_menu_bak` VALUES (172, '文件上传', 170, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:add', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '上传文件');
INSERT INTO `sys_menu_bak` VALUES (173, '文件删除', 170, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:delete', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '逻辑删除');
INSERT INTO `sys_menu_bak` VALUES (174, '文件恢复', 170, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:restore', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '回收站恢复');
INSERT INTO `sys_menu_bak` VALUES (175, '文件夹查询', 170, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:query', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '文件夹树');
INSERT INTO `sys_menu_bak` VALUES (176, '文件夹新增', 170, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:add', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '新建文件夹');
INSERT INTO `sys_menu_bak` VALUES (177, '文件夹编辑', 170, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:edit', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '重命名/排序');
INSERT INTO `sys_menu_bak` VALUES (178, '文件夹删除', 170, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:delete', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '删除文件夹');
INSERT INTO `sys_menu_bak` VALUES (179, '文件迁移', 170, 9, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:move', '#', 'admin', '2026-06-22 20:54:50', '', NULL, '迁移到其他文件夹');
INSERT INTO `sys_menu_bak` VALUES (180, 'spu管理', 28, 1, 'spu', '/mall/pms/product/spu/index', NULL, 'SpuManager', 1, 1, 'C', '0', '0', '', 'ri:shining-2-line', 'admin', '2026-06-23 16:40:46', 'admin', '2026-06-23 21:23:25', '');
INSERT INTO `sys_menu_bak` VALUES (181, '发布商品', 28, 2, 'publish', '/mall/pms/product/publish/index', NULL, 'PublishSPU', 1, 1, 'C', '0', '0', '', 'heroicons-outline:annotation', 'admin', '2026-06-23 16:43:05', 'admin', '2026-06-23 21:24:37', '');
INSERT INTO `sys_menu_bak` VALUES (182, '商品管理', 28, 3, 'manager', '/mall/pms/product/manager/index', NULL, 'GoodsManager', 1, 1, 'C', '0', '0', '', 'ep:apple', 'admin', '2026-06-23 16:44:11', 'admin', '2026-06-23 21:25:22', '');
INSERT INTO `sys_menu_bak` VALUES (183, '库存工作单', 131, 4, 'task', '/mall/wms/task/index', NULL, 'TaskManager', 1, 1, 'C', '0', '0', 'mall:task:list', 'ri:task-fill', 'admin', '2026-06-23 16:47:13', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (184, '采购单维护', 131, 5, 'PurchaseOrder', '', NULL, '', 1, 1, 'M', '0', '0', '', '#', 'admin', '2026-06-23 16:48:04', 'admin', '2026-06-23 16:50:35', '');
INSERT INTO `sys_menu_bak` VALUES (185, '采购需求', 184, 1, 'purchaseitem', '/mall/wms/purchaseitem/index', NULL, '', 1, 1, 'C', '0', '0', 'mall:purchase:item', '#', 'admin', '2026-06-23 16:49:05', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (186, '采购单', 184, 2, 'purchaseorder', '/mall/wms/purchaseorder/index', NULL, '', 1, 1, 'C', '0', '0', 'mall:purchase:list', '#', 'admin', '2026-06-23 16:52:05', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (187, '优惠营销', 4, 1, 'sms', '', NULL, 'PromotionMarketing', 1, 1, 'M', '0', '0', '', 'ep:promotion', 'admin', '2026-06-23 16:55:30', 'admin', '2026-06-23 19:58:27', '');
INSERT INTO `sys_menu_bak` VALUES (188, '优惠券管理', 187, 1, 'coupon', '/mall/sms/coupon/coupon/index', NULL, 'CouponManager', 1, 1, 'C', '0', '0', 'mall:coupon:list', 'mdi:candy', 'admin', '2026-06-23 16:56:59', 'admin', '2026-06-24 15:00:55', '');
INSERT INTO `sys_menu_bak` VALUES (189, '发放记录', 187, 2, 'history', '/mall/sms/coupon/history/index', NULL, 'HistoryManager', 1, 1, 'C', '0', '0', 'mall:coupon:history', 'ri:coupon-2-line', 'admin', '2026-06-23 16:57:58', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (190, '专题活动', 187, 3, 'subject', '/mall/sms/coupon/subject/index', NULL, 'SubjectManager', 1, 1, 'C', '0', '0', 'mall:subject:list', 'ep:aim', 'admin', '2026-06-23 16:59:42', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (191, '秒杀活动', 187, 4, 'seckill', '/mall/sms/coupon/seckill/index', NULL, 'SeckillManager', 1, 1, 'C', '0', '0', 'mall:seckill:list', '#', 'admin', '2026-06-23 17:00:55', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (192, '积分维护', 187, 5, 'bounds', '/mall/sms/coupon/bounds/index', NULL, 'BoundsManager', 1, 1, 'C', '0', '0', 'mall:bounds:list', '#', 'admin', '2026-06-23 17:02:00', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (193, '满减折扣', 187, 6, 'full', '/mall/sms/coupon/full/index', NULL, 'FullManager', 1, 1, 'C', '0', '0', 'mall:reduction:list', '#', 'admin', '2026-06-23 17:02:43', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (194, '会员价格', 187, 7, 'memberprice', '/mall/sms/coupon/memberprice/index', NULL, 'Memberprice', 1, 1, 'C', '0', '0', 'mall:memberprice:list', '#', 'admin', '2026-06-23 17:03:36', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (195, '每日秒杀', 187, 8, 'seckillsession', '/mall/sms/coupon/seckillsession/index', NULL, 'Seckillsession', 1, 1, 'C', '0', '0', 'mall:seckill:session', '#', 'admin', '2026-06-23 17:05:50', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (196, '订单系统', 4, 4, 'oms', '', NULL, 'OrderManager', 1, 1, 'M', '0', '0', '', 'mdi:order-bool-descending-variant', 'admin', '2026-06-23 17:07:36', 'admin', '2026-06-23 19:59:00', '');
INSERT INTO `sys_menu_bak` VALUES (197, '订单查询', 196, 1, 'order', '/mall/oms/order/order/index', NULL, 'OrderQuery', 1, 1, 'C', '0', '0', 'mall:order:list', '#', 'admin', '2026-06-23 17:08:34', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (198, '退货单处理', 196, 2, 'return', '/mall/oms/order/return/index', NULL, 'OrderReturn', 1, 1, 'C', '0', '0', 'mall:return:list', '#', 'admin', '2026-06-23 17:09:12', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (199, '等级规则', 196, 3, 'level', '/mall/oms/order/level/index', NULL, 'LevelSettings', 1, 1, 'C', '0', '0', 'mall:order:setting', '#', 'admin', '2026-06-23 17:10:11', 'admin', '2026-06-24 15:36:48', '');
INSERT INTO `sys_menu_bak` VALUES (200, '支付流水查询', 196, 4, 'payment', '/mall/oms/order/payment/index', NULL, 'PaymentLog', 1, 1, 'C', '0', '0', 'mall:payment:list', '#', 'admin', '2026-06-23 17:11:01', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (201, '退款流水查询', 196, 5, 'refund', '/mall/oms/order/refund/index', NULL, 'RefundLog', 1, 1, 'C', '0', '0', 'mall:refund:list', '#', 'admin', '2026-06-23 17:11:42', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (202, '用户系统', 4, 5, 'ums', '', NULL, 'MemberSystem', 1, 1, 'M', '0', '0', '', 'mdi:wallet-membership', 'admin', '2026-06-23 17:12:59', 'admin', '2026-06-23 19:59:22', '');
INSERT INTO `sys_menu_bak` VALUES (203, '会员列表', 202, 1, 'member', '/mall/ums/member/member/index', NULL, 'MemberList', 1, 1, 'C', '0', '0', 'mall:member:list', '#', 'admin', '2026-06-23 17:13:52', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (204, '会员等级', 202, 2, 'level', '/mall/ums/member/level/index', NULL, 'MemberLevel', 1, 1, 'C', '0', '0', 'mall:member:level', '#', 'admin', '2026-06-23 17:14:34', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (205, '积分变化', 202, 3, 'growth', '/mall/ums/member/growth/index', NULL, 'GrowthRecord', 1, 1, 'C', '0', '0', 'mall:member:growth', '#', 'admin', '2026-06-23 17:15:27', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (206, '统计信息', 202, 4, 'statistics', '/mall/ums/member/statistics/index', NULL, 'MemberStatistics', 1, 1, 'C', '0', '0', 'mall:member:statistics', '#', 'admin', '2026-06-23 17:16:07', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu_bak` VALUES (207, '内容管理', 4, 6, 'cms', '', NULL, 'ContentManage', 1, 1, 'M', '0', '0', '', 'mdi:content-paste', 'admin', '2026-06-23 17:17:02', 'admin', '2026-06-23 19:59:30', '');
INSERT INTO `sys_menu_bak` VALUES (208, '首页推荐', 207, 1, 'home', '/mall/cms/content/home/index', NULL, 'HomeRecommend', 1, 1, 'C', '0', '0', 'mall:adv:list', '#', 'admin', '2026-06-23 17:18:06', 'admin', '2026-06-24 15:36:48', ' — （路由：content/index）');
INSERT INTO `sys_menu_bak` VALUES (209, '分类热门', 207, 2, 'category', '/mall/cms/content/category/index', NULL, 'CategoryHot', 1, 1, 'C', '0', '0', '', '#', 'admin', '2026-06-23 17:18:49', 'admin', '2026-06-24 15:36:48', '无独立表，引导至专题活动 sms_home_subject');
INSERT INTO `sys_menu_bak` VALUES (210, '评论管理', 207, 3, 'comments', '/mall/cms/content/comments/index', NULL, 'CommentManage', 1, 1, 'C', '0', '0', 'mall:comment:list', '#', 'admin', '2026-06-23 17:19:35', 'admin', '2026-06-24 15:36:48', '');
INSERT INTO `sys_menu_bak` VALUES (280, 'C端商城', 4, 0, '/portal', '', NULL, 'MallPortal', 1, 1, 'C', '0', '0', '', 'ep:shopping-bag', 'admin', '2026-06-24 13:11:09', '', NULL, '打开 C 端商城前台');
INSERT INTO `sys_menu_bak` VALUES (281, 'SKU 管理', 28, 4, 'sku', '/mall/pms/sku/index', NULL, 'PmsSkuManager', 1, 1, 'C', '0', '0', 'mall:product:list', 'mdi:barcode', 'admin', '2026-06-24 13:11:09', '', NULL, 'PMS SKU 独立管理');
INSERT INTO `sys_menu_bak` VALUES (282, '订单查询', 197, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:order:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (283, '订单发货', 197, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:order:deliver', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (284, '关闭订单', 197, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:order:close', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (285, '退货审核', 198, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:return:audit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (286, '退货查询', 198, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:return:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (287, '优惠券新增', 188, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:coupon:add', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (288, '优惠券修改', 188, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:coupon:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (289, '优惠券删除', 188, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:coupon:delete', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (290, '会员查询', 203, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (291, '会员修改', 203, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (292, '采购单查询', 186, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:purchase:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (293, '工作单处理', 183, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:task:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (294, '商品查询', 180, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (295, '商品新增', 180, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:add', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (296, '商品修改', 180, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (297, '商品删除', 180, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:delete', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (298, '轮播新增', 208, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:adv:add', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (299, '轮播修改', 208, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:adv:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (300, '轮播删除', 208, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:adv:delete', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (301, '评论查询', 210, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:comment:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (302, '评论修改', 210, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:comment:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (303, '评论删除', 210, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:comment:delete', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (304, '工作单列表', 183, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:task:list', '#', 'admin', '2026-06-24 13:16:20', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (305, '积分新增', 192, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bounds:add', '#', 'admin', '2026-06-24 15:36:48', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (306, '积分修改', 192, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bounds:edit', '#', 'admin', '2026-06-24 15:36:48', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (307, '积分删除', 192, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bounds:delete', '#', 'admin', '2026-06-24 15:36:48', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (308, '会员价查询', 194, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:memberprice:query', '#', 'admin', '2026-06-24 15:36:48', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (309, '会员价新增', 194, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:memberprice:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (310, '会员价修改', 194, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:memberprice:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (311, '会员价删除', 194, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:memberprice:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (312, '满减查询', 193, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:reduction:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (313, '满减新增', 193, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:reduction:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (314, '满减修改', 193, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:reduction:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (315, '满减删除', 193, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:reduction:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (316, '秒杀查询', 191, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (317, '秒杀新增', 191, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (318, '秒杀修改', 191, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (319, '秒杀删除', 191, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (320, '场次新增', 195, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:session:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (321, '场次修改', 195, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:session:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (322, '场次删除', 195, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:session:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (323, '专题查询', 190, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:subject:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (324, '专题新增', 190, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:subject:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (325, '专题修改', 190, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:subject:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (326, '专题删除', 190, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:subject:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (327, '支付流水查询', 200, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:payment:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (328, '退款流水查询', 201, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:refund:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (329, '等级新增', 204, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:level:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (330, '等级修改', 204, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:level:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (331, '等级删除', 204, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:level:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (332, '采购单处理', 186, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:purchase:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (333, '优惠券查询', 188, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:coupon:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (334, '审批中心', 0, 5, '/approval', '', NULL, 'ApprovalCenter', 1, 1, 'M', '0', '0', '', 'mdi:clipboard-check-outline', 'admin', '2026-06-26 10:49:49', '', NULL, 'SAS 审批中台');
INSERT INTO `sys_menu_bak` VALUES (335, '审批模板', 334, 1, 'template', '/approval/template/index', NULL, 'ApprovalTemplate', 1, 1, 'C', '0', '0', 'approval:template:query', 'ep:document', 'admin', '2026-06-26 10:49:49', '', NULL, '审批模板配置');
INSERT INTO `sys_menu_bak` VALUES (336, '业务绑定', 334, 2, 'bind', '/approval/bind/index', NULL, 'ApprovalBind', 1, 1, 'C', '0', '0', 'approval:bind:edit', 'ep:link', 'admin', '2026-06-26 10:49:49', '', NULL, '业务类型绑定规则');
INSERT INTO `sys_menu_bak` VALUES (337, '待办审批', 334, 3, 'todo', '/approval/todo/index', NULL, 'ApprovalTodo', 1, 1, 'C', '0', '0', 'approval:task:query', 'ri:bell-line', 'admin', '2026-06-26 10:49:49', '', NULL, '待办与已办');
INSERT INTO `sys_menu_bak` VALUES (338, '我发起的', 334, 4, 'mine', '/approval/mine/index', NULL, 'ApprovalMine', 1, 1, 'C', '0', '0', 'approval:instance:query', 'mdi:user-arrow-right', 'admin', '2026-06-26 10:49:49', '', NULL, '我发起的审批');
INSERT INTO `sys_menu_bak` VALUES (339, '模板编辑', 335, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:template:edit', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (340, '模板发布', 335, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:template:publish', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (341, '绑定编辑', 336, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:bind:edit', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (342, '待办查询', 337, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:task:query', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (343, '审批操作', 337, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:task:action', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (344, '提交审批', 337, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:instance:submit', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (345, '发起查询', 338, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:instance:query', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu_bak` VALUES (346, '撤回审批', 338, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:instance:withdraw', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');

-- ----------------------------
-- Table structure for sys_notice
-- ----------------------------
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice`  (
  `notice_id` int(0) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `notice_title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告标题',
  `notice_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '公告类型（1通知 2公告）',
  `notice_content` longblob NULL COMMENT '公告内容',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '公告状态（0正常 1关闭）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`notice_id`) USING BTREE,
  INDEX `idx_notice_status_create`(`status`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知公告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_notice
-- ----------------------------
INSERT INTO `sys_notice` VALUES (3, '测试新增：2026-2-5 ', '1', 0x3C703EE6B58BE8AF95E696B0E5A29E3C2F703E, '0', 'admin', '2026-02-05 19:32:02', 'admin', '2026-06-01 12:19:10', NULL);

-- ----------------------------
-- Table structure for sys_online_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_online_user`;
CREATE TABLE `sys_online_user`  (
  `session_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会话ID（Redis key）',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录账号',
  `nick_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '用户昵称',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '部门名称',
  `ipaddr` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录IP地址',
  `login_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '登录地点',
  `browser` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '浏览器类型',
  `os` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '操作系统',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '1' COMMENT '状态（0在线 1离线）',
  `start_timestamp` datetime(0) NULL DEFAULT NULL COMMENT '会话开始时间（登录时间）',
  `last_access_time` datetime(0) NULL DEFAULT NULL COMMENT '最后访问时间',
  `end_timestamp` datetime(0) NULL DEFAULT NULL COMMENT '会话结束时间（登出/强制下线时间）',
  `expire_time` int(0) NULL DEFAULT NULL COMMENT '会话超时时间（秒）',
  `token_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '' COMMENT '令牌标识',
  `logout_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT '0' COMMENT '下线类型（0正常登出 1强制下线 2过期下线）',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间（记录入库时间）',
  PRIMARY KEY (`session_id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_start_timestamp`(`start_timestamp`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '在线用户记录表（历史记录）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_online_user
-- ----------------------------
INSERT INTO `sys_online_user` VALUES ('jwt:refresh:user:1', 1, 'admin', '超级管理员', '星枢科技', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome 149', 'Windows 10', '1', '2026-06-15 11:34:54', '2026-06-15 11:34:54', '2026-06-15 11:37:26', NULL, '', '0', '2026-06-15 11:37:26');
INSERT INTO `sys_online_user` VALUES ('jwt:refresh:user:100', 100, 'xinxin', '超级管理员', '星枢科技', '0:0:0:0:0:0:0:1', '内网IP', 'Edge 149', 'Windows 10', '1', '2026-06-09 18:54:00', '2026-06-09 18:54:00', '2026-06-09 19:00:12', NULL, '', '1', '2026-06-09 19:00:12');
INSERT INTO `sys_online_user` VALUES ('jwt:refresh:user:113', 113, 'wangwu', 'wangwu', '人事部', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome 143', 'Windows 10/11', '1', '2026-01-27 19:31:06', '2026-01-27 19:31:06', '2026-01-27 19:36:21', NULL, '', '0', '2026-01-27 19:36:21');
INSERT INTO `sys_online_user` VALUES ('jwt:refresh:user:114', 114, 'starPivot', 'starPivot演示用户', '山西分公司', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome 144', 'Windows 10', '1', '2026-02-09 13:31:46', '2026-02-09 13:31:46', '2026-02-09 13:32:38', NULL, '', '0', '2026-02-09 13:32:38');
INSERT INTO `sys_online_user` VALUES ('jwt:refresh:user:115', 115, 'xiaoming', 'xiaoming', '', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome 144', 'Windows 10', '1', '2026-02-12 16:49:35', '2026-02-12 16:49:35', '2026-02-12 16:54:27', NULL, '', '0', '2026-02-12 16:54:27');
INSERT INTO `sys_online_user` VALUES ('jwt:refresh:user:116', 116, '15536155268', '15536155268', '星枢科技', '183.200.95.198', '', 'Chrome Webview 134.0.6998.135', 'Android 16', '1', '2026-04-24 10:23:51', '2026-04-24 10:23:51', '2026-04-24 10:29:36', NULL, '', '1', '2026-04-24 10:29:36');
INSERT INTO `sys_online_user` VALUES ('jwt:refresh:user:2', 2, 'user', '用户管理员', '测试部门', '0:0:0:0:0:0:0:1', '内网IP', 'Chrome 144', 'Windows 10', '1', '2026-02-09 12:31:16', '2026-02-09 12:31:16', '2026-02-09 12:31:52', NULL, '', '0', '2026-02-09 12:31:52');

-- ----------------------------
-- Table structure for sys_oper_log
-- ----------------------------
DROP TABLE IF EXISTS `sys_oper_log`;
CREATE TABLE `sys_oper_log`  (
  `oper_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '日志主键',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '模块标题',
  `business_type` int(0) NULL DEFAULT 0 COMMENT '业务类型（0其它 1新增 2修改 3删除）',
  `method` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '方法名称',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '请求方式',
  `operator_type` int(0) NULL DEFAULT 0 COMMENT '操作类别（0其它 1后台用户 2手机端用户）',
  `oper_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作人员',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '部门名称',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '请求URL',
  `oper_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '主机地址',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '操作地点',
  `oper_param` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '请求参数',
  `json_result` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '返回参数',
  `status` int(0) NULL DEFAULT 0 COMMENT '操作状态（0正常 1异常）',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '错误消息',
  `oper_time` datetime(0) NULL DEFAULT NULL COMMENT '操作时间',
  `cost_time` bigint(0) NULL DEFAULT 0 COMMENT '消耗时间',
  PRIMARY KEY (`oper_id`) USING BTREE,
  INDEX `idx_sys_oper_log_bt`(`business_type`) USING BTREE,
  INDEX `idx_sys_oper_log_s`(`status`) USING BTREE,
  INDEX `idx_oper_time`(`oper_time`) USING BTREE,
  INDEX `idx_oper_name`(`oper_name`) USING BTREE,
  INDEX `idx_module_time`(`title`, `oper_time`) USING BTREE,
  INDEX `idx_operlog_time`(`oper_time`) USING BTREE,
  INDEX `idx_operlog_user`(`oper_name`) USING BTREE,
  INDEX `idx_operlog_type`(`business_type`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4890 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_oper_log
-- ----------------------------
INSERT INTO `sys_oper_log` VALUES (4864, '清空操作日志', 9, 'cn.org.starpivot.system.controller.SysOperLogController.clean()', 'DELETE', 1, 'admin', '星枢科技', '/api/v1/sys/operlog/clean', '0:0:0:0:0:0:0:1', '内网IP', '', '{\"code\":200,\"data\":\"清空成功\",\"fail\":false,\"message\":\"操作成功\",\"success\":true}', 0, '', '2026-06-27 10:43:17', 18);
INSERT INTO `sys_oper_log` VALUES (4865, '操作日志', 0, 'cn.org.starpivot.system.controller.SysOperLogController.pageList()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/operlog/pageList', '0:0:0:0:0:0:0:1', '内网IP', '{\"pageNum\":1,\"pageSize\":20}', '{\"code\":200,\"data\":{\"pageCount\":1,\"pageNum\":1,\"pageSize\":20,\"rows\":[{\"businessType\":9,\"costTime\":18,\"deptName\":\"星枢科技\",\"errorMsg\":\"\",\"jsonResult\":\"{\\\"code\\\":200,\\\"data\\\":\\\"清空成功\\\",\\\"fail\\\":false,\\\"message\\\":\\\"操作成功\\\",\\\"success\\\":true}\",\"method\":\"cn.org.starpivot.system.controller.SysOperLogController.clean()\",\"operId\":4864,\"operIp\":\"0:0:0:0:0:0:0:1\",\"operLocation\":\"内网IP\",\"operName\":\"admin\",\"operParam\":\"\",\"operTime\":\"2026-06-27 10:43:17\",\"operUrl\":\"/api/v1/sys/operlog/clean\",\"operatorType\":1,\"requestMethod\":\"DELETE\",\"status\":0,\"title\":\"清空操作日志\"}],\"total\":1},\"fail\":false,\"message\":\"操作成功\",\"success\":true}', 0, '', '2026-06-27 10:43:18', 14);
INSERT INTO `sys_oper_log` VALUES (4866, '登录日志', 0, 'cn.org.starpivot.system.controller.SysLogininforController.pageList()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/logininfor/pageList', '0:0:0:0:0:0:0:1', '内网IP', '{\"pageNum\":1,\"pageSize\":20}', '{\"code\":200,\"data\":{\"pageCount\":2,\"pageNum\":1,\"pageSize\":20,\"rows\":[{\"browser\":\"Chrome\",\"infoId\":497,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 18:58:00\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":496,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 18:33:45\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":495,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:56:01\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":494,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:42:44\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":493,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:41:26\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":492,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:41:16\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":491,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:40:29\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":490,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:39:52\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":489,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:36:16\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":488,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:35:03\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"use...', 0, '', '2026-06-27 10:43:20', 21);
INSERT INTO `sys_oper_log` VALUES (4867, '登录日志', 0, 'cn.org.starpivot.system.controller.SysLogininforController.pageList()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/logininfor/pageList', '0:0:0:0:0:0:0:1', '内网IP', '{\"pageNum\":1,\"pageSize\":20}', '{\"code\":200,\"data\":{\"pageCount\":2,\"pageNum\":1,\"pageSize\":20,\"rows\":[{\"browser\":\"Chrome\",\"infoId\":497,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 18:58:00\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":496,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 18:33:45\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":495,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:56:01\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":494,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:42:44\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":493,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:41:26\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":492,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:41:16\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":491,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:40:29\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":490,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:39:52\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":489,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:36:16\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":488,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:35:03\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"use...', 0, '', '2026-06-27 10:43:32', 20);
INSERT INTO `sys_oper_log` VALUES (4868, '操作日志', 0, 'cn.org.starpivot.system.controller.SysOperLogController.pageList()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/operlog/pageList', '0:0:0:0:0:0:0:1', '内网IP', '{\"pageNum\":1,\"pageSize\":20}', '{\"code\":200,\"data\":{\"pageCount\":1,\"pageNum\":1,\"pageSize\":20,\"rows\":[{\"businessType\":0,\"costTime\":20,\"deptName\":\"星枢科技\",\"errorMsg\":\"\",\"jsonResult\":\"{\\\"code\\\":200,\\\"data\\\":{\\\"pageCount\\\":2,\\\"pageNum\\\":1,\\\"pageSize\\\":20,\\\"rows\\\":[{\\\"browser\\\":\\\"Chrome\\\",\\\"infoId\\\":497,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 18:58:00\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Chrome\\\",\\\"infoId\\\":496,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 18:33:45\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Unknown\\\",\\\"infoId\\\":495,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 14:56:01\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Unknown\\\",\\\"infoId\\\":494,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 14:42:44\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Unknown\\\",\\\"infoId\\\":493,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 14:41:26\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Unknown\\\",\\\"infoId\\\":492,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 14:41:16\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Unknown\\\",\\\"infoId\\\":491,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 14:40:29\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Unknown\\\",\\\"infoId\\\":490,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 14:39:52\\\",\\\"msg\\\":\\\"登录成功...', 0, '', '2026-06-27 10:43:45', 14);
INSERT INTO `sys_oper_log` VALUES (4869, '登录日志', 0, 'cn.org.starpivot.system.controller.SysLogininforController.pageList()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/logininfor/pageList', '0:0:0:0:0:0:0:1', '内网IP', '{\"pageNum\":1,\"pageSize\":20}', '{\"code\":200,\"data\":{\"pageCount\":2,\"pageNum\":1,\"pageSize\":20,\"rows\":[{\"browser\":\"Chrome\",\"infoId\":497,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 18:58:00\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":496,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 18:33:45\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":495,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:56:01\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":494,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:42:44\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":493,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:41:26\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":492,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:41:16\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":491,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:40:29\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":490,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:39:52\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":489,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:36:16\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":488,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:35:03\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"use...', 0, '', '2026-06-27 10:43:46', 13);
INSERT INTO `sys_oper_log` VALUES (4870, '操作日志', 0, 'cn.org.starpivot.system.controller.SysOperLogController.pageList()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/operlog/pageList', '0:0:0:0:0:0:0:1', '内网IP', '{\"pageNum\":1,\"pageSize\":20}', '{\"code\":200,\"data\":{\"pageCount\":1,\"pageNum\":1,\"pageSize\":20,\"rows\":[{\"businessType\":0,\"costTime\":13,\"deptName\":\"星枢科技\",\"errorMsg\":\"\",\"jsonResult\":\"{\\\"code\\\":200,\\\"data\\\":{\\\"pageCount\\\":2,\\\"pageNum\\\":1,\\\"pageSize\\\":20,\\\"rows\\\":[{\\\"browser\\\":\\\"Chrome\\\",\\\"infoId\\\":497,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 18:58:00\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Chrome\\\",\\\"infoId\\\":496,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 18:33:45\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Unknown\\\",\\\"infoId\\\":495,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 14:56:01\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Unknown\\\",\\\"infoId\\\":494,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 14:42:44\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Unknown\\\",\\\"infoId\\\":493,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 14:41:26\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Unknown\\\",\\\"infoId\\\":492,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 14:41:16\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Unknown\\\",\\\"infoId\\\":491,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 14:40:29\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10\\\",\\\"status\\\":\\\"0\\\",\\\"userName\\\":\\\"admin\\\"},{\\\"browser\\\":\\\"Unknown\\\",\\\"infoId\\\":490,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-26 14:39:52\\\",\\\"msg\\\":\\\"登录成功...', 0, '', '2026-06-29 17:07:18', 223);
INSERT INTO `sys_oper_log` VALUES (4871, '登录日志', 0, 'cn.org.starpivot.system.controller.SysLogininforController.pageList()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/logininfor/pageList', '0:0:0:0:0:0:0:1', '内网IP', '{\"pageNum\":1,\"pageSize\":20}', '{\"code\":200,\"data\":{\"pageCount\":2,\"pageNum\":1,\"pageSize\":20,\"rows\":[{\"browser\":\"Chrome\",\"infoId\":498,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-29 17:06:41\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":497,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 18:58:00\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":496,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 18:33:45\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":495,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:56:01\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":494,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:42:44\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":493,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:41:26\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":492,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:41:16\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":491,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:40:29\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":490,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:39:52\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":489,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:36:16\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"user...', 0, '', '2026-06-29 17:07:20', 31);
INSERT INTO `sys_oper_log` VALUES (4872, '修改菜单', 2, 'cn.org.starpivot.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', '星枢科技', '/api/v1/sys/menu', '0:0:0:0:0:0:0:1', '内网IP', '{\"menuId\":1,\"menuName\":\"系统管理11\",\"orderNum\":1,\"path\":\"/system\",\"component\":\"\",\"routeName\":\"System\",\"isFrame\":1,\"isCache\":1,\"menuType\":\"M\",\"visible\":\"0\",\"status\":\"0\",\"perms\":\"\",\"icon\":\"ep:setting\",\"remark\":\"系统管理模块111\"}', '{\"code\":200,\"data\":\"修改菜单成功\",\"fail\":false,\"message\":\"操作成功\",\"success\":true}', 0, '', '2026-06-29 18:20:10', 156);
INSERT INTO `sys_oper_log` VALUES (4873, '修改菜单', 2, 'cn.org.starpivot.system.controller.SysMenuController.edit()', 'PUT', 1, 'admin', '星枢科技', '/api/v1/sys/menu', '0:0:0:0:0:0:0:1', '内网IP', '{\"menuId\":1,\"menuName\":\"系统管理\",\"orderNum\":1,\"path\":\"/system\",\"component\":\"\",\"routeName\":\"System\",\"isFrame\":1,\"isCache\":1,\"menuType\":\"M\",\"visible\":\"0\",\"status\":\"0\",\"perms\":\"\",\"icon\":\"ep:setting\",\"remark\":\"系统管理模块111\"}', '{\"code\":200,\"data\":\"修改菜单成功\",\"fail\":false,\"message\":\"操作成功\",\"success\":true}', 0, '', '2026-06-29 18:23:27', 31);
INSERT INTO `sys_oper_log` VALUES (4874, '操作日志', 0, 'cn.org.starpivot.system.controller.SysOperLogController.pageList()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/operlog/pageList', '0:0:0:0:0:0:0:1', '内网IP', '{\"pageNum\":1,\"pageSize\":20}', '{\"code\":200,\"data\":{\"pageCount\":1,\"pageNum\":1,\"pageSize\":20,\"rows\":[{\"businessType\":2,\"costTime\":31,\"deptName\":\"星枢科技\",\"errorMsg\":\"\",\"jsonResult\":\"{\\\"code\\\":200,\\\"data\\\":\\\"修改菜单成功\\\",\\\"fail\\\":false,\\\"message\\\":\\\"操作成功\\\",\\\"success\\\":true}\",\"method\":\"cn.org.starpivot.system.controller.SysMenuController.edit()\",\"operId\":4873,\"operIp\":\"0:0:0:0:0:0:0:1\",\"operLocation\":\"内网IP\",\"operName\":\"admin\",\"operParam\":\"{\\\"menuId\\\":1,\\\"menuName\\\":\\\"系统管理\\\",\\\"orderNum\\\":1,\\\"path\\\":\\\"/system\\\",\\\"component\\\":\\\"\\\",\\\"routeName\\\":\\\"System\\\",\\\"isFrame\\\":1,\\\"isCache\\\":1,\\\"menuType\\\":\\\"M\\\",\\\"visible\\\":\\\"0\\\",\\\"status\\\":\\\"0\\\",\\\"perms\\\":\\\"\\\",\\\"icon\\\":\\\"ep:setting\\\",\\\"remark\\\":\\\"系统管理模块111\\\"}\",\"operTime\":\"2026-06-29 18:23:27\",\"operUrl\":\"/api/v1/sys/menu\",\"operatorType\":1,\"requestMethod\":\"PUT\",\"status\":0,\"title\":\"修改菜单\"},{\"businessType\":2,\"costTime\":156,\"deptName\":\"星枢科技\",\"errorMsg\":\"\",\"jsonResult\":\"{\\\"code\\\":200,\\\"data\\\":\\\"修改菜单成功\\\",\\\"fail\\\":false,\\\"message\\\":\\\"操作成功\\\",\\\"success\\\":true}\",\"method\":\"cn.org.starpivot.system.controller.SysMenuController.edit()\",\"operId\":4872,\"operIp\":\"0:0:0:0:0:0:0:1\",\"operLocation\":\"内网IP\",\"operName\":\"admin\",\"operParam\":\"{\\\"menuId\\\":1,\\\"menuName\\\":\\\"系统管理11\\\",\\\"orderNum\\\":1,\\\"path\\\":\\\"/system\\\",\\\"component\\\":\\\"\\\",\\\"routeName\\\":\\\"System\\\",\\\"isFrame\\\":1,\\\"isCache\\\":1,\\\"menuType\\\":\\\"M\\\",\\\"visible\\\":\\\"0\\\",\\\"status\\\":\\\"0\\\",\\\"perms\\\":\\\"\\\",\\\"icon\\\":\\\"ep:setting\\\",\\\"remark\\\":\\\"系统管理模块111\\\"}\",\"operTime\":\"2026-06-29 18:20:10\",\"operUrl\":\"/api/v1/sys/menu\",\"operatorType\":1,\"requestMethod\":\"PUT\",\"status\":0,\"title\":\"修改菜单\"},{\"businessType\":0,\"costTime\":31,\"deptName\":\"星枢科技\",\"errorMsg\":\"\",\"jsonResult\":\"{\\\"code\\\":200,\\\"data\\\":{\\\"pageCount\\\":2,\\\"pageNum\\\":1,\\\"pageSize\\\":20,\\\"rows\\\":[{\\\"browser\\\":\\\"Chrome\\\",\\\"infoId\\\":498,\\\"ipaddr\\\":\\\"0:0:0:0:0:0:0:1\\\",\\\"loginLocation\\\":\\\"内网IP\\\",\\\"loginTime\\\":\\\"2026-06-29 17:06:41\\\",\\\"msg\\\":\\\"登录成功\\\",\\\"os\\\":\\\"Windows 10...', 0, '', '2026-06-29 19:48:19', 141);
INSERT INTO `sys_oper_log` VALUES (4875, '登录日志', 0, 'cn.org.starpivot.system.controller.SysLogininforController.pageList()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/logininfor/pageList', '0:0:0:0:0:0:0:1', '内网IP', '{\"pageNum\":1,\"pageSize\":20}', '{\"code\":200,\"data\":{\"pageCount\":2,\"pageNum\":1,\"pageSize\":20,\"rows\":[{\"browser\":\"Chrome\",\"infoId\":499,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-29 18:19:41\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":498,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-29 17:06:41\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":497,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 18:58:00\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":496,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 18:33:45\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":495,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:56:01\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":494,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:42:44\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":493,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:41:26\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":492,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:41:16\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":491,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:40:29\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Unknown\",\"infoId\":490,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 14:39:52\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userN...', 0, '', '2026-06-29 19:48:20', 21);
INSERT INTO `sys_oper_log` VALUES (4876, '上传用户头像', 2, 'cn.org.starpivot.system.controller.AvatarController.upload()', 'POST', 1, 'admin', '星枢科技', '/api/v1/avatar/upload', '172.19.0.1', '内网IP', '[\"1\",true]', '{\"code\":200,\"data\":{\"avatarUrl\":\"/local-storage/avatar/1.jpeg\",\"presignedUrl\":\"/local-storage/avatar/1.jpeg\",\"isPresigned\":\"true\"},\"fail\":false,\"message\":\"上传成功\",\"success\":true}', 0, '', '2026-06-30 06:14:27', 130);
INSERT INTO `sys_oper_log` VALUES (4877, '修改用户', 2, 'cn.org.starpivot.system.controller.SysUserController.updateUser()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/user/update', '172.19.0.1', '内网IP', '{\"userId\":1,\"deptId\":100,\"userName\":\"admin\",\"nickName\":\"超级管理员\",\"email\":\"******\",\"avatar\":\"/local-storage/avatar/1.jpeg\",\"phonenumber\":\"******\",\"sex\":\"0\",\"status\":\"0\",\"remark\":\"超级管理员\",\"roleIds\":[1,6],\"postIds\":[1]}', '{\"code\":200,\"fail\":false,\"message\":\"修改用户成功\",\"success\":true}', 0, '', '2026-06-30 06:14:29', 59);
INSERT INTO `sys_oper_log` VALUES (4878, '删除用户头像', 3, 'cn.org.starpivot.system.controller.AvatarController.delete()', 'DELETE', 1, 'admin', '星枢科技', '/api/v1/avatar/delete', '0:0:0:0:0:0:0:1', '内网IP', '\"1\"', '{\"code\":200,\"data\":\"删除成功\",\"fail\":false,\"message\":\"操作成功\",\"success\":true}', 0, '', '2026-06-30 15:08:47', 482);
INSERT INTO `sys_oper_log` VALUES (4879, '上传用户头像', 2, 'cn.org.starpivot.system.controller.AvatarController.upload()', 'POST', 1, 'admin', '星枢科技', '/api/v1/avatar/upload', '0:0:0:0:0:0:0:1', '内网IP', '[\"1\",true]', '{\"code\":200,\"data\":{\"avatarUrl\":\"https://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/1.jpeg\",\"presignedUrl\":\"http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/1.jpeg?Expires=1783408131&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=pBFAtc9ZPlPTZUvVKn9axMWwQR8%3D\",\"isPresigned\":\"true\"},\"fail\":false,\"message\":\"上传成功\",\"success\":true}', 0, '', '2026-06-30 15:08:52', 189);
INSERT INTO `sys_oper_log` VALUES (4880, '修改用户', 2, 'cn.org.starpivot.system.controller.SysUserController.updateUser()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/user/update', '0:0:0:0:0:0:0:1', '内网IP', '{\"userId\":1,\"deptId\":100,\"userName\":\"admin\",\"nickName\":\"超级管理员\",\"email\":\"******\",\"avatar\":\"http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/1.jpeg?Expires=1783408131&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=pBFAtc9ZPlPTZUvVKn9axMWwQR8%3D\",\"phonenumber\":\"******\",\"sex\":\"0\",\"status\":\"0\",\"remark\":\"超级管理员\",\"roleIds\":[1,6],\"postIds\":[1]}', '{\"code\":200,\"fail\":false,\"message\":\"修改用户成功\",\"success\":true}', 0, '', '2026-06-30 15:08:53', 69);
INSERT INTO `sys_oper_log` VALUES (4881, '上传用户头像', 2, 'cn.org.starpivot.system.controller.AvatarController.upload()', 'POST', 1, 'admin', '星枢科技', '/api/v1/avatar/upload', '0:0:0:0:0:0:0:1', '内网IP', '[\"100\",true]', '{\"code\":200,\"data\":{\"avatarUrl\":\"https://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/100.webp\",\"presignedUrl\":\"http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/100.webp?Expires=1783408143&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=gN%2FbKm%2B0F%2Fpk%2FMngsWzj3ra8TzE%3D\",\"isPresigned\":\"true\"},\"fail\":false,\"message\":\"上传成功\",\"success\":true}', 0, '', '2026-06-30 15:09:04', 82);
INSERT INTO `sys_oper_log` VALUES (4882, '修改用户', 2, 'cn.org.starpivot.system.controller.SysUserController.updateUser()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/user/update', '0:0:0:0:0:0:0:1', '内网IP', '{\"userId\":100,\"deptId\":100,\"userName\":\"xinxin\",\"nickName\":\"超级管理员\",\"email\":\"******\",\"avatar\":\"http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/100.webp?Expires=1783408143&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=gN%2FbKm%2B0F%2Fpk%2FMngsWzj3ra8TzE%3D\",\"phonenumber\":\"******\",\"sex\":\"0\",\"status\":\"0\",\"remark\":\"超级管理员\",\"roleIds\":[1],\"postIds\":[1]}', '{\"code\":200,\"fail\":false,\"message\":\"修改用户成功\",\"success\":true}', 0, '', '2026-06-30 15:09:05', 49);
INSERT INTO `sys_oper_log` VALUES (4883, '上传用户头像', 2, 'cn.org.starpivot.system.controller.AvatarController.upload()', 'POST', 1, 'admin', '星枢科技', '/api/v1/avatar/upload', '0:0:0:0:0:0:0:1', '内网IP', '[\"101\",true]', '{\"code\":200,\"data\":{\"avatarUrl\":\"https://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp\",\"presignedUrl\":\"http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp?Expires=1783408151&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=2%2FhBISg7Gl7GGSymG4DEIl1TdnU%3D\",\"isPresigned\":\"true\"},\"fail\":false,\"message\":\"上传成功\",\"success\":true}', 0, '', '2026-06-30 15:09:12', 84);
INSERT INTO `sys_oper_log` VALUES (4884, '修改用户', 2, 'cn.org.starpivot.system.controller.SysUserController.updateUser()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/user/update', '0:0:0:0:0:0:0:1', '内网IP', '{\"userId\":101,\"deptId\":105,\"userName\":\"test\",\"nickName\":\"测试用户\",\"email\":\"******\",\"avatar\":\"http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp?Expires=1783408151&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=2%2FhBISg7Gl7GGSymG4DEIl1TdnU%3D\",\"phonenumber\":\"******\",\"sex\":\"0\",\"status\":\"0\",\"remark\":\"测试用户专属\",\"roleIds\":[],\"postIds\":[]}', '{\"code\":200,\"fail\":false,\"message\":\"修改用户成功\",\"success\":true}', 0, '', '2026-06-30 15:09:13', 24);
INSERT INTO `sys_oper_log` VALUES (4885, '删除用户头像', 3, 'cn.org.starpivot.system.controller.AvatarController.delete()', 'DELETE', 1, 'admin', '星枢科技', '/api/v1/avatar/delete', '0:0:0:0:0:0:0:1', '内网IP', '\"101\"', '{\"code\":200,\"data\":\"删除成功\",\"fail\":false,\"message\":\"操作成功\",\"success\":true}', 0, '', '2026-06-30 15:27:32', 97);
INSERT INTO `sys_oper_log` VALUES (4886, '上传用户头像', 2, 'cn.org.starpivot.system.controller.AvatarController.upload()', 'POST', 1, 'admin', '星枢科技', '/api/v1/avatar/upload', '0:0:0:0:0:0:0:1', '内网IP', '[\"101\",true]', '{\"code\":200,\"data\":{\"avatarUrl\":\"https://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp\",\"presignedUrl\":\"http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp?Expires=1783409258&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=pXajrKUFQqnFhG32Icj1u2hfwYQ%3D\",\"isPresigned\":\"true\"},\"fail\":false,\"message\":\"上传成功\",\"success\":true}', 0, '', '2026-06-30 15:27:38', 85);
INSERT INTO `sys_oper_log` VALUES (4887, '修改用户', 2, 'cn.org.starpivot.system.controller.SysUserController.updateUser()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/user/update', '0:0:0:0:0:0:0:1', '内网IP', '{\"userId\":101,\"deptId\":105,\"userName\":\"test\",\"nickName\":\"测试用户\",\"email\":\"******\",\"avatar\":\"http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp?Expires=1783409258&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=pXajrKUFQqnFhG32Icj1u2hfwYQ%3D\",\"phonenumber\":\"******\",\"sex\":\"0\",\"status\":\"0\",\"remark\":\"测试用户专属\",\"roleIds\":[],\"postIds\":[]}', '{\"code\":200,\"fail\":false,\"message\":\"修改用户成功\",\"success\":true}', 0, '', '2026-06-30 15:27:39', 31);
INSERT INTO `sys_oper_log` VALUES (4888, '上传用户头像', 2, 'cn.org.starpivot.system.controller.AvatarController.upload()', 'POST', 1, 'admin', '星枢科技', '/api/v1/avatar/upload', '0:0:0:0:0:0:0:1', '内网IP', '[\"101\",true]', '{\"code\":200,\"data\":{\"avatarUrl\":\"https://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp\",\"presignedUrl\":\"http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp?Expires=1783409494&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=SHVtAbtjcsHfq4UAfwWjb1L8TYA%3D\",\"isPresigned\":\"true\"},\"fail\":false,\"message\":\"上传成功\",\"success\":true}', 0, '', '2026-06-30 15:31:34', 154);
INSERT INTO `sys_oper_log` VALUES (4889, '修改用户', 2, 'cn.org.starpivot.system.controller.SysUserController.updateUser()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/user/update', '0:0:0:0:0:0:0:1', '内网IP', '{\"userId\":101,\"deptId\":105,\"userName\":\"test\",\"nickName\":\"测试用户\",\"email\":\"******\",\"avatar\":\"http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp?Expires=1783409494&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=SHVtAbtjcsHfq4UAfwWjb1L8TYA%3D\",\"phonenumber\":\"******\",\"sex\":\"0\",\"status\":\"0\",\"remark\":\"测试用户专属\",\"roleIds\":[],\"postIds\":[]}', '{\"code\":200,\"fail\":false,\"message\":\"修改用户成功\",\"success\":true}', 0, '', '2026-06-30 15:31:36', 30);
INSERT INTO `sys_oper_log` VALUES (4890, '操作日志', 0, 'cn.org.starpivot.system.controller.SysOperLogController.list()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/operlog/operLogPageList', '0:0:0:0:0:0:0:1', '内网IP', '{\"pageNum\":1,\"pageSize\":20}', '{\"code\":200,\"data\":{\"pageCount\":2,\"pageNum\":1,\"pageSize\":20,\"rows\":[{\"businessType\":2,\"costTime\":30,\"deptName\":\"星枢科技\",\"errorMsg\":\"\",\"jsonResult\":\"{\\\"code\\\":200,\\\"fail\\\":false,\\\"message\\\":\\\"修改用户成功\\\",\\\"success\\\":true}\",\"method\":\"cn.org.starpivot.system.controller.SysUserController.updateUser()\",\"operId\":4889,\"operIp\":\"0:0:0:0:0:0:0:1\",\"operLocation\":\"内网IP\",\"operName\":\"admin\",\"operParam\":\"{\\\"userId\\\":101,\\\"deptId\\\":105,\\\"userName\\\":\\\"test\\\",\\\"nickName\\\":\\\"测试用户\\\",\\\"email\\\":\\\"******\\\",\\\"avatar\\\":\\\"http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp?Expires=1783409494&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=SHVtAbtjcsHfq4UAfwWjb1L8TYA%3D\\\",\\\"phonenumber\\\":\\\"******\\\",\\\"sex\\\":\\\"0\\\",\\\"status\\\":\\\"0\\\",\\\"remark\\\":\\\"测试用户专属\\\",\\\"roleIds\\\":[],\\\"postIds\\\":[]}\",\"operTime\":\"2026-06-30 15:31:36\",\"operUrl\":\"/api/v1/sys/user/update\",\"operatorType\":1,\"requestMethod\":\"POST\",\"status\":0,\"title\":\"修改用户\"},{\"businessType\":2,\"costTime\":154,\"deptName\":\"星枢科技\",\"errorMsg\":\"\",\"jsonResult\":\"{\\\"code\\\":200,\\\"data\\\":{\\\"avatarUrl\\\":\\\"https://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp\\\",\\\"presignedUrl\\\":\\\"http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp?Expires=1783409494&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=SHVtAbtjcsHfq4UAfwWjb1L8TYA%3D\\\",\\\"isPresigned\\\":\\\"true\\\"},\\\"fail\\\":false,\\\"message\\\":\\\"上传成功\\\",\\\"success\\\":true}\",\"method\":\"cn.org.starpivot.system.controller.AvatarController.upload()\",\"operId\":4888,\"operIp\":\"0:0:0:0:0:0:0:1\",\"operLocation\":\"内网IP\",\"operName\":\"admin\",\"operParam\":\"[\\\"101\\\",true]\",\"operTime\":\"2026-06-30 15:31:34\",\"operUrl\":\"/api/v1/avatar/upload\",\"operatorType\":1,\"requestMethod\":\"POST\",\"status\":0,\"title\":\"上传用户头像\"},{\"businessType\":2,\"costTime\":31,\"deptName\":\"星枢科技\",\"errorMsg\":\"\",\"jsonResult\":\"{\\\"code\\\":200,\\\"fail\\\":false,\\\"message\\\":\\\"修改用户成功\\\",\\\"success\\\":true}\",\"method\":\"cn.org.starpivot.system.controller.SysUserControll...', 0, '', '2026-07-01 11:08:10', 213);
INSERT INTO `sys_oper_log` VALUES (4891, '登录日志', 0, 'cn.org.starpivot.system.controller.SysLogininforController.list()', 'POST', 1, 'admin', '星枢科技', '/api/v1/sys/logininfor/logininforPageList', '0:0:0:0:0:0:0:1', '内网IP', '{\"pageNum\":1,\"pageSize\":20}', '{\"code\":200,\"data\":{\"pageCount\":3,\"pageNum\":1,\"pageSize\":20,\"rows\":[{\"browser\":\"Chrome\",\"infoId\":506,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-30 18:09:14\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":505,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-30 16:13:48\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":504,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-30 15:44:50\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":503,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-30 15:31:23\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":502,\"ipaddr\":\"172.19.0.1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-30 06:13:27\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":501,\"ipaddr\":\"172.19.0.1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-30 06:05:51\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":500,\"ipaddr\":\"172.19.0.1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-30 06:04:46\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":499,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-29 18:19:41\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":498,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-29 17:06:41\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"browser\":\"Chrome\",\"infoId\":497,\"ipaddr\":\"0:0:0:0:0:0:0:1\",\"loginLocation\":\"内网IP\",\"loginTime\":\"2026-06-26 18:58:00\",\"msg\":\"登录成功\",\"os\":\"Windows 10\",\"status\":\"0\",\"userName\":\"admin\"},{\"brows...', 0, '', '2026-07-01 11:08:11', 22);
INSERT INTO `sys_oper_log` VALUES (4892, '删除菜单', 3, 'cn.org.starpivot.system.controller.SysMenuController.remove()', 'DELETE', 1, 'admin', '星枢科技', '/api/v1/sys/menu/removeMenu', '0:0:0:0:0:0:0:1', '内网IP', '{\"ids\":[142]}', '', 1, '菜单ID 142 已被角色使用，不允许删除', '2026-07-01 11:10:08', 20);
INSERT INTO `sys_oper_log` VALUES (4893, '删除菜单', 3, 'cn.org.starpivot.system.controller.SysMenuController.remove()', 'DELETE', 1, 'admin', '星枢科技', '/api/v1/sys/menu/removeMenu', '0:0:0:0:0:0:0:1', '内网IP', '{\"ids\":[142]}', '', 1, '菜单ID 142 已被角色使用，不允许删除', '2026-07-01 11:10:33', 9);

-- ----------------------------
-- Table structure for sys_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_post`;
CREATE TABLE `sys_post`  (
  `post_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '岗位ID',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位编码',
  `post_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '岗位名称',
  `post_sort` int(0) NOT NULL COMMENT '显示顺序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`post_id`) USING BTREE,
  UNIQUE INDEX `uk_post_code`(`post_code`) USING BTREE,
  INDEX `idx_post_status`(`status`) USING BTREE,
  INDEX `idx_post_sort`(`post_sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '岗位信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_post
-- ----------------------------
INSERT INTO `sys_post` VALUES (1, 'CEO', '董事长', 1, '0', 'admin', '2025-12-28 13:46:34', 'admin', '2026-06-02 13:42:38', '');
INSERT INTO `sys_post` VALUES (2, 'SE', '项目经理', 2, '0', 'admin', '2025-12-28 13:46:34', 'admin', '2026-06-02 13:42:43', '');
INSERT INTO `sys_post` VALUES (3, 'HR', '人力资源', 3, '0', 'admin', '2025-12-28 13:46:34', 'admin', '2026-06-02 13:42:48', '');
INSERT INTO `sys_post` VALUES (4, 'USER', '普通员工', 4, '0', 'admin', '2025-12-28 13:46:34', 'admin', '2026-06-02 13:42:54', '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色权限字符串',
  `role_sort` int(0) NOT NULL COMMENT '显示顺序',
  `data_scope` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '1' COMMENT '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
  `menu_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '菜单树选择项是否关联显示',
  `dept_check_strictly` tinyint(1) NULL DEFAULT 1 COMMENT '部门树选择项是否关联显示',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE INDEX `uk_role_key`(`role_key`) USING BTREE,
  INDEX `idx_del_flag`(`del_flag`) USING BTREE,
  INDEX `idx_role_sort`(`role_sort`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE,
  INDEX `idx_del_status`(`del_flag`, `status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_role_status_del`(`status`, `del_flag`) USING BTREE,
  INDEX `idx_role_key`(`role_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '超级管理员', 'admin', 1, '1', 1, 1, '0', '0', 'admin', '2025-12-28 13:46:34', 'admin', '2026-01-23 19:59:34', '超级管理员：拥有所有菜单权限');
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, '2', 1, 1, '0', '0', 'admin', '2025-12-28 13:46:34', 'admin', '2026-02-12 17:54:47', '普通角色');
INSERT INTO `sys_role` VALUES (3, '测试专属', 'test', 3, '2', 1, 1, '0', '0', '', '2026-01-03 17:06:10', 'admin', '2026-06-09 15:12:02', '专属于测试的角色');
INSERT INTO `sys_role` VALUES (4, '演示专属', 'yanshi', 4, '4', 0, 1, '0', '0', 'admin', '2026-02-09 12:25:59', 'admin', '2026-02-12 17:55:59', '演示专属角色');
INSERT INTO `sys_role` VALUES (5, '注册用户', 'register_role', 5, '5', 0, 1, '0', '0', 'admin', '2026-02-12 16:57:43', 'admin', '2026-05-14 14:07:40', '注册时，新用户初始化角色');
INSERT INTO `sys_role` VALUES (6, '财务', 'finance', 6, '1', 0, 1, '0', '0', 'admin', '2026-06-26 11:08:58', '', NULL, '财务');

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  `dept_id` bigint(0) NOT NULL COMMENT '部门ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_role_dept`(`role_id`, `dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 169 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色与部门关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------
INSERT INTO `sys_role_dept` VALUES (167, 2, 100);
INSERT INTO `sys_role_dept` VALUES (168, 2, 101);
INSERT INTO `sys_role_dept` VALUES (169, 2, 104);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(0) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_role_menu`(`role_id`, `menu_id`) USING BTREE,
  UNIQUE INDEX `uk_role_menu`(`role_id`, `menu_id`) USING BTREE,
  UNIQUE INDEX `idx_role_menu_unique`(`role_id`, `menu_id`) USING BTREE,
  INDEX `idx_role_menu_cover`(`role_id`, `menu_id`) USING BTREE,
  INDEX `idx_menu_id`(`menu_id`) USING BTREE,
  INDEX `idx_role_menu_role`(`role_id`) USING BTREE,
  INDEX `idx_role_menu_menu`(`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1248 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色与菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint(0) NULL DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户昵称',
  `user_type` varchar(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '00' COMMENT '用户类型（00系统用户）',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `phonenumber` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '手机号码',
  `sex` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '密码',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '账号状态（0正常 1停用）',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  `login_ip` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '最后登录IP',
  `login_date` datetime(0) NULL DEFAULT NULL COMMENT '最后登录时间',
  `pwd_update_date` datetime(0) NULL DEFAULT NULL COMMENT '密码最后更新时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_user_name`(`user_name`) USING BTREE,
  UNIQUE INDEX `idx_user_username_del`(`user_name`, `del_flag`) USING BTREE,
  INDEX `idx_username`(`user_name`) USING BTREE,
  INDEX `idx_phone`(`phonenumber`) USING BTREE,
  INDEX `idx_email`(`email`) USING BTREE,
  INDEX `idx_dept_id`(`dept_id`) USING BTREE,
  INDEX `idx_del_flag_status`(`del_flag`, `status`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE,
  INDEX `idx_dept_del_status`(`dept_id`, `del_flag`, `status`) USING BTREE,
  INDEX `idx_phonenumber`(`phonenumber`) USING BTREE,
  INDEX `idx_user_name`(`user_name`) USING BTREE,
  INDEX `idx_status_delflag`(`status`, `del_flag`) USING BTREE,
  INDEX `idx_user_status_del`(`status`, `del_flag`) USING BTREE,
  INDEX `idx_user_create_time`(`create_time`) USING BTREE,
  INDEX `idx_user_dept_status`(`dept_id`, `status`, `del_flag`) USING BTREE,
  INDEX `idx_user_nickname`(`nick_name`) USING BTREE,
  INDEX `idx_user_phone`(`phonenumber`) USING BTREE,
  INDEX `idx_user_email`(`email`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 120 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 100, 'admin', '超级管理员', '00', 'admin@163.com', '18888888888', '0', 'http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/1.jpeg?Expires=1783408131&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=pBFAtc9ZPlPTZUvVKn9axMWwQR8%3D', '$2a$10$h9dX1p1v.cRRPymoWCbZOOQPqMoVQZ9W.R5u6.wCJ5rm6Ru2ORYsS', '0', '0', '0:0:0:0:0:0:0:1', '2025-12-29 22:16:38', '2026-06-23 10:11:22', 'admin', '2025-12-28 13:46:34', 'admin', '2026-06-30 15:08:52', '超级管理员');
INSERT INTO `sys_user` VALUES (2, 105, 'user', '用户管理员', '00', 'user@qq.com', '15666666666', '1', 'http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/2.webp?Expires=1771492975&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=crgqBsWiZLm6O%2BvLufm%2BJF4wYd4%3D', '$2a$12$eYocwR0zs3iWKl7gsvHVQ.KLDTWvXqecm.29aXPi3IAF.mmkARVR.', '0', '0', '0:0:0:0:0:0:0:1', '2025-12-29 22:08:54', '2025-12-28 13:46:34', 'admin', '2025-12-28 13:46:34', 'admin', '2026-02-12 17:22:56', '测试员--用户管理员');
INSERT INTO `sys_user` VALUES (100, 100, 'xinxin', '超级管理员', '00', '123@qq.com', '18888888888', '0', 'http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/100.webp?Expires=1783408143&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=gN%2FbKm%2B0F%2Fpk%2FMngsWzj3ra8TzE%3D', '$2a$10$eFW.co7zyXl//sJwjqxFHuL.sx4vn2AaX2LJmIjF/KMTvh841WJry', '0', '0', '', NULL, '2026-06-09 19:00:12', 'admin', '2026-01-04 15:34:36', 'admin', '2026-06-30 15:09:05', '超级管理员');
INSERT INTO `sys_user` VALUES (101, 105, 'test', '测试用户', '00', '123@qq.com', '18825454547', '0', 'http://star-pivot.oss-cn-beijing.aliyuncs.com/avatar/101.webp?Expires=1783409494&OSSAccessKeyId=LTAI5tG8uSJeTeuRYEY4x3nG&Signature=SHVtAbtjcsHfq4UAfwWjb1L8TYA%3D', '$2a$10$YdqAWweActfkWOaWEjz9p.bBWqNWVdT9EQ2OHcUODgFg.f3ma13Va', '0', '0', '', NULL, NULL, 'admin', '2026-01-04 16:50:12', 'admin', '2026-06-30 15:31:36', '测试用户专属');

-- ----------------------------
-- Table structure for sys_user_post
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_post`;
CREATE TABLE `sys_user_post`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `post_id` bigint(0) NOT NULL COMMENT '岗位ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_post`(`user_id`, `post_id`) USING BTREE,
  UNIQUE INDEX `uk_user_post`(`user_id`, `post_id`) USING BTREE,
  INDEX `idx_post_id`(`post_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 220 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户与岗位关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_post
-- ----------------------------
INSERT INTO `sys_user_post` VALUES (218, 1, 1);
INSERT INTO `sys_user_post` VALUES (195, 2, 2);
INSERT INTO `sys_user_post` VALUES (219, 100, 1);
INSERT INTO `sys_user_post` VALUES (201, 102, 4);
INSERT INTO `sys_user_post` VALUES (192, 113, 4);
INSERT INTO `sys_user_post` VALUES (190, 114, 4);
INSERT INTO `sys_user_post` VALUES (197, 115, 4);
INSERT INTO `sys_user_post` VALUES (202, 116, 3);
INSERT INTO `sys_user_post` VALUES (204, 117, 4);
INSERT INTO `sys_user_post` VALUES (208, 118, 4);
INSERT INTO `sys_user_post` VALUES (210, 119, 4);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_user_role`(`user_id`, `role_id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id`, `role_id`) USING BTREE,
  UNIQUE INDEX `idx_user_role_unique`(`user_id`, `role_id`) USING BTREE,
  INDEX `idx_user_role_cover`(`user_id`, `role_id`) USING BTREE,
  INDEX `idx_role_id`(`role_id`) USING BTREE,
  INDEX `idx_user_role_user`(`user_id`) USING BTREE,
  INDEX `idx_user_role_role`(`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 244 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户与角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
