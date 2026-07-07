/*
 Navicat Premium Data Transfer

 Source Server         : docker
 Source Server Type    : MySQL
 Source Server Version : 80046
 Source Host           : localhost:3307
 Source Schema         : star_pivot_member

 Target Server Type    : MySQL
 Target Server Version : 80046
 File Encoding         : 65001

 Date: 07/07/2026 19:13:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ums_growth_change_history
-- ----------------------------
DROP TABLE IF EXISTS `ums_growth_change_history`;
CREATE TABLE `ums_growth_change_history`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint(0) NULL DEFAULT NULL COMMENT '会员id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `change_count` int(0) NULL DEFAULT NULL COMMENT '变化值(正负)',
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `source_type` tinyint(0) NULL DEFAULT NULL COMMENT '0购物 1管理员修改',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_id`(`member_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '成长值变化历史' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_growth_change_history
-- ----------------------------
INSERT INTO `ums_growth_change_history` VALUES (1, 1, '2026-07-04 13:57:00', 100, '购物赠送成长值，订单号：SP20260704135653714766', 1);
INSERT INTO `ums_growth_change_history` VALUES (2, 1, '2026-07-05 18:31:46', 100, '购物赠送成长值，订单号：SP20260705183141479976', 1);
INSERT INTO `ums_growth_change_history` VALUES (3, 1, '2026-07-05 18:42:10', 100, '购物赠送成长值，订单号：SP20260705184207923311', 1);

-- ----------------------------
-- Table structure for ums_integration_change_history
-- ----------------------------
DROP TABLE IF EXISTS `ums_integration_change_history`;
CREATE TABLE `ums_integration_change_history`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint(0) NULL DEFAULT NULL COMMENT '会员id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `change_count` int(0) NULL DEFAULT NULL COMMENT '变化值',
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `source_type` tinyint(0) NULL DEFAULT NULL COMMENT '0购物 1管理员 2活动',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_id`(`member_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分变化历史' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_integration_change_history
-- ----------------------------
INSERT INTO `ums_integration_change_history` VALUES (1, 1, '2026-07-04 13:57:00', 100, '购物赠送积分，订单号：SP20260704135653714766', 3);
INSERT INTO `ums_integration_change_history` VALUES (2, 1, '2026-07-05 18:31:46', 100, '购物赠送积分，订单号：SP20260705183141479976', 3);
INSERT INTO `ums_integration_change_history` VALUES (3, 1, '2026-07-05 18:42:10', 100, '购物赠送积分，订单号：SP20260705184207923311', 3);

-- ----------------------------
-- Table structure for ums_member
-- ----------------------------
DROP TABLE IF EXISTS `ums_member`;
CREATE TABLE `ums_member`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `level_id` bigint(0) NULL DEFAULT NULL COMMENT '会员等级id',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '昵称',
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `header` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `gender` tinyint(0) NULL DEFAULT NULL COMMENT '性别',
  `birth` date NULL DEFAULT NULL COMMENT '生日',
  `city` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `job` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '职业',
  `sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个性签名',
  `source_type` tinyint(0) NULL DEFAULT NULL COMMENT '用户来源',
  `integration` int(0) NULL DEFAULT 0 COMMENT '积分',
  `growth` int(0) NULL DEFAULT 0 COMMENT '成长值',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '注册时间',
  `social_uid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '社交用户id',
  `access_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '访问令牌',
  `expires_in` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '令牌过期时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username`) USING BTREE,
  UNIQUE INDEX `uk_mobile`(`mobile`) USING BTREE,
  INDEX `idx_level_id`(`level_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member
-- ----------------------------
INSERT INTO `ums_member` VALUES (1, 1, 'xinxin', '$2a$10$hZddjzuDOBB1oQF8oqwvvuobQMdYphAF0.sYqdwZ3jRdncfv8kj0C', 'xinxin', 'xinxin', NULL, 'file/goods/7/2026/07/03/e408ad7f-4268-4b74-984b-d2b983eb20cc.webp', 1, NULL, NULL, NULL, '', 0, 300, 300, 1, '2026-06-24 12:46:01', NULL, NULL, NULL);
INSERT INTO `ums_member` VALUES (2, NULL, 'm_1124_03aa1f', '$2a$10$ogTiZ/YJ9KAxpN8k9o18Vu4WWdvcZU3Sa74iH7saumuFZlu3STCJS', '用户1124', '18834581124', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3, 0, 0, 1, '2026-06-30 19:27:14', NULL, NULL, NULL);
INSERT INTO `ums_member` VALUES (3, NULL, 'wx_88d098a7e3', '$2a$10$4GCjIwd4hPn9RuRi35fQ.erO9YGh9OmGyiVHaIMmVDSZeQHhx2GrG', '微信测试用户', NULL, NULL, '', NULL, NULL, NULL, NULL, NULL, 2, 0, 0, 1, '2026-06-30 19:59:26', 'mock_union_001', NULL, NULL);
INSERT INTO `ums_member` VALUES (4, NULL, 'wx_67d31d2385', '$2a$10$VH83q12714f8Kgw6RTmAEOoc9oY2/6NZkuvrTYY2y6U247Oz3AfwW', '小程序测试用户', NULL, NULL, '', NULL, NULL, NULL, NULL, NULL, 2, 0, 0, 1, '2026-07-02 11:35:02', 'mock_mini_union_001', NULL, NULL);
INSERT INTO `ums_member` VALUES (5, NULL, 'm_2878_b02688', '$2a$10$YoyXam1IL4XKaq6rQ38kW.zaz66NoizXMqwzL4xHHKlUYUzWVjygS', '用户2878', '18518712878', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 3, 0, 0, 1, '2026-07-02 17:59:24', NULL, NULL, NULL);

-- ----------------------------
-- Table structure for ums_member_auth
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_auth`;
CREATE TABLE `ums_member_auth`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `member_id` bigint(0) NOT NULL COMMENT '会员 ID，关联 ums_member.id',
  `auth_type` tinyint(0) NOT NULL COMMENT '登录方式：1密码 2手机 3微信 4QQ 5支付宝 6Apple 7邮箱',
  `identifier` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '凭证标识：username/mobile/unionid/openid 等',
  `credential` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码哈希，仅 auth_type=1 使用',
  `extra_json` json NULL COMMENT '扩展：unionid、openid、provider_app_id、昵称快照等',
  `bind_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '绑定时间',
  `last_login` datetime(0) NULL DEFAULT NULL COMMENT '该方式最近一次登录时间',
  `status` tinyint(0) NOT NULL DEFAULT 1 COMMENT '1正常 0已解绑',
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  `update_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_type_identifier`(`auth_type`, `identifier`) USING BTREE COMMENT '一种凭证全局唯一，不可绑多个会员',
  INDEX `idx_member_status`(`member_id`, `status`) USING BTREE,
  INDEX `idx_member_type`(`member_id`, `auth_type`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'C端会员登录方式绑定（一对多）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_auth
-- ----------------------------
INSERT INTO `ums_member_auth` VALUES (1, 2, 2, '18834581124', NULL, NULL, '2026-06-30 19:27:14', '2026-07-02 17:55:19', 1, '2026-06-30 19:27:14', '2026-07-02 17:55:19');
INSERT INTO `ums_member_auth` VALUES (2, 3, 3, 'mock_union_001', NULL, '{\"avatar\": \"\", \"openid\": \"mock_open_001\", \"unionid\": \"mock_union_001\", \"nickname\": \"微信测试用户\"}', '2026-06-30 19:59:26', '2026-07-02 21:13:32', 1, '2026-06-30 19:59:26', '2026-07-02 21:13:32');
INSERT INTO `ums_member_auth` VALUES (3, 4, 3, 'mock_mini_union_001', NULL, '{\"avatar\": \"\", \"openid\": \"mock_mini_open_001\", \"unionid\": \"mock_mini_union_001\", \"nickname\": \"小程序测试用户\"}', '2026-07-02 11:35:02', '2026-07-07 18:04:13', 1, '2026-07-02 11:35:02', '2026-07-07 18:04:13');
INSERT INTO `ums_member_auth` VALUES (4, 5, 2, '18518712878', NULL, NULL, '2026-07-02 17:59:24', NULL, 1, '2026-07-02 17:59:24', '2026-07-02 17:59:24');

-- ----------------------------
-- Table structure for ums_member_collect_spu
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_collect_spu`;
CREATE TABLE `ums_member_collect_spu`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint(0) NULL DEFAULT NULL COMMENT '会员id',
  `spu_id` bigint(0) NULL DEFAULT NULL COMMENT 'spu_id',
  `spu_name` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'spu名称',
  `spu_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'spu图片',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_member_spu`(`member_id`, `spu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员收藏商品' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_collect_spu
-- ----------------------------
INSERT INTO `ums_member_collect_spu` VALUES (1, 4, 13, ' Apple iPhone 11 (A2223) ', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', '2026-07-02 19:14:32');
INSERT INTO `ums_member_collect_spu` VALUES (2, 1, 13, ' Apple iPhone 11 (A2223) ', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', '2026-07-02 21:32:42');
INSERT INTO `ums_member_collect_spu` VALUES (3, 1, 11, '华为 HUAWEI Mate 30 Pro', 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', '2026-07-03 22:22:25');

-- ----------------------------
-- Table structure for ums_member_collect_subject
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_collect_subject`;
CREATE TABLE `ums_member_collect_subject`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint(0) NULL DEFAULT NULL COMMENT '会员id',
  `subject_id` bigint(0) NULL DEFAULT NULL COMMENT '专题id',
  `subject_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专题名称',
  `subject_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专题图片',
  `subject_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动链接',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_id`(`member_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员收藏专题' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_collect_subject
-- ----------------------------

-- ----------------------------
-- Table structure for ums_member_level
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_level`;
CREATE TABLE `ums_member_level`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '等级名称',
  `growth_point` int(0) NULL DEFAULT NULL COMMENT '所需成长值',
  `default_status` tinyint(0) NULL DEFAULT 0 COMMENT '是否默认等级',
  `free_freight_point` decimal(18, 4) NULL DEFAULT NULL COMMENT '免运费标准',
  `comment_growth_point` int(0) NULL DEFAULT NULL COMMENT '评价成长值',
  `privilege_free_freight` tinyint(0) NULL DEFAULT 0 COMMENT '免邮特权',
  `privilege_member_price` tinyint(0) NULL DEFAULT 0 COMMENT '会员价特权',
  `privilege_birthday` tinyint(0) NULL DEFAULT 0 COMMENT '生日特权',
  `note` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员等级' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_level
-- ----------------------------
INSERT INTO `ums_member_level` VALUES (1, '普通会员', 0, 1, 99.0000, 10, 0, 0, 0, '默认等级');
INSERT INTO `ums_member_level` VALUES (2, '银卡会员', 1000, 0, 79.0000, 20, 0, 1, 0, NULL);
INSERT INTO `ums_member_level` VALUES (3, '金卡会员', 5000, 0, 49.0000, 30, 1, 1, 1, NULL);
INSERT INTO `ums_member_level` VALUES (4, '钻石会员', 20000, 0, 0.0000, 50, 1, 1, 1, NULL);

-- ----------------------------
-- Table structure for ums_member_login_log
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_login_log`;
CREATE TABLE `ums_member_login_log`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint(0) NULL DEFAULT NULL COMMENT '会员id',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '登录时间',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `login_type` tinyint(0) NULL DEFAULT NULL COMMENT '1-web 2-app',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_id`(`member_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 33 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员登录记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_login_log
-- ----------------------------
INSERT INTO `ums_member_login_log` VALUES (1, 2, '2026-06-30 19:27:14', '0:0:0:0:0:0:0:1', NULL, 10);
INSERT INTO `ums_member_login_log` VALUES (2, 3, '2026-06-30 19:59:27', '0:0:0:0:0:0:0:1', NULL, 10);
INSERT INTO `ums_member_login_log` VALUES (3, 1, '2026-07-01 16:40:37', '0:0:0:0:0:0:0:1', NULL, 1);
INSERT INTO `ums_member_login_log` VALUES (4, 1, '2026-07-01 16:49:42', '0:0:0:0:0:0:0:1', NULL, 1);
INSERT INTO `ums_member_login_log` VALUES (5, 3, '2026-07-01 18:07:36', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (6, 4, '2026-07-02 11:35:02', '0:0:0:0:0:0:0:1', NULL, 10);
INSERT INTO `ums_member_login_log` VALUES (7, 4, '2026-07-02 11:35:40', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (8, 4, '2026-07-02 11:37:33', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (9, 4, '2026-07-02 12:32:01', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (10, 4, '2026-07-02 12:43:57', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (11, 4, '2026-07-02 13:11:28', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (12, 2, '2026-07-02 17:49:33', '0:0:0:0:0:0:0:1', NULL, 2);
INSERT INTO `ums_member_login_log` VALUES (13, 2, '2026-07-02 17:55:19', '0:0:0:0:0:0:0:1', NULL, 2);
INSERT INTO `ums_member_login_log` VALUES (14, 5, '2026-07-02 17:59:24', '0:0:0:0:0:0:0:1', NULL, 10);
INSERT INTO `ums_member_login_log` VALUES (15, 4, '2026-07-02 18:36:15', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (16, 4, '2026-07-02 18:57:25', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (17, 4, '2026-07-02 19:25:44', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (18, 3, '2026-07-02 21:13:32', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (19, 1, '2026-07-02 21:21:41', '0:0:0:0:0:0:0:1', NULL, 1);
INSERT INTO `ums_member_login_log` VALUES (20, 1, '2026-07-03 20:26:41', '0:0:0:0:0:0:0:1', NULL, 1);
INSERT INTO `ums_member_login_log` VALUES (21, 1, '2026-07-03 22:45:40', '0:0:0:0:0:0:0:1', NULL, 1);
INSERT INTO `ums_member_login_log` VALUES (22, 1, '2026-07-04 13:56:46', '0:0:0:0:0:0:0:1', NULL, 1);
INSERT INTO `ums_member_login_log` VALUES (23, 1, '2026-07-05 11:58:31', '0:0:0:0:0:0:0:1', NULL, 1);
INSERT INTO `ums_member_login_log` VALUES (24, 1, '2026-07-05 18:31:09', '0:0:0:0:0:0:0:1', NULL, 1);
INSERT INTO `ums_member_login_log` VALUES (25, 1, '2026-07-06 17:42:04', '0:0:0:0:0:0:0:1', NULL, 1);
INSERT INTO `ums_member_login_log` VALUES (26, 4, '2026-07-07 17:53:23', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (27, 4, '2026-07-07 17:56:27', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (28, 4, '2026-07-07 18:02:00', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (29, 4, '2026-07-07 18:02:08', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (30, 4, '2026-07-07 18:02:25', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (31, 4, '2026-07-07 18:02:33', '0:0:0:0:0:0:0:1', NULL, 3);
INSERT INTO `ums_member_login_log` VALUES (32, 4, '2026-07-07 18:04:13', '0:0:0:0:0:0:0:1', NULL, 3);

-- ----------------------------
-- Table structure for ums_member_receive_address
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_receive_address`;
CREATE TABLE `ums_member_receive_address`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint(0) NULL DEFAULT NULL COMMENT '会员id',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货人',
  `phone` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话',
  `post_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮编',
  `province` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省',
  `city` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '市',
  `region` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区',
  `detail_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `areacode` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区域编码',
  `default_status` tinyint(0) NULL DEFAULT 0 COMMENT '是否默认',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_id`(`member_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员收货地址' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_receive_address
-- ----------------------------
INSERT INTO `ums_member_receive_address` VALUES (1, 1, '张三', '15888888888', NULL, '北京市', '北京市', '西城区', '皇城国际', NULL, 1);

-- ----------------------------
-- Table structure for ums_member_sms_log
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_sms_log`;
CREATE TABLE `ums_member_sms_log`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `mobile` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '手机号',
  `scene` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'login/bind/unbind/set_password',
  `provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'aliyun/tencent/mock',
  `send_status` tinyint(0) NOT NULL DEFAULT 0 COMMENT '0失败 1成功',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_mobile_time`(`mobile`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'C端短信发送流水' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_sms_log
-- ----------------------------
INSERT INTO `ums_member_sms_log` VALUES (1, '18834581124', 'login', 'mock', 1, '0:0:0:0:0:0:0:1', '2026-06-30 19:27:12');
INSERT INTO `ums_member_sms_log` VALUES (2, '18834581124', 'login', 'mock', 1, '0:0:0:0:0:0:0:1', '2026-07-02 17:49:05');
INSERT INTO `ums_member_sms_log` VALUES (3, '18834581124', 'login', 'mock', 1, '0:0:0:0:0:0:0:1', '2026-07-02 17:50:21');
INSERT INTO `ums_member_sms_log` VALUES (4, '18834581124', 'login', 'mock', 1, '0:0:0:0:0:0:0:1', '2026-07-02 17:52:28');
INSERT INTO `ums_member_sms_log` VALUES (5, '18834581124', 'login', 'aliyun', 1, '0:0:0:0:0:0:0:1', '2026-07-02 17:55:10');
INSERT INTO `ums_member_sms_log` VALUES (6, '18518712878', 'register', 'aliyun', 1, '0:0:0:0:0:0:0:1', '2026-07-02 17:59:13');

-- ----------------------------
-- Table structure for ums_member_statistics_info
-- ----------------------------
DROP TABLE IF EXISTS `ums_member_statistics_info`;
CREATE TABLE `ums_member_statistics_info`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint(0) NULL DEFAULT NULL COMMENT '会员id',
  `consume_amount` decimal(18, 4) NULL COMMENT '累计消费',
  `coupon_amount` decimal(18, 4) NULL COMMENT '累计优惠',
  `order_count` int(0) NULL DEFAULT 0 COMMENT '订单数',
  `coupon_count` int(0) NULL DEFAULT 0 COMMENT '优惠券数',
  `comment_count` int(0) NULL DEFAULT 0 COMMENT '评价数',
  `return_order_count` int(0) NULL DEFAULT 0 COMMENT '退货数',
  `login_count` int(0) NULL DEFAULT 0 COMMENT '登录次数',
  `attend_count` int(0) NULL DEFAULT 0 COMMENT '关注数',
  `fans_count` int(0) NULL DEFAULT 0 COMMENT '粉丝数',
  `collect_product_count` int(0) NULL DEFAULT 0 COMMENT '收藏商品数',
  `collect_subject_count` int(0) NULL DEFAULT 0 COMMENT '收藏专题数',
  `collect_comment_count` int(0) NULL DEFAULT 0 COMMENT '收藏评论数',
  `invite_friend_count` int(0) NULL DEFAULT 0 COMMENT '邀请好友数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_member_id`(`member_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员统计信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of ums_member_statistics_info
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
