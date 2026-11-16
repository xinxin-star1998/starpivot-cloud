-- ----------------------------
-- 拼团 / 砍价 营销活动
-- 依赖：starpivot-mall-promotion 模块
-- ----------------------------

-- ----------------------------
-- Table structure for sms_group_buy_activity
-- ----------------------------
DROP TABLE IF EXISTS `sms_group_buy_activity`;
CREATE TABLE `sms_group_buy_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动标题',
  `sku_id` bigint NULL DEFAULT NULL COMMENT 'sku_id',
  `group_price` decimal(10, 4) NULL DEFAULT NULL COMMENT '拼团价',
  `group_size` int NULL DEFAULT 2 COMMENT '成团人数（含团长）',
  `stock_count` int NULL DEFAULT 0 COMMENT '活动库存',
  `limit_buy` int NULL DEFAULT 1 COMMENT '每人限购件数',
  `start_time` datetime NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '活动结束时间',
  `valid_hours` int NULL DEFAULT 24 COMMENT '开团后成团有效小时数',
  `status` tinyint NULL DEFAULT 0 COMMENT '0下线 1上线',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sku_id`(`sku_id`) USING BTREE,
  KEY `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '拼团活动' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sms_group_buy_record
-- ----------------------------
DROP TABLE IF EXISTS `sms_group_buy_record`;
CREATE TABLE `sms_group_buy_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `activity_id` bigint NOT NULL COMMENT '活动id',
  `member_id` bigint NOT NULL COMMENT '参团会员id',
  `leader_member_id` bigint NULL DEFAULT NULL COMMENT '团长会员id，非团长为空',
  `sku_id` bigint NOT NULL COMMENT 'sku_id',
  `group_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '团号',
  `group_size` int NULL DEFAULT 2 COMMENT '成团人数',
  `group_price` decimal(10, 4) NULL DEFAULT NULL COMMENT '拼团价',
  `quantity` int NULL DEFAULT 1 COMMENT '购买数量',
  `status` tinyint NULL DEFAULT 0 COMMENT '0待支付 1待成团 2已成团 3成团失败 4已取消',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '成团截止时间',
  `pay_expire_time` datetime NULL DEFAULT NULL COMMENT '支付截止时间',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单id',
  `order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单号',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `complete_time` datetime NULL DEFAULT NULL COMMENT '成团时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_activity_status`(`activity_id`, `status`) USING BTREE,
  KEY `idx_group_no`(`group_no`) USING BTREE,
  KEY `idx_member_id`(`member_id`) USING BTREE,
  KEY `idx_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '拼团记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sms_bargain_activity
-- ----------------------------
DROP TABLE IF EXISTS `sms_bargain_activity`;
CREATE TABLE `sms_bargain_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动标题',
  `sku_id` bigint NULL DEFAULT NULL COMMENT 'sku_id',
  `original_price` decimal(10, 4) NULL DEFAULT NULL COMMENT '商品原价',
  `floor_price` decimal(10, 4) NULL DEFAULT NULL COMMENT '砍价底价',
  `bargain_times` int NULL DEFAULT 10 COMMENT '最多助力次数',
  `min_amount` decimal(10, 4) NULL DEFAULT 1.0000 COMMENT '单次最低砍价金额',
  `max_amount` decimal(10, 4) NULL DEFAULT 10.0000 COMMENT '单次最高砍价金额',
  `stock_count` int NULL DEFAULT 0 COMMENT '活动库存',
  `limit_buy` int NULL DEFAULT 1 COMMENT '每人限购件数',
  `start_time` datetime NULL DEFAULT NULL COMMENT '活动开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '活动结束时间',
  `valid_hours` int NULL DEFAULT 24 COMMENT '发起砍价有效小时数',
  `status` tinyint NULL DEFAULT 0 COMMENT '0下线 1上线',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `user_id` bigint NULL DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sku_id`(`sku_id`) USING BTREE,
  KEY `idx_status`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '砍价活动' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sms_bargain_record
-- ----------------------------
DROP TABLE IF EXISTS `sms_bargain_record`;
CREATE TABLE `sms_bargain_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `activity_id` bigint NOT NULL COMMENT '活动id',
  `member_id` bigint NOT NULL COMMENT '发起会员id',
  `sku_id` bigint NOT NULL COMMENT 'sku_id',
  `original_price` decimal(10, 4) NULL DEFAULT NULL COMMENT '原价',
  `current_price` decimal(10, 4) NULL DEFAULT NULL COMMENT '当前砍后价',
  `floor_price` decimal(10, 4) NULL DEFAULT NULL COMMENT '底价',
  `status` tinyint NULL DEFAULT 0 COMMENT '0砍价中 1已到底 2已下单 3已失效',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '砍价截止时间',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单id',
  `order_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '订单号',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_activity_member`(`activity_id`, `member_id`) USING BTREE,
  KEY `idx_status`(`status`) USING BTREE,
  KEY `idx_order_id`(`order_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '砍价记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sms_bargain_help
-- ----------------------------
DROP TABLE IF EXISTS `sms_bargain_help`;
CREATE TABLE `sms_bargain_help` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `record_id` bigint NOT NULL COMMENT '砍价记录id',
  `helper_member_id` bigint NOT NULL COMMENT '助力会员id',
  `cut_amount` decimal(10, 4) NULL DEFAULT NULL COMMENT '本次砍掉金额',
  `create_time` datetime NULL DEFAULT NULL COMMENT '助力时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_record_helper`(`record_id`, `helper_member_id`) USING BTREE,
  KEY `idx_record_id`(`record_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '砍价助力记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sms_group_buy_activity
-- ----------------------------
INSERT INTO `sms_group_buy_activity`
VALUES (1, '华为 Mate 30 Pro 3人成团', 35, 5599.0000, 3, 100, 1, '2026-10-01 00:00:00', '2026-12-31 23:59:59', 24, 1, 1, NOW(), NOW(), NULL);

-- ----------------------------
-- Records of sms_bargain_activity
-- ----------------------------
INSERT INTO `sms_bargain_activity`
VALUES (1, 'iPhone 11 128G 砍至底价', 86, 5999.0000, 4999.0000, 10, 30.0000, 120.0000, 100, 1, '2026-10-01 00:00:00', '2026-12-31 23:59:59', 48, 1, 1, NOW(), NOW(), NULL);

-- ----------------------------
-- 后台菜单（营销管理 parent_id=31）
-- ----------------------------
INSERT INTO `sys_menu` VALUES (352, '拼团活动', 31, 9, 'groupbuy', '/mall/sms/groupbuy/index', NULL, 'GroupBuyActivity', 1, 1, 'C', '0', '0', 'mall:groupbuy:list', 'ri:group-line', 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (353, '拼团查询', 352, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:groupbuy:query', '#', 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (354, '拼团新增', 352, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:groupbuy:add', '#', 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (355, '拼团修改', 352, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:groupbuy:edit', '#', 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (356, '拼团删除', 352, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:groupbuy:delete', '#', 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (357, '砍价活动', 31, 10, 'bargain', '/mall/sms/bargain/index', NULL, 'BargainActivity', 1, 1, 'C', '0', '0', 'mall:bargain:list', 'ri:scissors-line', 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (358, '砍价查询', 357, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bargain:query', '#', 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (359, '砍价新增', 357, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bargain:add', '#', 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (360, '砍价修改', 357, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bargain:edit', '#', 'admin', NOW(), 'admin', NOW(), '');
INSERT INTO `sys_menu` VALUES (361, '砍价删除', 357, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bargain:delete', '#', 'admin', NOW(), 'admin', NOW(), '');
