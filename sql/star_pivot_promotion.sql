/*
 Navicat Premium Data Transfer

 Source Server         : docker
 Source Server Type    : MySQL
 Source Server Version : 80046
 Source Host           : localhost:3307
 Source Schema         : star_pivot_promotion

 Target Server Type    : MySQL
 Target Server Version : 80046
 File Encoding         : 65001

 Date: 07/07/2026 19:13:54
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sms_coupon
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon`;
CREATE TABLE `sms_coupon`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `coupon_type` tinyint(0) NULL DEFAULT NULL COMMENT '0全场 1会员 2购物 3注册',
  `coupon_img` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '优惠券图片',
  `coupon_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '优惠券名称',
  `num` int(0) NULL DEFAULT NULL COMMENT '数量',
  `amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '金额',
  `per_limit` int(0) NULL DEFAULT NULL COMMENT '每人限领',
  `min_point` decimal(18, 4) NULL DEFAULT NULL COMMENT '使用门槛',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '使用开始',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '使用结束',
  `use_type` tinyint(0) NULL DEFAULT NULL COMMENT '0全场 1分类 2商品',
  `note` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `publish_count` int(0) NULL DEFAULT 0 COMMENT '发行数量',
  `use_count` int(0) NULL DEFAULT 0 COMMENT '已使用',
  `receive_count` int(0) NULL DEFAULT 0 COMMENT '已领取',
  `enable_start_time` datetime(0) NULL DEFAULT NULL COMMENT '领取开始',
  `enable_end_time` datetime(0) NULL DEFAULT NULL COMMENT '领取结束',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '优惠码',
  `member_level` tinyint(0) NULL DEFAULT 0 COMMENT '0不限等级',
  `publish` tinyint(0) NULL DEFAULT 0 COMMENT '0未发布 1已发布',
  `approval_instance_id` bigint(0) NULL DEFAULT NULL COMMENT '审批实例ID',
  `audit_status` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PENDING/APPROVED/REJECTED/WITHDRAWN',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_publish_time`(`publish`, `enable_start_time`, `enable_end_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_coupon
-- ----------------------------
INSERT INTO `sms_coupon` VALUES (1, 0, NULL, 'Apple iPhone 11', NULL, 1000.0000, 1, 5999.0000, '2026-06-26 10:00:00', '2026-06-26 12:00:00', 2, NULL, 100, 0, 1, '2026-06-25 14:25:00', '2026-06-25 23:00:00', NULL, 0, 1, NULL, 'APPROVED');

-- ----------------------------
-- Table structure for sms_coupon_history
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon_history`;
CREATE TABLE `sms_coupon_history`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `coupon_id` bigint(0) NULL DEFAULT NULL COMMENT '优惠券id',
  `member_id` bigint(0) NULL DEFAULT NULL COMMENT '会员id',
  `member_nick_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会员昵称',
  `get_type` tinyint(0) NULL DEFAULT NULL COMMENT '0后台赠送 1主动领取',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '领取时间',
  `use_type` tinyint(0) NULL DEFAULT 0 COMMENT '0未使用 1已使用 2已过期',
  `use_time` datetime(0) NULL DEFAULT NULL COMMENT '使用时间',
  `order_id` bigint(0) NULL DEFAULT NULL COMMENT '订单id',
  `order_sn` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单号',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_id`(`member_id`) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券领取历史' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_coupon_history
-- ----------------------------
INSERT INTO `sms_coupon_history` VALUES (1, 1, 1, 'xinxin', 0, '2026-06-25 14:28:46', 0, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sms_coupon_spu_category_relation
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon_spu_category_relation`;
CREATE TABLE `sms_coupon_spu_category_relation`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `coupon_id` bigint(0) NULL DEFAULT NULL COMMENT '优惠券id',
  `category_id` bigint(0) NULL DEFAULT NULL COMMENT '分类id',
  `category_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类名称',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券分类关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_coupon_spu_category_relation
-- ----------------------------

-- ----------------------------
-- Table structure for sms_coupon_spu_relation
-- ----------------------------
DROP TABLE IF EXISTS `sms_coupon_spu_relation`;
CREATE TABLE `sms_coupon_spu_relation`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `coupon_id` bigint(0) NULL DEFAULT NULL COMMENT '优惠券id',
  `spu_id` bigint(0) NULL DEFAULT NULL COMMENT 'spu_id',
  `spu_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'spu名称',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id`) USING BTREE,
  INDEX `idx_spu_id`(`spu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券商品关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_coupon_spu_relation
-- ----------------------------
INSERT INTO `sms_coupon_spu_relation` VALUES (2, 1, 13, ' Apple iPhone 11 (A2223) ');

-- ----------------------------
-- Table structure for sms_home_adv
-- ----------------------------
DROP TABLE IF EXISTS `sms_home_adv`;
CREATE TABLE `sms_home_adv`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `pic` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '结束时间',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '0下线 1上线',
  `click_count` int(0) NULL DEFAULT 0 COMMENT '点击数',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '跳转地址',
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `sort` int(0) NULL DEFAULT 0 COMMENT '排序',
  `publisher_id` bigint(0) NULL DEFAULT NULL COMMENT '发布者',
  `auth_id` bigint(0) NULL DEFAULT NULL COMMENT '审核者',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_status_sort`(`status`, `sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '首页轮播广告' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_home_adv
-- ----------------------------
INSERT INTO `sms_home_adv` VALUES (1, '小米17 pro max 限时降价1000', 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg', '2026-07-05 12:00:00', '2026-07-05 14:00:00', 1, 0, '', '', 1, NULL, NULL);

-- ----------------------------
-- Table structure for sms_home_category_hot
-- ----------------------------
DROP TABLE IF EXISTS `sms_home_category_hot`;
CREATE TABLE `sms_home_category_hot`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `cat_id` bigint(0) NOT NULL COMMENT '关联分类ID（pms_category.cat_id）',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '展示标题（空则取分类名）',
  `icon` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '展示图标',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '跳转链接（空则按分类搜索页）',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '0下线 1上线',
  `sort` int(0) NULL DEFAULT 0 COMMENT '排序',
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cat_id`(`cat_id`) USING BTREE,
  INDEX `idx_status_sort`(`status`, `sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '首页分类热门' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_home_category_hot
-- ----------------------------

-- ----------------------------
-- Table structure for sms_home_subject
-- ----------------------------
DROP TABLE IF EXISTS `sms_home_subject`;
CREATE TABLE `sms_home_subject`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专题名',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '标题',
  `sub_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '副标题',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '显示状态',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详情链接',
  `sort` int(0) NULL DEFAULT 0 COMMENT '排序',
  `img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专题图片',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '首页专题' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_home_subject
-- ----------------------------
INSERT INTO `sms_home_subject` VALUES (1, '家电焕新', '家电焕新季', '爆款直降 · 品质生活', 1, '/portal', 1, '');

-- ----------------------------
-- Table structure for sms_home_subject_spu
-- ----------------------------
DROP TABLE IF EXISTS `sms_home_subject_spu`;
CREATE TABLE `sms_home_subject_spu`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '专题名',
  `subject_id` bigint(0) NULL DEFAULT NULL COMMENT '专题id',
  `spu_id` bigint(0) NULL DEFAULT NULL COMMENT 'spu_id',
  `sort` int(0) NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_subject_id`(`subject_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '专题商品' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_home_subject_spu
-- ----------------------------
INSERT INTO `sms_home_subject_spu` VALUES (1, 'Mate 30 Pro', 1, 11, 1);
INSERT INTO `sms_home_subject_spu` VALUES (2, 'Mate 30', 1, 12, 2);
INSERT INTO `sms_home_subject_spu` VALUES (3, 'P30 Pro', 1, 13, 3);
INSERT INTO `sms_home_subject_spu` VALUES (4, 'nova 5 Pro', 1, 14, 4);

-- ----------------------------
-- Table structure for sms_member_price
-- ----------------------------
DROP TABLE IF EXISTS `sms_member_price`;
CREATE TABLE `sms_member_price`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id` bigint(0) NULL DEFAULT NULL COMMENT 'sku_id',
  `member_level_id` bigint(0) NULL DEFAULT NULL COMMENT '会员等级id',
  `member_level_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '等级名称',
  `member_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '会员价',
  `add_other` tinyint(0) NULL DEFAULT 0 COMMENT '0不可叠加 1可叠加',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sku_level`(`sku_id`, `member_level_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品会员价格' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_member_price
-- ----------------------------

-- ----------------------------
-- Table structure for sms_seckill_promotion
-- ----------------------------
DROP TABLE IF EXISTS `sms_seckill_promotion`;
CREATE TABLE `sms_seckill_promotion`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动标题',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '开始日期',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '结束日期',
  `status` tinyint(0) NULL DEFAULT 0 COMMENT '上下线',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `user_id` bigint(0) NULL DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '秒杀活动' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_seckill_promotion
-- ----------------------------
INSERT INTO `sms_seckill_promotion` VALUES (1, 'StarPivot 限时秒杀', '2026-06-23 16:30:47', '2026-07-24 16:30:47', 1, '2026-06-24 16:30:47', NULL);

-- ----------------------------
-- Table structure for sms_seckill_session
-- ----------------------------
DROP TABLE IF EXISTS `sms_seckill_session`;
CREATE TABLE `sms_seckill_session`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '场次名称',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT '每日开始',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT '每日结束',
  `status` tinyint(0) NULL DEFAULT 1 COMMENT '启用状态',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '秒杀场次' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_seckill_session
-- ----------------------------
INSERT INTO `sms_seckill_session` VALUES (1, '上午场', '2026-06-24 10:00:00', '2026-06-24 12:00:00', 1, '2026-06-24 16:30:47');
INSERT INTO `sms_seckill_session` VALUES (2, '下午场', '2026-06-24 14:00:00', '2026-06-24 16:00:00', 1, '2026-06-24 16:30:47');
INSERT INTO `sms_seckill_session` VALUES (3, '晚间场', '2026-06-24 20:00:00', '2026-06-24 22:00:00', 1, '2026-06-24 16:30:47');

-- ----------------------------
-- Table structure for sms_seckill_sku_notice
-- ----------------------------
DROP TABLE IF EXISTS `sms_seckill_sku_notice`;
CREATE TABLE `sms_seckill_sku_notice`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint(0) NULL DEFAULT NULL COMMENT '会员id',
  `sku_id` bigint(0) NULL DEFAULT NULL COMMENT 'sku_id',
  `session_id` bigint(0) NULL DEFAULT NULL COMMENT '场次id',
  `subscribe_time` datetime(0) NULL DEFAULT NULL COMMENT '订阅时间',
  `send_time` datetime(0) NULL DEFAULT NULL COMMENT '发送时间',
  `notice_type` tinyint(0) NULL DEFAULT 0 COMMENT '0短信 1邮件',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_sku`(`member_id`, `sku_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '秒杀订阅通知' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_seckill_sku_notice
-- ----------------------------

-- ----------------------------
-- Table structure for sms_seckill_sku_relation
-- ----------------------------
DROP TABLE IF EXISTS `sms_seckill_sku_relation`;
CREATE TABLE `sms_seckill_sku_relation`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `promotion_id` bigint(0) NULL DEFAULT NULL COMMENT '活动id',
  `promotion_session_id` bigint(0) NULL DEFAULT NULL COMMENT '场次id',
  `sku_id` bigint(0) NULL DEFAULT NULL COMMENT 'sku_id',
  `seckill_price` decimal(10, 4) NULL DEFAULT NULL COMMENT '秒杀价',
  `seckill_count` int(0) NULL DEFAULT NULL COMMENT '秒杀总量',
  `seckill_limit` int(0) NULL DEFAULT NULL COMMENT '每人限购',
  `seckill_sort` int(0) NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_promotion_session`(`promotion_id`, `promotion_session_id`) USING BTREE,
  INDEX `idx_sku_id`(`sku_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '秒杀商品关联' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_seckill_sku_relation
-- ----------------------------
INSERT INTO `sms_seckill_sku_relation` VALUES (1, 1, 1, 1, 5999.0000, 100, 1, 1);
INSERT INTO `sms_seckill_sku_relation` VALUES (2, 1, 1, 2, 5499.0000, 100, 1, 2);
INSERT INTO `sms_seckill_sku_relation` VALUES (3, 1, 2, 3, 4299.0000, 50, 1, 1);
INSERT INTO `sms_seckill_sku_relation` VALUES (4, 1, 2, 4, 3999.0000, 50, 1, 2);
INSERT INTO `sms_seckill_sku_relation` VALUES (5, 1, 3, 5, 2999.0000, 30, 1, 1);
INSERT INTO `sms_seckill_sku_relation` VALUES (6, 1, 3, 6, 2799.0000, 30, 1, 2);

-- ----------------------------
-- Table structure for sms_sku_full_reduction
-- ----------------------------
DROP TABLE IF EXISTS `sms_sku_full_reduction`;
CREATE TABLE `sms_sku_full_reduction`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id` bigint(0) NULL DEFAULT NULL COMMENT 'sku_id',
  `full_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '满多少',
  `reduce_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '减多少',
  `add_other` tinyint(0) NULL DEFAULT 0 COMMENT '是否参与其他优惠',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sku_id`(`sku_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品满减' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_sku_full_reduction
-- ----------------------------

-- ----------------------------
-- Table structure for sms_sku_ladder
-- ----------------------------
DROP TABLE IF EXISTS `sms_sku_ladder`;
CREATE TABLE `sms_sku_ladder`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id` bigint(0) NULL DEFAULT NULL COMMENT 'sku_id',
  `full_count` int(0) NULL DEFAULT NULL COMMENT '满几件',
  `discount` decimal(4, 2) NULL DEFAULT NULL COMMENT '折扣',
  `price` decimal(18, 4) NULL DEFAULT NULL COMMENT '折后价',
  `add_other` tinyint(0) NULL DEFAULT 0 COMMENT '0不可叠加 1可叠加',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sku_id`(`sku_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品阶梯价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_sku_ladder
-- ----------------------------

-- ----------------------------
-- Table structure for sms_spu_bounds
-- ----------------------------
DROP TABLE IF EXISTS `sms_spu_bounds`;
CREATE TABLE `sms_spu_bounds`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `spu_id` bigint(0) NULL DEFAULT NULL COMMENT 'spu_id',
  `grow_bounds` decimal(18, 4) NULL DEFAULT NULL COMMENT '成长积分',
  `buy_bounds` decimal(18, 4) NULL DEFAULT NULL COMMENT '购物积分',
  `work` tinyint(0) NULL DEFAULT NULL COMMENT '积分赠送状态位',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_spu_id`(`spu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'SPU积分设置' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_spu_bounds
-- ----------------------------
INSERT INTO `sms_spu_bounds` VALUES (1, 14, 100.0000, 100.0000, 1);

SET FOREIGN_KEY_CHECKS = 1;
