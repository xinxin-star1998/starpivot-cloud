/*
 Navicat Premium Data Transfer

 Source Server         : docker
 Source Server Type    : MySQL
 Source Server Version : 80046
 Source Host           : localhost:3307
 Source Schema         : star_pivot_order

 Target Server Type    : MySQL
 Target Server Version : 80046
 File Encoding         : 65001

 Date: 07/07/2026 19:13:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for mq_message
-- ----------------------------
DROP TABLE IF EXISTS `mq_message`;
CREATE TABLE `mq_message`  (
  `message_id` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `to_exchange` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标交换机',
  `routing_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由键',
  `class_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '消息类型',
  `message_status` tinyint(0) NULL DEFAULT 0 COMMENT '0-新建 1-已发送 2-错误抵达 3-已抵达',
  `create_time` datetime(0) NULL DEFAULT NULL,
  `update_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`message_id`) USING BTREE,
  INDEX `idx_mq_status_time`(`message_status`, `create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'MQ本地消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of mq_message
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint(0) NULL DEFAULT NULL COMMENT '会员id',
  `order_sn` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单号',
  `coupon_id` bigint(0) NULL DEFAULT NULL COMMENT '使用的优惠券',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `member_username` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户名',
  `total_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '订单总额',
  `pay_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '应付总额',
  `freight_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '运费金额',
  `promotion_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '促销优化金额',
  `integration_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '积分抵扣金额',
  `coupon_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '优惠券抵扣金额',
  `discount_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '后台调整折扣金额',
  `pay_type` tinyint(0) NULL DEFAULT NULL COMMENT '1支付宝 2微信 3银联 4货到付款',
  `source_type` tinyint(0) NULL DEFAULT NULL COMMENT '0-PC 1-app',
  `status` tinyint(0) NULL DEFAULT NULL COMMENT '0待付款 1待发货 2已发货 3已完成 4已关闭 5无效',
  `delivery_company` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物流公司',
  `delivery_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '物流单号',
  `auto_confirm_day` int(0) NULL DEFAULT NULL COMMENT '自动确认收货天数',
  `integration` int(0) NULL DEFAULT NULL COMMENT '可获得积分',
  `growth` int(0) NULL DEFAULT NULL COMMENT '可获得成长值',
  `bill_type` tinyint(0) NULL DEFAULT NULL COMMENT '0不开发票 1电子 2纸质',
  `bill_header` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发票抬头',
  `bill_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发票内容',
  `bill_receiver_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收票人电话',
  `bill_receiver_email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收票人邮箱',
  `receiver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货人电话',
  `receiver_post_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货人邮编',
  `receiver_province` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省份',
  `receiver_city` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `receiver_region` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区',
  `receiver_detail_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单备注',
  `confirm_status` tinyint(0) NULL DEFAULT NULL COMMENT '0未确认 1已确认',
  `delete_status` tinyint(0) NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  `use_integration` int(0) NULL DEFAULT NULL COMMENT '下单使用积分',
  `payment_time` datetime(0) NULL DEFAULT NULL COMMENT '支付时间',
  `delivery_time` datetime(0) NULL DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime(0) NULL DEFAULT NULL COMMENT '确认收货时间',
  `comment_time` datetime(0) NULL DEFAULT NULL COMMENT '评价时间',
  `modify_time` datetime(0) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_sn`(`order_sn`) USING BTREE,
  INDEX `idx_member_id`(`member_id`) USING BTREE,
  INDEX `idx_status_create`(`status`, `create_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order
-- ----------------------------
INSERT INTO `oms_order` VALUES (1, 1, 'SP20260625122910520589', NULL, '2026-06-25 12:29:10', 'xinxin', 5999.0000, 5999.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '张三', '15888888888', NULL, '北京市', '北京市', '西城区', '皇城国际', NULL, 0, 0, NULL, '2026-06-25 12:29:13', NULL, NULL, NULL, '2026-06-25 12:29:13');
INSERT INTO `oms_order` VALUES (2, 1, 'SP20260625124203897265', NULL, '2026-06-25 12:42:03', 'xinxin', 5999.0000, 5999.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '张三', '15888888888', NULL, '北京市', '北京市', '西城区', '皇城国际', NULL, 0, 0, NULL, '2026-06-25 12:42:15', NULL, NULL, NULL, '2026-06-25 12:42:15');
INSERT INTO `oms_order` VALUES (3, 1, 'SP20260625124908785630', NULL, '2026-06-25 12:49:09', 'xinxin', 5999.0000, 5999.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 1, 0, 1, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '张三', '15888888888', NULL, '北京市', '北京市', '西城区', '皇城国际', NULL, 0, 0, NULL, '2026-06-25 12:49:12', NULL, NULL, NULL, '2026-06-25 12:49:12');
INSERT INTO `oms_order` VALUES (4, 1, 'SP20260625125530808180', NULL, '2026-06-25 12:55:31', 'xinxin', 5999.0000, 5999.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 1, 0, 3, '中通快递', 'ZT124548651302165', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '张三', '15888888888', NULL, '北京市', '北京市', '西城区', '皇城国际', NULL, 1, 0, NULL, '2026-06-25 12:55:32', '2026-07-02 21:24:13', '2026-07-02 21:32:28', NULL, '2026-07-02 21:32:28');
INSERT INTO `oms_order` VALUES (5, 1, 'SP20260625165025371495', NULL, '2026-06-25 16:50:25', 'xinxin', 19998.0000, 19998.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 1, 0, 3, '顺丰速递', 'SF2135464651654684946', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '张三', '15888888888', NULL, '北京市', '北京市', '西城区', '皇城国际', NULL, 1, 0, NULL, '2026-06-25 16:50:27', '2026-07-02 21:12:05', '2026-07-02 21:22:33', NULL, '2026-07-02 21:22:33');
INSERT INTO `oms_order` VALUES (6, 1, 'SP20260703224706552122', NULL, '2026-07-03 22:47:07', 'xinxin', 9999.0000, 10009.0000, 10.0000, 0.0000, 0.0000, 0.0000, 0.0000, 2, 0, 3, '顺丰速递', 'SF543546813213', NULL, 0, 0, NULL, NULL, NULL, NULL, NULL, '张三', '15888888888', NULL, '北京市', '北京市', '西城区', '皇城国际', NULL, 1, 0, 0, '2026-07-03 22:47:29', '2026-07-03 22:48:03', '2026-07-03 22:48:16', NULL, '2026-07-03 22:48:16');
INSERT INTO `oms_order` VALUES (7, 1, 'SP20260704135653714766', NULL, '2026-07-04 13:56:53', 'xinxin', 6999.0000, 7009.0000, 10.0000, 0.0000, 0.0000, 0.0000, 0.0000, 2, 0, 3, '顺丰速运', 'SF46546813213', NULL, 100, 100, NULL, NULL, NULL, NULL, NULL, '张三', '15888888888', NULL, '北京市', '北京市', '西城区', '皇城国际', NULL, 1, 0, 0, '2026-07-04 13:56:59', '2026-07-04 13:58:04', '2026-07-04 13:59:11', NULL, '2026-07-04 13:59:11');
INSERT INTO `oms_order` VALUES (8, 1, 'SP20260705183141479976', NULL, '2026-07-05 18:31:42', 'xinxin', 6999.0000, 6999.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 2, 0, 3, '顺丰速运', 'SF5468411641535', NULL, 100, 100, NULL, NULL, NULL, NULL, NULL, '张三', '15888888888', NULL, '北京市', '北京市', '西城区', '皇城国际', NULL, 1, 0, 0, '2026-07-05 18:31:46', '2026-07-05 18:32:23', '2026-07-05 18:33:29', NULL, '2026-07-05 18:33:29');
INSERT INTO `oms_order` VALUES (9, 1, 'SP20260705184207923311', NULL, '2026-07-05 18:42:07', 'xinxin', 6999.0000, 6999.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 2, 0, 3, '圆通速递', 'YT0052140042346', NULL, 100, 100, NULL, NULL, NULL, NULL, NULL, '张三', '15888888888', NULL, '北京市', '北京市', '西城区', '皇城国际', NULL, 1, 0, 0, '2026-07-05 18:42:10', '2026-07-05 18:42:27', '2026-07-05 18:42:50', NULL, '2026-07-05 18:42:50');

-- ----------------------------
-- Table structure for oms_order_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id` bigint(0) NULL DEFAULT NULL COMMENT '订单id',
  `order_sn` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单号',
  `spu_id` bigint(0) NULL DEFAULT NULL COMMENT 'spu_id',
  `spu_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'spu_name',
  `spu_pic` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'spu_pic',
  `spu_brand` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '品牌',
  `category_id` bigint(0) NULL DEFAULT NULL COMMENT '分类id',
  `sku_id` bigint(0) NULL DEFAULT NULL COMMENT 'sku_id',
  `sku_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'sku名称',
  `sku_pic` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'sku图片',
  `sku_price` decimal(18, 4) NULL DEFAULT NULL COMMENT 'sku价格',
  `sku_quantity` int(0) NULL DEFAULT NULL COMMENT '购买数量',
  `sku_attrs_vals` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '销售属性JSON',
  `promotion_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '促销分解金额',
  `coupon_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '优惠券分解金额',
  `integration_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '积分分解金额',
  `real_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '优惠后分解金额',
  `gift_integration` int(0) NULL DEFAULT NULL COMMENT '赠送积分',
  `gift_growth` int(0) NULL DEFAULT NULL COMMENT '赠送成长值',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_order_sn`(`order_sn`) USING BTREE,
  INDEX `idx_sku_id`(`sku_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单项信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_item
-- ----------------------------
INSERT INTO `oms_order_item` VALUES (1, 1, 'SP20260625122910520589', 13, ' Apple iPhone 11 (A2223) ', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 'Apple', 225, 40, ' Apple iPhone 11 (A2223)  黑色 128GB 移动联通电信4G手机 双卡双待 最后几件优惠', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 5999.0000, 1, '版本:128GB ;颜色:黑色', 0.0000, 0.0000, 0.0000, 5999.0000, NULL, NULL);
INSERT INTO `oms_order_item` VALUES (2, 2, 'SP20260625124203897265', 13, ' Apple iPhone 11 (A2223) ', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 'Apple', 225, 40, ' Apple iPhone 11 (A2223)  黑色 128GB 移动联通电信4G手机 双卡双待 最后几件优惠', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 5999.0000, 1, '版本:128GB ;颜色:黑色', 0.0000, 0.0000, 0.0000, 5999.0000, NULL, NULL);
INSERT INTO `oms_order_item` VALUES (3, 3, 'SP20260625124908785630', 13, ' Apple iPhone 11 (A2223) ', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 'Apple', 225, 40, ' Apple iPhone 11 (A2223)  黑色 128GB 移动联通电信4G手机 双卡双待 最后几件优惠', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 5999.0000, 1, '版本:128GB ;颜色:黑色', 0.0000, 0.0000, 0.0000, 5999.0000, NULL, NULL);
INSERT INTO `oms_order_item` VALUES (4, 4, 'SP20260625125530808180', 13, ' Apple iPhone 11 (A2223) ', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 'Apple', 225, 40, ' Apple iPhone 11 (A2223)  黑色 128GB 移动联通电信4G手机 双卡双待 最后几件优惠', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 5999.0000, 1, '版本:128GB ;颜色:黑色', 0.0000, 0.0000, 0.0000, 5999.0000, NULL, NULL);
INSERT INTO `oms_order_item` VALUES (5, 5, 'SP20260625165025371495', 13, ' Apple iPhone 11 (A2223) ', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 'Apple', 225, 85, ' Apple iPhone 11 (A2223)  粉色 6GB 128GB 标准版', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 9999.0000, 1, '机身颜色:粉色;运行内存(RAM):6GB;机身存储(ROM):128GB;机型版本:标准版', 0.0000, 0.0000, 0.0000, 9999.0000, NULL, NULL);
INSERT INTO `oms_order_item` VALUES (6, 5, 'SP20260625165025371495', 13, ' Apple iPhone 11 (A2223) ', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 'Apple', 225, 84, ' Apple iPhone 11 (A2223)  红色 6GB 128GB 标准版', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 9999.0000, 1, '机身颜色:红色;运行内存(RAM):6GB;机身存储(ROM):128GB;机型版本:标准版', 0.0000, 0.0000, 0.0000, 9999.0000, NULL, NULL);
INSERT INTO `oms_order_item` VALUES (7, 6, 'SP20260703224706552122', 13, ' Apple iPhone 11 (A2223) ', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 'Apple', 225, 99, ' Apple iPhone 11 (A2223)  红色 6GB 128GB 标准版', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 9999.0000, 1, '机身颜色:红色;运行内存(RAM):6GB;机身存储(ROM):128GB;机型版本:标准版', 0.0000, 0.0000, 0.0000, 9999.0000, NULL, NULL);
INSERT INTO `oms_order_item` VALUES (8, 7, 'SP20260704135653714766', 14, '小米promax', 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', '小米', 225, 102, '小米promax 黑色 12GB 512GB Ultra版', 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', 6999.0000, 1, '机身颜色:黑色;运行内存(RAM):12GB;机身存储(ROM):512GB;机型版本:Ultra版', 0.0000, 0.0000, 0.0000, 6999.0000, NULL, NULL);
INSERT INTO `oms_order_item` VALUES (9, 8, 'SP20260705183141479976', 14, '小米promax', 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', '小米', 225, 102, '小米promax 黑色 12GB 512GB Ultra版', 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', 6999.0000, 1, '机身颜色:黑色;运行内存(RAM):12GB;机身存储(ROM):512GB;机型版本:Ultra版', 0.0000, 0.0000, 0.0000, 6999.0000, NULL, NULL);
INSERT INTO `oms_order_item` VALUES (10, 9, 'SP20260705184207923311', 14, '小米promax', 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', '小米', 225, 102, '小米promax 黑色 12GB 512GB Ultra版', 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', 6999.0000, 1, '机身颜色:黑色;运行内存(RAM):12GB;机身存储(ROM):512GB;机型版本:Ultra版', 0.0000, 0.0000, 0.0000, 6999.0000, NULL, NULL);

-- ----------------------------
-- Table structure for oms_order_operate_history
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_operate_history`;
CREATE TABLE `oms_order_operate_history`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id` bigint(0) NULL DEFAULT NULL COMMENT '订单id',
  `operate_man` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作人',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '操作时间',
  `order_status` tinyint(0) NULL DEFAULT NULL COMMENT '订单状态',
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单操作历史记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_operate_history
-- ----------------------------
INSERT INTO `oms_order_operate_history` VALUES (1, 1, 'xinxin', '2026-06-25 12:29:11', 0, '提交订单');
INSERT INTO `oms_order_operate_history` VALUES (2, 1, 'xinxin', '2026-06-25 12:29:13', 1, 'Mock支付成功');
INSERT INTO `oms_order_operate_history` VALUES (3, 2, 'xinxin', '2026-06-25 12:42:04', 0, '提交订单');
INSERT INTO `oms_order_operate_history` VALUES (4, 2, 'xinxin', '2026-06-25 12:42:15', 1, '支付成功');
INSERT INTO `oms_order_operate_history` VALUES (5, 3, 'xinxin', '2026-06-25 12:49:09', 0, '提交订单');
INSERT INTO `oms_order_operate_history` VALUES (6, 3, 'xinxin', '2026-06-25 12:49:12', 1, '支付成功');
INSERT INTO `oms_order_operate_history` VALUES (7, 4, 'xinxin', '2026-06-25 12:55:31', 0, '提交订单');
INSERT INTO `oms_order_operate_history` VALUES (8, 4, 'xinxin', '2026-06-25 12:55:32', 1, '支付成功');
INSERT INTO `oms_order_operate_history` VALUES (9, 5, 'xinxin', '2026-06-25 16:50:25', 0, '提交订单');
INSERT INTO `oms_order_operate_history` VALUES (10, 5, 'xinxin', '2026-06-25 16:50:27', 1, '支付成功');
INSERT INTO `oms_order_operate_history` VALUES (11, 5, 'admin', '2026-07-02 21:12:05', 2, '订单发货');
INSERT INTO `oms_order_operate_history` VALUES (12, 5, 'xinxin', '2026-07-02 21:22:33', 3, '确认收货');
INSERT INTO `oms_order_operate_history` VALUES (13, 4, 'admin', '2026-07-02 21:24:13', 2, '订单发货');
INSERT INTO `oms_order_operate_history` VALUES (14, 4, 'xinxin', '2026-07-02 21:32:28', 3, '确认收货');
INSERT INTO `oms_order_operate_history` VALUES (15, 6, 'xinxin', '2026-07-03 22:47:07', 0, '提交订单');
INSERT INTO `oms_order_operate_history` VALUES (16, 6, 'xinxin', '2026-07-03 22:47:29', 1, '支付成功');
INSERT INTO `oms_order_operate_history` VALUES (17, 6, 'admin', '2026-07-03 22:48:03', 2, '订单发货');
INSERT INTO `oms_order_operate_history` VALUES (18, 6, 'xinxin', '2026-07-03 22:48:16', 3, '确认收货');
INSERT INTO `oms_order_operate_history` VALUES (19, 7, 'xinxin', '2026-07-04 13:56:54', 0, '提交订单');
INSERT INTO `oms_order_operate_history` VALUES (20, 7, 'xinxin', '2026-07-04 13:57:00', 1, '支付成功');
INSERT INTO `oms_order_operate_history` VALUES (21, 7, 'admin', '2026-07-04 13:58:04', 2, '订单发货');
INSERT INTO `oms_order_operate_history` VALUES (22, 7, 'xinxin', '2026-07-04 13:59:11', 3, '确认收货');
INSERT INTO `oms_order_operate_history` VALUES (23, 8, 'xinxin', '2026-07-05 18:31:42', 0, '提交订单');
INSERT INTO `oms_order_operate_history` VALUES (24, 8, 'xinxin', '2026-07-05 18:31:47', 1, '支付成功');
INSERT INTO `oms_order_operate_history` VALUES (25, 8, 'admin', '2026-07-05 18:32:23', 2, '订单发货');
INSERT INTO `oms_order_operate_history` VALUES (26, 8, 'xinxin', '2026-07-05 18:33:29', 3, '确认收货');
INSERT INTO `oms_order_operate_history` VALUES (27, 9, 'xinxin', '2026-07-05 18:42:07', 0, '提交订单');
INSERT INTO `oms_order_operate_history` VALUES (28, 9, 'xinxin', '2026-07-05 18:42:10', 1, '支付成功');
INSERT INTO `oms_order_operate_history` VALUES (29, 9, 'admin', '2026-07-05 18:42:27', 2, '订单发货');
INSERT INTO `oms_order_operate_history` VALUES (30, 9, 'xinxin', '2026-07-05 18:42:50', 3, '确认收货');

-- ----------------------------
-- Table structure for oms_order_return_apply
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_return_apply`;
CREATE TABLE `oms_order_return_apply`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id` bigint(0) NULL DEFAULT NULL COMMENT '订单id',
  `sku_id` bigint(0) NULL DEFAULT NULL COMMENT '退货sku_id',
  `order_sn` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单号',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '申请时间',
  `member_username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会员用户名',
  `return_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '退款金额',
  `return_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退货人姓名',
  `return_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退货人电话',
  `status` tinyint(0) NULL DEFAULT NULL COMMENT '0待处理 1退货中 2已完成 3已拒绝',
  `approval_instance_id` bigint(0) NULL DEFAULT NULL COMMENT '审批实例ID',
  `audit_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PENDING/APPROVED/REJECTED/WITHDRAWN',
  `handle_time` datetime(0) NULL DEFAULT NULL COMMENT '处理时间',
  `sku_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品图片',
  `sku_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品名称',
  `sku_brand` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '商品品牌',
  `sku_attrs_vals` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '销售属性JSON',
  `sku_count` int(0) NULL DEFAULT NULL COMMENT '退货数量',
  `sku_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '商品单价',
  `sku_real_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '实付单价',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '原因',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `desc_pics` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '凭证图片',
  `handle_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理备注',
  `handle_man` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '处理人员',
  `receive_man` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货人',
  `receive_time` datetime(0) NULL DEFAULT NULL COMMENT '收货时间',
  `receive_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货备注',
  `receive_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '收货电话',
  `company_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '公司收货地址',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE,
  INDEX `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单退货申请' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_return_apply
-- ----------------------------
INSERT INTO `oms_order_return_apply` VALUES (1, 4, 40, 'SP20260625125530808180', '2026-07-02 21:24:39', 'xinxin', 5999.0000, '张三', '15888888888', 0, 7, 'WITHDRAWN', '2026-07-02 21:27:04', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', ' Apple iPhone 11 (A2223)  黑色 128GB 移动联通电信4G手机 双卡双待 最后几件优惠', 'Apple', '版本:128GB ;颜色:黑色', 1, 5999.0000, 5999.0000, '买错了', NULL, NULL, '发起人撤回', NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for oms_order_return_reason
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_return_reason`;
CREATE TABLE `oms_order_return_reason`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退货原因',
  `sort` int(0) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '0禁用 1启用',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '退货原因' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_return_reason
-- ----------------------------
INSERT INTO `oms_order_return_reason` VALUES (1, '商品质量问题', 1, 1, '2026-06-23 16:10:58');
INSERT INTO `oms_order_return_reason` VALUES (2, '商品与描述不符', 2, 1, '2026-06-23 16:10:58');
INSERT INTO `oms_order_return_reason` VALUES (3, '收到商品破损', 3, 1, '2026-06-23 16:10:58');
INSERT INTO `oms_order_return_reason` VALUES (4, '拍错/多拍/不想要', 4, 1, '2026-06-23 16:10:58');
INSERT INTO `oms_order_return_reason` VALUES (5, '其他原因', 99, 1, '2026-06-23 16:10:58');

-- ----------------------------
-- Table structure for oms_order_setting
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_setting`;
CREATE TABLE `oms_order_setting`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `flash_order_overtime` int(0) NULL DEFAULT 30 COMMENT '秒杀订单超时(分)',
  `normal_order_overtime` int(0) NULL DEFAULT 30 COMMENT '正常订单超时(分)',
  `confirm_overtime` int(0) NULL DEFAULT 7 COMMENT '自动确认收货(天)',
  `finish_overtime` int(0) NULL DEFAULT 7 COMMENT '自动完成交易(天)',
  `comment_overtime` int(0) NULL DEFAULT 7 COMMENT '自动好评(天)',
  `member_level` tinyint(0) NULL DEFAULT 0 COMMENT '0不限等级',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单配置信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_setting
-- ----------------------------
INSERT INTO `oms_order_setting` VALUES (1, 15, 30, 7, 7, 7, 0);

-- ----------------------------
-- Table structure for oms_payment_info
-- ----------------------------
DROP TABLE IF EXISTS `oms_payment_info`;
CREATE TABLE `oms_payment_info`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_sn` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单号',
  `order_id` bigint(0) NULL DEFAULT NULL COMMENT '订单id',
  `alipay_trade_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付宝流水号',
  `total_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '支付总金额',
  `subject` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '交易内容',
  `payment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付状态',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `confirm_time` datetime(0) NULL DEFAULT NULL COMMENT '确认时间',
  `callback_content` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '回调内容',
  `callback_time` datetime(0) NULL DEFAULT NULL COMMENT '回调时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_payment_order_sn`(`order_sn`) USING BTREE,
  UNIQUE INDEX `uk_alipay_trade_no`(`alipay_trade_no`) USING BTREE,
  INDEX `idx_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '支付信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_payment_info
-- ----------------------------
INSERT INTO `oms_payment_info` VALUES (1, 'SP20260625122910520589', 1, 'MOCK1782361753082', 5999.0000, '商城订单-SP20260625122910520589', 'TRADE_SUCCESS', '2026-06-25 12:29:13', '2026-06-25 12:29:13', NULL, NULL);
INSERT INTO `oms_payment_info` VALUES (2, 'SP20260625124203897265', 2, 'MOCK1782362535273', 5999.0000, '商城订单-SP20260625124203897265', 'TRADE_SUCCESS', '2026-06-25 12:42:15', '2026-06-25 12:42:15', NULL, NULL);
INSERT INTO `oms_payment_info` VALUES (3, 'SP20260625124908785630', 3, 'MOCK1782362952165', 5999.0000, '商城订单-SP20260625124908785630', 'TRADE_SUCCESS', '2026-06-25 12:49:12', '2026-06-25 12:49:12', NULL, NULL);
INSERT INTO `oms_payment_info` VALUES (4, 'SP20260625125530808180', 4, 'MOCK1782363331854', 5999.0000, '商城订单-SP20260625125530808180', 'TRADE_SUCCESS', '2026-06-25 12:55:32', '2026-06-25 12:55:32', NULL, NULL);
INSERT INTO `oms_payment_info` VALUES (5, 'SP20260625165025371495', 5, 'MOCK1782377426717', 19998.0000, '商城订单-SP20260625165025371495', 'TRADE_SUCCESS', '2026-06-25 16:50:27', '2026-06-25 16:50:27', NULL, NULL);
INSERT INTO `oms_payment_info` VALUES (6, 'SP20260703224706552122', 6, 'WXMOCK1783090048748', 10009.0000, '商城订单-SP20260703224706552122', 'TRADE_SUCCESS', '2026-07-03 22:47:29', '2026-07-03 22:47:29', '{\"mock\":true,\"channel\":\"wechat\"}', '2026-07-03 22:47:29');
INSERT INTO `oms_payment_info` VALUES (7, 'SP20260704135653714766', 7, 'WXMOCK1783144619207', 7009.0000, '商城订单-SP20260704135653714766', 'TRADE_SUCCESS', '2026-07-04 13:56:59', '2026-07-04 13:56:59', '{\"mock\":true,\"channel\":\"wechat\"}', '2026-07-04 13:56:59');
INSERT INTO `oms_payment_info` VALUES (8, 'SP20260705183141479976', 8, 'WXMOCK1783247505851', 6999.0000, '商城订单-SP20260705183141479976', 'TRADE_SUCCESS', '2026-07-05 18:31:46', '2026-07-05 18:31:46', '{\"mock\":true,\"channel\":\"wechat\"}', '2026-07-05 18:31:46');
INSERT INTO `oms_payment_info` VALUES (9, 'SP20260705184207923311', 9, 'WXMOCK1783248129532', 6999.0000, '商城订单-SP20260705184207923311', 'TRADE_SUCCESS', '2026-07-05 18:42:10', '2026-07-05 18:42:10', '{\"mock\":true,\"channel\":\"wechat\"}', '2026-07-05 18:42:10');

-- ----------------------------
-- Table structure for oms_refund_info
-- ----------------------------
DROP TABLE IF EXISTS `oms_refund_info`;
CREATE TABLE `oms_refund_info`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_return_id` bigint(0) NULL DEFAULT NULL COMMENT '退货申请id',
  `refund` decimal(18, 4) NULL DEFAULT NULL COMMENT '退款金额',
  `refund_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退款流水号',
  `refund_status` tinyint(0) NULL DEFAULT NULL COMMENT '退款状态',
  `refund_channel` tinyint(0) NULL DEFAULT NULL COMMENT '1支付宝 2微信 3银联 4汇款',
  `refund_content` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '回调内容',
  `alert_ack` tinyint(0) NOT NULL DEFAULT 1 COMMENT '失败告警已读：0未读 1已读',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order_return_id`(`order_return_id`) USING BTREE,
  INDEX `idx_refund_sn`(`refund_sn`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '退款信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_refund_info
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
