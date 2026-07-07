/*
 Navicat Premium Data Transfer

 Source Server         : docker
 Source Server Type    : MySQL
 Source Server Version : 80046
 Source Host           : localhost:3307
 Source Schema         : star_pivot_tms

 Target Server Type    : MySQL
 Target Server Version : 80046
 File Encoding         : 65001

 Date: 07/07/2026 19:14:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tms_carrier
-- ----------------------------
DROP TABLE IF EXISTS `tms_carrier`;
CREATE TABLE `tms_carrier`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `carrier_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '承运商编码',
  `carrier_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '承运商名称',
  `kuaidi100_com` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '快递100 com 编码',
  `sort_order` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '0正常 1停用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '0存在 2删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_carrier_code`(`carrier_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'TMS承运商' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tms_carrier
-- ----------------------------
INSERT INTO `tms_carrier` VALUES (1, 'SF', '顺丰速运', 'shunfeng', 1, '0', '快递100: shunfeng', '2026-07-05 18:08:23', '2026-07-05 18:08:23', '0');
INSERT INTO `tms_carrier` VALUES (2, 'YTO', '圆通速递', 'yuantong', 2, '0', '快递100: yuantong', '2026-07-05 18:08:23', '2026-07-05 18:08:23', '0');
INSERT INTO `tms_carrier` VALUES (3, 'ZTO', '中通快递', 'zhongtong', 3, '0', '快递100: zhongtong', '2026-07-05 18:08:23', '2026-07-05 18:08:23', '0');
INSERT INTO `tms_carrier` VALUES (4, 'YD', '韵达快递', 'yunda', 4, '0', '快递100: yunda', '2026-07-05 18:08:23', '2026-07-05 18:08:23', '0');
INSERT INTO `tms_carrier` VALUES (5, 'STO', '申通快递', 'shentong', 5, '0', '快递100: shentong', '2026-07-05 18:08:23', '2026-07-05 18:08:23', '0');
INSERT INTO `tms_carrier` VALUES (6, 'JD', '京东物流', 'jd', 6, '0', '快递100: jd', '2026-07-05 18:08:23', '2026-07-05 18:08:23', '0');
INSERT INTO `tms_carrier` VALUES (7, 'EMS', 'EMS', 'ems', 7, '0', '快递100: ems', '2026-07-05 18:08:23', '2026-07-05 18:08:23', '0');
INSERT INTO `tms_carrier` VALUES (8, 'HTKY', '百世快递', 'huitongkuaidi', 8, '0', '快递100: huitongkuaidi', '2026-07-05 18:08:23', '2026-07-05 18:08:23', '0');

-- ----------------------------
-- Table structure for tms_freight_rule
-- ----------------------------
DROP TABLE IF EXISTS `tms_freight_rule`;
CREATE TABLE `tms_freight_rule`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '规则名称',
  `rule_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'FIXED/WEIGHT',
  `default_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '1默认规则',
  `base_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '固定运费(FIXED)',
  `first_weight_kg` decimal(10, 3) NULL DEFAULT NULL COMMENT '首重kg(WEIGHT)',
  `first_fee` decimal(10, 2) NULL DEFAULT NULL COMMENT '首重费用(WEIGHT)',
  `continue_fee_per_kg` decimal(10, 2) NULL DEFAULT NULL COMMENT '续重单价/kg(WEIGHT)',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '0正常 1停用',
  `sort_order` int(0) NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '0存在 2删除',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'TMS运费规则' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tms_freight_rule
-- ----------------------------
INSERT INTO `tms_freight_rule` VALUES (1, '默认固定运费', 'FIXED', '1', 10.00, NULL, NULL, NULL, '0', 1, '与商城原 defaultFreight 一致', '2026-07-05 18:18:54', '2026-07-05 18:18:54', '0');
INSERT INTO `tms_freight_rule` VALUES (2, '按重量计费', 'WEIGHT', '0', NULL, 1.000, 8.00, 2.00, '0', 2, '首重1kg 8元，续重2元/kg', '2026-07-05 18:18:54', '2026-07-05 18:18:54', '0');

-- ----------------------------
-- Table structure for tms_shipment
-- ----------------------------
DROP TABLE IF EXISTS `tms_shipment`;
CREATE TABLE `tms_shipment`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shipment_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '运单业务号',
  `biz_module` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务域 mall',
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务类型 order',
  `biz_id` bigint(0) NOT NULL COMMENT '业务主键',
  `biz_key` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '业务唯一键',
  `order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联订单号',
  `carrier_id` bigint(0) NULL DEFAULT NULL COMMENT '承运商ID',
  `carrier_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '承运商名称',
  `kuaidi100_com` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '快递100编码',
  `tracking_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '物流单号',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SHIPPED' COMMENT 'SHIPPED/IN_TRANSIT/DELIVERED/EXCEPTION',
  `receiver_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收件人',
  `receiver_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收件电话',
  `receiver_address` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收件地址',
  `ship_time` datetime(0) NULL DEFAULT NULL COMMENT '发货时间',
  `deliver_time` datetime(0) NULL DEFAULT NULL COMMENT '签收时间',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `del_flag` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '0' COMMENT '0存在 2删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_shipment_sn`(`shipment_sn`) USING BTREE,
  INDEX `idx_biz`(`biz_module`, `biz_type`, `biz_id`) USING BTREE,
  INDEX `idx_tracking_no`(`tracking_no`) USING BTREE,
  INDEX `idx_order_sn`(`order_sn`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'TMS运单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tms_shipment
-- ----------------------------
INSERT INTO `tms_shipment` VALUES (1, 'TMS17832475431455693', 'mall', 'order', 8, 'mall:order:8', 'SP20260705183141479976', 1, '顺丰速运', 'shunfeng', 'SF5468411641535', 'SHIPPED', '张三', '15888888888', '北京市北京市西城区皇城国际', '2026-07-05 18:32:23', NULL, '2026-07-05 18:32:23', '2026-07-05 18:32:23', '0');
INSERT INTO `tms_shipment` VALUES (2, 'TMS17832481471476511', 'mall', 'order', 9, 'mall:order:9', 'SP20260705184207923311', 2, '圆通速递', 'yuantong', 'YT0052140042346', 'SHIPPED', '张三', '15888888888', '北京市北京市西城区皇城国际', '2026-07-05 18:42:27', NULL, '2026-07-05 18:42:27', '2026-07-05 18:42:27', '0');

-- ----------------------------
-- Table structure for tms_track_event
-- ----------------------------
DROP TABLE IF EXISTS `tms_track_event`;
CREATE TABLE `tms_track_event`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shipment_id` bigint(0) NOT NULL COMMENT '运单ID',
  `event_time` datetime(0) NOT NULL COMMENT '事件时间',
  `event_status` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态码',
  `event_desc` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '描述',
  `location` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地点',
  `source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'SYSTEM' COMMENT 'SYSTEM/KUAIDI100/MANUAL',
  `raw_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '原始报文',
  `create_time` datetime(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shipment_id`(`shipment_id`) USING BTREE,
  INDEX `idx_event_time`(`event_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'TMS物流轨迹' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tms_track_event
-- ----------------------------
INSERT INTO `tms_track_event` VALUES (1, 1, '2026-07-05 18:32:23', 'SHIPPED', '卖家已发货，等待承运商揽收', NULL, 'SYSTEM', NULL, '2026-07-05 18:32:23');
INSERT INTO `tms_track_event` VALUES (2, 2, '2026-07-05 18:42:27', 'SHIPPED', '卖家已发货，等待承运商揽收', NULL, 'SYSTEM', NULL, '2026-07-05 18:42:27');

SET FOREIGN_KEY_CHECKS = 1;
