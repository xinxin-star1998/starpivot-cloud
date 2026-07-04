/*
 Navicat Premium Data Transfer

 Source Server         : docker
 Source Server Type    : MySQL
 Source Server Version : 80046
 Source Host           : localhost:3307
 Source Schema         : star_pivot_approval

 Target Server Type    : MySQL
 Target Server Version : 80046
 File Encoding         : 65001

 Date: 04/07/2026 20:00:00
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
USE `star_pivot_approval`;

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
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批实例' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ap_instance
-- ----------------------------
INSERT INTO `ap_instance` VALUES (1, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:2', '采购单审批 #2', 1, 'WITHDRAWN', NULL, '{\"amount\": 629900.0, \"wareId\": 3}', DEFAULT, '2026-06-26 11:02:32', '2026-06-26 11:09:26');
INSERT INTO `ap_instance` VALUES (2, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:2', '采购单审批 #2', 1, 'WITHDRAWN', NULL, '{\"amount\": 629900.0, \"wareId\": 3}', DEFAULT, '2026-06-26 11:09:42', '2026-06-26 11:10:04');
INSERT INTO `ap_instance` VALUES (3, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:2', '采购单审批 #2', 1, 'WITHDRAWN', NULL, '{\"amount\": 629900.0, \"wareId\": 3}', DEFAULT, '2026-06-26 11:10:21', '2026-06-26 11:13:08');
INSERT INTO `ap_instance` VALUES (4, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:2', '采购单审批 #2', 1, 'APPROVED', 2, '{\"amount\": 629900.0, \"wareId\": 3}', DEFAULT, '2026-06-26 11:13:17', '2026-06-26 11:15:40');
INSERT INTO `ap_instance` VALUES (5, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:3', '采购单审批 #3', 1, 'APPROVED', 2, '{\"amount\": 8888.0, \"wareId\": 1}', DEFAULT, '2026-06-26 14:39:53', '2026-06-26 14:42:45');
INSERT INTO `ap_instance` VALUES (6, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:3', '采购单审批 #3', 1, 'APPROVED', 2, '{\"amount\": 8888.0, \"wareId\": 1}', DEFAULT, '2026-06-26 14:56:01', '2026-06-26 14:56:03');
INSERT INTO `ap_instance` VALUES (7, 2, 'mall_return_default', 'mall', 'return', 'mall:return:1', '退货审批 #1 · SP20260625125530808180', 1, 'WITHDRAWN', 3, '{\"skuId\": 40, \"orderSn\": \"SP20260625125530808180\", \"skuName\": \" Apple iPhone 11 (A2223)  黑色 128GB 移动联通电信4G手机 双卡双待 最后几件优惠\", \"returnAmount\": 5999.0}', DEFAULT, '2026-07-02 21:24:55', '2026-07-02 21:27:04');
INSERT INTO `ap_instance` VALUES (8, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:4', '采购单审批 #4', 1, 'APPROVED', 2, '{\"amount\": 999900.0, \"wareId\": 2}', DEFAULT, '2026-07-03 22:29:58', '2026-07-03 22:30:18');
INSERT INTO `ap_instance` VALUES (9, 4, 'mall_spu_default', 'mall', 'spu', 'mall:spu:14', '商品上架审批 #14 · 小米promax', 1, 'REJECTED', 7, '{\"brandId\": 10, \"catalogId\": 225}', DEFAULT, '2026-07-04 13:34:55', '2026-07-04 13:35:38');
INSERT INTO `ap_instance` VALUES (10, 1, 'mall_purchase_default', 'mall', 'purchase', 'mall:purchase:5', '采购单审批 #5', 1, 'APPROVED', 2, '{\"amount\": 699900.0, \"wareId\": 1}', DEFAULT, '2026-07-04 13:39:31', '2026-07-04 13:39:52');

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
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批站内通知' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ap_notification
-- ----------------------------
INSERT INTO `ap_notification` VALUES (1, 1, 'TASK_ASSIGNED', '待办审批：退货审批 #1 · SP20260625125530808180', '步骤「部门负责人」待您处理', 7, NULL, '1', '2026-07-02 21:24:55');
INSERT INTO `ap_notification` VALUES (2, 1, 'INSTANCE_FINISHED', '审批完结：退货审批 #1 · SP20260625125530808180', '您的申请已撤回', 7, NULL, '1', '2026-07-02 21:27:04');
INSERT INTO `ap_notification` VALUES (3, 1, 'TASK_ASSIGNED', '待办审批：采购单审批 #4', '步骤「部门负责人」待您处理', 8, NULL, '1', '2026-07-03 22:29:59');
INSERT INTO `ap_notification` VALUES (4, 1, 'TASK_ASSIGNED', '待办审批：采购单审批 #4', '步骤「财务审批」待您处理', 8, NULL, '1', '2026-07-03 22:30:13');
INSERT INTO `ap_notification` VALUES (5, 1, 'INSTANCE_FINISHED', '审批完结：采购单审批 #4', '您的申请已通过', 8, NULL, '1', '2026-07-03 22:30:18');
INSERT INTO `ap_notification` VALUES (6, 1, 'TASK_ASSIGNED', '待办审批：商品上架审批 #14 · 小米promax', '步骤「品类负责人」待您处理', 9, NULL, '1', '2026-07-04 13:34:55');
INSERT INTO `ap_notification` VALUES (7, 1, 'INSTANCE_FINISHED', '审批完结：商品上架审批 #14 · 小米promax', '您的申请已驳回', 9, NULL, '1', '2026-07-04 13:35:38');
INSERT INTO `ap_notification` VALUES (8, 1, 'TASK_ASSIGNED', '待办审批：采购单审批 #5', '步骤「部门负责人」待您处理', 10, NULL, '1', '2026-07-04 13:39:31');
INSERT INTO `ap_notification` VALUES (9, 1, 'TASK_ASSIGNED', '待办审批：采购单审批 #5', '步骤「财务审批」待您处理', 10, NULL, '1', '2026-07-04 13:39:41');
INSERT INTO `ap_notification` VALUES (10, 1, 'INSTANCE_FINISHED', '审批完结：采购单审批 #5', '您的申请已通过', 10, NULL, '1', '2026-07-04 13:39:52');

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
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批记录' ROW_FORMAT = Dynamic;

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
INSERT INTO `ap_record` VALUES (16, 7, NULL, 'START', '发起', 1, 'SUBMIT', '发起审批', '2026-07-02 21:24:55');
INSERT INTO `ap_record` VALUES (17, 7, 7, 'dept_leader', '部门负责人', 1, 'APPROVE', '', '2026-07-02 21:25:18');
INSERT INTO `ap_record` VALUES (18, 7, NULL, 'WITHDRAW', '撤回', 1, 'WITHDRAW', '发起人撤回', '2026-07-02 21:27:04');
INSERT INTO `ap_record` VALUES (19, 8, NULL, 'START', '发起', 1, 'SUBMIT', '发起审批', '2026-07-03 22:29:58');
INSERT INTO `ap_record` VALUES (20, 8, 8, 'dept_leader', '部门负责人', 1, 'APPROVE', '', '2026-07-03 22:30:13');
INSERT INTO `ap_record` VALUES (21, 8, 9, 'finance', '财务审批', 1, 'APPROVE', '', '2026-07-03 22:30:18');
INSERT INTO `ap_record` VALUES (22, 9, NULL, 'START', '发起', 1, 'SUBMIT', '发起审批', '2026-07-04 13:34:55');
INSERT INTO `ap_record` VALUES (23, 9, 10, 'category_leader', '品类负责人', 1, 'REJECT', '暂时不用', '2026-07-04 13:35:38');
INSERT INTO `ap_record` VALUES (24, 10, NULL, 'START', '发起', 1, 'SUBMIT', '发起审批', '2026-07-04 13:39:31');
INSERT INTO `ap_record` VALUES (25, 10, 11, 'dept_leader', '部门负责人', 1, 'APPROVE', '', '2026-07-04 13:39:41');
INSERT INTO `ap_record` VALUES (26, 10, 12, 'finance', '财务审批', 1, 'APPROVE', '', '2026-07-04 13:39:52');

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
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批待办' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ap_task
-- ----------------------------
INSERT INTO `ap_task` VALUES (1, 4, 1, 'dept_leader', '部门负责人', 1, 'DONE', 'APPROVE', '通过', '2026-06-26 11:13:17', NULL, '2026-06-26 11:13:58');
INSERT INTO `ap_task` VALUES (2, 4, 2, 'finance', '财务审批', 1, 'DONE', 'APPROVE', '通过', '2026-06-26 11:13:58', NULL, '2026-06-26 11:15:40');
INSERT INTO `ap_task` VALUES (3, 5, 1, 'dept_leader', '部门负责人', 1, 'DONE', 'APPROVE', 'E2E pass round 1', '2026-06-26 14:39:53', NULL, '2026-06-26 14:42:44');
INSERT INTO `ap_task` VALUES (4, 5, 2, 'finance', '财务审批', 1, 'DONE', 'APPROVE', 'E2E pass round 2', '2026-06-26 14:42:44', NULL, '2026-06-26 14:42:45');
INSERT INTO `ap_task` VALUES (5, 6, 1, 'dept_leader', '部门负责人', 1, 'DONE', 'APPROVE', 'e2e-approval.ps1', '2026-06-26 14:56:01', NULL, '2026-06-26 14:56:03');
INSERT INTO `ap_task` VALUES (6, 6, 2, 'finance', '财务审批', 1, 'DONE', 'APPROVE', 'e2e-approval.ps1', '2026-06-26 14:56:03', NULL, '2026-06-26 14:56:03');
INSERT INTO `ap_task` VALUES (7, 7, 3, 'dept_leader', '部门负责人', 1, 'DONE', 'APPROVE', '', '2026-07-02 21:24:55', NULL, '2026-07-02 21:25:18');
INSERT INTO `ap_task` VALUES (8, 8, 1, 'dept_leader', '部门负责人', 1, 'DONE', 'APPROVE', '', '2026-07-03 22:29:59', NULL, '2026-07-03 22:30:13');
INSERT INTO `ap_task` VALUES (9, 8, 2, 'finance', '财务审批', 1, 'DONE', 'APPROVE', '', '2026-07-03 22:30:13', NULL, '2026-07-03 22:30:18');
INSERT INTO `ap_task` VALUES (10, 9, 7, 'category_leader', '品类负责人', 1, 'DONE', 'REJECT', '暂时不用', '2026-07-04 13:34:55', NULL, '2026-07-04 13:35:38');
INSERT INTO `ap_task` VALUES (11, 10, 1, 'dept_leader', '部门负责人', 1, 'DONE', 'APPROVE', '', '2026-07-04 13:39:31', NULL, '2026-07-04 13:39:41');
INSERT INTO `ap_task` VALUES (12, 10, 2, 'finance', '财务审批', 1, 'DONE', 'APPROVE', '', '2026-07-04 13:39:41', NULL, '2026-07-04 13:39:51');

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批模板' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批模板业务绑定' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批步骤路由' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批模板步骤' ROW_FORMAT = Dynamic;

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

SET FOREIGN_KEY_CHECKS = 1;
