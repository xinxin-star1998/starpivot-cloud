-- =============================================================================
-- StarPivot 商城 - 营销模块 (SMS)
-- 目标库: star_pivot_mall
-- =============================================================================

USE `star_pivot_mall`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `sms_coupon`;
CREATE TABLE `sms_coupon` (
  `id`                bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `coupon_type`       tinyint        NULL DEFAULT NULL COMMENT '0全场 1会员 2购物 3注册',
  `coupon_img`        varchar(2000)  NULL DEFAULT NULL COMMENT '优惠券图片',
  `coupon_name`       varchar(100)   NULL DEFAULT NULL COMMENT '优惠券名称',
  `num`               int            NULL DEFAULT NULL COMMENT '数量',
  `amount`            decimal(18, 4) NULL DEFAULT NULL COMMENT '金额',
  `per_limit`         int            NULL DEFAULT NULL COMMENT '每人限领',
  `min_point`         decimal(18, 4) NULL DEFAULT NULL COMMENT '使用门槛',
  `start_time`        datetime       NULL DEFAULT NULL COMMENT '使用开始',
  `end_time`          datetime       NULL DEFAULT NULL COMMENT '使用结束',
  `use_type`          tinyint        NULL DEFAULT NULL COMMENT '0全场 1分类 2商品',
  `note`              varchar(200)   NULL DEFAULT NULL COMMENT '备注',
  `publish_count`     int            NULL DEFAULT 0 COMMENT '发行数量',
  `use_count`         int            NULL DEFAULT 0 COMMENT '已使用',
  `receive_count`     int            NULL DEFAULT 0 COMMENT '已领取',
  `enable_start_time` datetime       NULL DEFAULT NULL COMMENT '领取开始',
  `enable_end_time`   datetime       NULL DEFAULT NULL COMMENT '领取结束',
  `code`              varchar(64)    NULL DEFAULT NULL COMMENT '优惠码',
  `member_level`      tinyint        NULL DEFAULT 0 COMMENT '0不限等级',
  `publish`           tinyint        NULL DEFAULT 0 COMMENT '0未发布 1已发布',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_publish_time` (`publish`, `enable_start_time`, `enable_end_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券信息' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_coupon_history`;
CREATE TABLE `sms_coupon_history` (
  `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `coupon_id`        bigint       NULL DEFAULT NULL COMMENT '优惠券id',
  `member_id`        bigint       NULL DEFAULT NULL COMMENT '会员id',
  `member_nick_name` varchar(64)  NULL DEFAULT NULL COMMENT '会员昵称',
  `get_type`         tinyint      NULL DEFAULT NULL COMMENT '0后台赠送 1主动领取',
  `create_time`      datetime     NULL DEFAULT NULL COMMENT '领取时间',
  `use_type`         tinyint      NULL DEFAULT 0 COMMENT '0未使用 1已使用 2已过期',
  `use_time`         datetime     NULL DEFAULT NULL COMMENT '使用时间',
  `order_id`         bigint       NULL DEFAULT NULL COMMENT '订单id',
  `order_sn`         char(64)     NULL DEFAULT NULL COMMENT '订单号',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_member_id` (`member_id`) USING BTREE,
  KEY `idx_coupon_id` (`coupon_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券领取历史' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_coupon_spu_category_relation`;
CREATE TABLE `sms_coupon_spu_category_relation` (
  `id`            bigint      NOT NULL AUTO_INCREMENT COMMENT 'id',
  `coupon_id`     bigint      NULL DEFAULT NULL COMMENT '优惠券id',
  `category_id`   bigint      NULL DEFAULT NULL COMMENT '分类id',
  `category_name` varchar(64) NULL DEFAULT NULL COMMENT '分类名称',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_coupon_id` (`coupon_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券分类关联' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_coupon_spu_relation`;
CREATE TABLE `sms_coupon_spu_relation` (
  `id`        bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `coupon_id` bigint       NULL DEFAULT NULL COMMENT '优惠券id',
  `spu_id`    bigint       NULL DEFAULT NULL COMMENT 'spu_id',
  `spu_name`  varchar(255) NULL DEFAULT NULL COMMENT 'spu名称',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_coupon_id` (`coupon_id`) USING BTREE,
  KEY `idx_spu_id` (`spu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券商品关联' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_home_adv`;
CREATE TABLE `sms_home_adv` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name`         varchar(100) NULL DEFAULT NULL COMMENT '名称',
  `pic`          varchar(500) NULL DEFAULT NULL COMMENT '图片',
  `start_time`   datetime     NULL DEFAULT NULL COMMENT '开始时间',
  `end_time`     datetime     NULL DEFAULT NULL COMMENT '结束时间',
  `status`       tinyint      NULL DEFAULT 1 COMMENT '0下线 1上线',
  `click_count`  int          NULL DEFAULT 0 COMMENT '点击数',
  `url`          varchar(500) NULL DEFAULT NULL COMMENT '跳转地址',
  `note`         varchar(500) NULL DEFAULT NULL COMMENT '备注',
  `sort`         int          NULL DEFAULT 0 COMMENT '排序',
  `publisher_id` bigint       NULL DEFAULT NULL COMMENT '发布者',
  `auth_id`      bigint       NULL DEFAULT NULL COMMENT '审核者',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status_sort` (`status`, `sort`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '首页轮播广告' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_home_subject`;
CREATE TABLE `sms_home_subject` (
  `id`        bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name`      varchar(200) NULL DEFAULT NULL COMMENT '专题名',
  `title`     varchar(255) NULL DEFAULT NULL COMMENT '标题',
  `sub_title` varchar(255) NULL DEFAULT NULL COMMENT '副标题',
  `status`    tinyint      NULL DEFAULT 1 COMMENT '显示状态',
  `url`       varchar(500) NULL DEFAULT NULL COMMENT '详情链接',
  `sort`      int          NULL DEFAULT 0 COMMENT '排序',
  `img`       varchar(500) NULL DEFAULT NULL COMMENT '专题图片',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '首页专题' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_home_subject_spu`;
CREATE TABLE `sms_home_subject_spu` (
  `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name`       varchar(200) NULL DEFAULT NULL COMMENT '专题名',
  `subject_id` bigint       NULL DEFAULT NULL COMMENT '专题id',
  `spu_id`     bigint       NULL DEFAULT NULL COMMENT 'spu_id',
  `sort`       int          NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_subject_id` (`subject_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '专题商品' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_member_price`;
CREATE TABLE `sms_member_price` (
  `id`                bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id`            bigint         NULL DEFAULT NULL COMMENT 'sku_id',
  `member_level_id`   bigint         NULL DEFAULT NULL COMMENT '会员等级id',
  `member_level_name` varchar(100)   NULL DEFAULT NULL COMMENT '等级名称',
  `member_price`      decimal(18, 4) NULL DEFAULT NULL COMMENT '会员价',
  `add_other`         tinyint        NULL DEFAULT 0 COMMENT '0不可叠加 1可叠加',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sku_level` (`sku_id`, `member_level_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品会员价格' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_seckill_promotion`;
CREATE TABLE `sms_seckill_promotion` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title`       varchar(255) NULL DEFAULT NULL COMMENT '活动标题',
  `start_time`  datetime     NULL DEFAULT NULL COMMENT '开始日期',
  `end_time`    datetime     NULL DEFAULT NULL COMMENT '结束日期',
  `status`      tinyint      NULL DEFAULT 0 COMMENT '上下线',
  `create_time` datetime     NULL DEFAULT NULL COMMENT '创建时间',
  `user_id`     bigint       NULL DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '秒杀活动' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_seckill_session`;
CREATE TABLE `sms_seckill_session` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name`        varchar(200) NULL DEFAULT NULL COMMENT '场次名称',
  `start_time`  datetime     NULL DEFAULT NULL COMMENT '每日开始',
  `end_time`    datetime     NULL DEFAULT NULL COMMENT '每日结束',
  `status`      tinyint      NULL DEFAULT 1 COMMENT '启用状态',
  `create_time` datetime     NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '秒杀场次' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_seckill_sku_notice`;
CREATE TABLE `sms_seckill_sku_notice` (
  `id`             bigint   NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id`      bigint   NULL DEFAULT NULL COMMENT '会员id',
  `sku_id`         bigint   NULL DEFAULT NULL COMMENT 'sku_id',
  `session_id`     bigint   NULL DEFAULT NULL COMMENT '场次id',
  `subscribe_time` datetime NULL DEFAULT NULL COMMENT '订阅时间',
  `send_time`      datetime NULL DEFAULT NULL COMMENT '发送时间',
  `notice_type`    tinyint  NULL DEFAULT 0 COMMENT '0短信 1邮件',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_member_sku` (`member_id`, `sku_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '秒杀订阅通知' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_seckill_sku_relation`;
CREATE TABLE `sms_seckill_sku_relation` (
  `id`                   bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `promotion_id`         bigint         NULL DEFAULT NULL COMMENT '活动id',
  `promotion_session_id` bigint         NULL DEFAULT NULL COMMENT '场次id',
  `sku_id`               bigint         NULL DEFAULT NULL COMMENT 'sku_id',
  `seckill_price`        decimal(10, 4) NULL DEFAULT NULL COMMENT '秒杀价',
  `seckill_count`        int            NULL DEFAULT NULL COMMENT '秒杀总量',
  `seckill_limit`        int            NULL DEFAULT NULL COMMENT '每人限购',
  `seckill_sort`         int            NULL DEFAULT 0 COMMENT '排序',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_promotion_session` (`promotion_id`, `promotion_session_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '秒杀商品关联' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_sku_full_reduction`;
CREATE TABLE `sms_sku_full_reduction` (
  `id`           bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id`       bigint         NULL DEFAULT NULL COMMENT 'sku_id',
  `full_price`   decimal(18, 4) NULL DEFAULT NULL COMMENT '满多少',
  `reduce_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '减多少',
  `add_other`    tinyint        NULL DEFAULT 0 COMMENT '是否参与其他优惠',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品满减' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_sku_ladder`;
CREATE TABLE `sms_sku_ladder` (
  `id`         bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id`     bigint         NULL DEFAULT NULL COMMENT 'sku_id',
  `full_count` int            NULL DEFAULT NULL COMMENT '满几件',
  `discount`   decimal(4, 2)  NULL DEFAULT NULL COMMENT '折扣',
  `price`      decimal(18, 4) NULL DEFAULT NULL COMMENT '折后价',
  `add_other`  tinyint        NULL DEFAULT 0 COMMENT '0不可叠加 1可叠加',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品阶梯价' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `sms_spu_bounds`;
CREATE TABLE `sms_spu_bounds` (
  `id`          bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `spu_id`      bigint         NULL DEFAULT NULL COMMENT 'spu_id',
  `grow_bounds` decimal(18, 4) NULL DEFAULT NULL COMMENT '成长积分',
  `buy_bounds`  decimal(18, 4) NULL DEFAULT NULL COMMENT '购物积分',
  `work`        tinyint        NULL DEFAULT NULL COMMENT '积分赠送状态位',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_spu_id` (`spu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'SPU积分设置' ROW_FORMAT = Dynamic;

-- 示例秒杀场次
INSERT INTO `sms_seckill_session` (`id`, `name`, `start_time`, `end_time`, `status`, `create_time`) VALUES
(1, '上午场', CONCAT(CURDATE(), ' 10:00:00'), CONCAT(CURDATE(), ' 12:00:00'), 1, NOW()),
(2, '下午场', CONCAT(CURDATE(), ' 14:00:00'), CONCAT(CURDATE(), ' 16:00:00'), 1, NOW()),
(3, '晚间场', CONCAT(CURDATE(), ' 20:00:00'), CONCAT(CURDATE(), ' 22:00:00'), 1, NOW());

-- 示例秒杀活动与商品（依赖 pms_sku_info 已有数据）
INSERT INTO `sms_seckill_promotion` (`id`, `title`, `start_time`, `end_time`, `status`, `create_time`) VALUES
(1, 'StarPivot 限时秒杀', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, NOW());

INSERT INTO `sms_seckill_sku_relation` (`promotion_id`, `promotion_session_id`, `sku_id`, `seckill_price`, `seckill_count`, `seckill_limit`, `seckill_sort`) VALUES
(1, 1, 1, 5999.0000, 100, 1, 1),
(1, 1, 2, 5499.0000, 100, 1, 2),
(1, 2, 3, 4299.0000, 50, 1, 1),
(1, 2, 4, 3999.0000, 50, 1, 2),
(1, 3, 5, 2999.0000, 30, 1, 1),
(1, 3, 6, 2799.0000, 30, 1, 2);

-- 示例首页专题
INSERT INTO `sms_home_subject` (`id`, `name`, `title`, `sub_title`, `status`, `url`, `sort`, `img`) VALUES
(1, '家电焕新', '家电焕新季', '爆款直降 · 品质生活', 1, '/portal', 1, '');

INSERT INTO `sms_home_subject_spu` (`subject_id`, `spu_id`, `name`, `sort`) VALUES
(1, 11, 'Mate 30 Pro', 1),
(1, 12, 'Mate 30', 2),
(1, 13, 'P30 Pro', 3),
(1, 14, 'nova 5 Pro', 4);

SET FOREIGN_KEY_CHECKS = 1;
