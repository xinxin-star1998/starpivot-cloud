-- =============================================================================
-- StarPivot 商城 - 会员模块 (UMS)
-- 目标库: star_pivot_mall
-- =============================================================================

USE `star_pivot_mall`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `ums_growth_change_history`;
CREATE TABLE `ums_growth_change_history` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id`    bigint       NULL DEFAULT NULL COMMENT '会员id',
  `create_time`  datetime     NULL DEFAULT NULL COMMENT '创建时间',
  `change_count` int          NULL DEFAULT NULL COMMENT '变化值(正负)',
  `note`         varchar(255) NULL DEFAULT NULL COMMENT '备注',
  `source_type`  tinyint      NULL DEFAULT NULL COMMENT '0购物 1管理员修改',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_member_id` (`member_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '成长值变化历史' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `ums_integration_change_history`;
CREATE TABLE `ums_integration_change_history` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id`    bigint       NULL DEFAULT NULL COMMENT '会员id',
  `create_time`  datetime     NULL DEFAULT NULL COMMENT '创建时间',
  `change_count` int          NULL DEFAULT NULL COMMENT '变化值',
  `note`         varchar(255) NULL DEFAULT NULL COMMENT '备注',
  `source_type`  tinyint      NULL DEFAULT NULL COMMENT '0购物 1管理员 2活动',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_member_id` (`member_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分变化历史' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `ums_member`;
CREATE TABLE `ums_member` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `level_id`     bigint       NULL DEFAULT NULL COMMENT '会员等级id',
  `username`     varchar(64)  NULL DEFAULT NULL COMMENT '用户名',
  `password`     varchar(128) NULL DEFAULT NULL COMMENT '密码',
  `nickname`     varchar(64)  NULL DEFAULT NULL COMMENT '昵称',
  `mobile`       varchar(20)  NULL DEFAULT NULL COMMENT '手机号',
  `email`        varchar(64)  NULL DEFAULT NULL COMMENT '邮箱',
  `header`       varchar(500) NULL DEFAULT NULL COMMENT '头像',
  `gender`       tinyint      NULL DEFAULT NULL COMMENT '性别',
  `birth`        date         NULL DEFAULT NULL COMMENT '生日',
  `city`         varchar(500) NULL DEFAULT NULL COMMENT '城市',
  `job`          varchar(255) NULL DEFAULT NULL COMMENT '职业',
  `sign`         varchar(255) NULL DEFAULT NULL COMMENT '个性签名',
  `source_type`  tinyint      NULL DEFAULT NULL COMMENT '用户来源',
  `integration`  int          NULL DEFAULT 0 COMMENT '积分',
  `growth`       int          NULL DEFAULT 0 COMMENT '成长值',
  `status`       tinyint      NULL DEFAULT 1 COMMENT '1启用 0禁用',
  `create_time`  datetime     NULL DEFAULT NULL COMMENT '注册时间',
  `social_uid`   varchar(255) NULL DEFAULT NULL COMMENT '社交用户id',
  `access_token` varchar(255) NULL DEFAULT NULL COMMENT '访问令牌',
  `expires_in`   varchar(255) NULL DEFAULT NULL COMMENT '令牌过期时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_username` (`username`) USING BTREE,
  UNIQUE KEY `uk_mobile` (`mobile`) USING BTREE,
  KEY `idx_level_id` (`level_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `ums_member_collect_spu`;
CREATE TABLE `ums_member_collect_spu` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id`   bigint       NULL DEFAULT NULL COMMENT '会员id',
  `spu_id`      bigint       NULL DEFAULT NULL COMMENT 'spu_id',
  `spu_name`    varchar(500) NULL DEFAULT NULL COMMENT 'spu名称',
  `spu_img`     varchar(500) NULL DEFAULT NULL COMMENT 'spu图片',
  `create_time` datetime     NULL DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_member_spu` (`member_id`, `spu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员收藏商品' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `ums_member_collect_subject`;
CREATE TABLE `ums_member_collect_subject` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id`    bigint       NULL DEFAULT NULL COMMENT '会员id',
  `subject_id`   bigint       NULL DEFAULT NULL COMMENT '专题id',
  `subject_name` varchar(255) NULL DEFAULT NULL COMMENT '专题名称',
  `subject_img`  varchar(500) NULL DEFAULT NULL COMMENT '专题图片',
  `subject_url`  varchar(500) NULL DEFAULT NULL COMMENT '活动链接',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_member_id` (`member_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员收藏专题' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `ums_member_level`;
CREATE TABLE `ums_member_level` (
  `id`                      bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name`                    varchar(100)   NULL DEFAULT NULL COMMENT '等级名称',
  `growth_point`            int            NULL DEFAULT NULL COMMENT '所需成长值',
  `default_status`          tinyint        NULL DEFAULT 0 COMMENT '是否默认等级',
  `free_freight_point`      decimal(18, 4) NULL DEFAULT NULL COMMENT '免运费标准',
  `comment_growth_point`    int            NULL DEFAULT NULL COMMENT '评价成长值',
  `privilege_free_freight`  tinyint        NULL DEFAULT 0 COMMENT '免邮特权',
  `privilege_member_price`  tinyint        NULL DEFAULT 0 COMMENT '会员价特权',
  `privilege_birthday`      tinyint        NULL DEFAULT 0 COMMENT '生日特权',
  `note`                    varchar(255)   NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员等级' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `ums_member_login_log`;
CREATE TABLE `ums_member_login_log` (
  `id`          bigint      NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id`   bigint      NULL DEFAULT NULL COMMENT '会员id',
  `create_time` datetime    NULL DEFAULT NULL COMMENT '登录时间',
  `ip`          varchar(64) NULL DEFAULT NULL COMMENT 'IP',
  `city`        varchar(64) NULL DEFAULT NULL COMMENT '城市',
  `login_type`  tinyint     NULL DEFAULT NULL COMMENT '1-web 2-app',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_member_id` (`member_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员登录记录' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `ums_member_receive_address`;
CREATE TABLE `ums_member_receive_address` (
  `id`             bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id`      bigint       NULL DEFAULT NULL COMMENT '会员id',
  `name`           varchar(255) NULL DEFAULT NULL COMMENT '收货人',
  `phone`          varchar(64)  NULL DEFAULT NULL COMMENT '电话',
  `post_code`      varchar(64)  NULL DEFAULT NULL COMMENT '邮编',
  `province`       varchar(100) NULL DEFAULT NULL COMMENT '省',
  `city`           varchar(100) NULL DEFAULT NULL COMMENT '市',
  `region`         varchar(100) NULL DEFAULT NULL COMMENT '区',
  `detail_address` varchar(255) NULL DEFAULT NULL COMMENT '详细地址',
  `areacode`       varchar(15)  NULL DEFAULT NULL COMMENT '区域编码',
  `default_status` tinyint      NULL DEFAULT 0 COMMENT '是否默认',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_member_id` (`member_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员收货地址' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `ums_member_statistics_info`;
CREATE TABLE `ums_member_statistics_info` (
  `id`                     bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id`              bigint         NULL DEFAULT NULL COMMENT '会员id',
  `consume_amount`         decimal(18, 4) NULL DEFAULT 0 COMMENT '累计消费',
  `coupon_amount`          decimal(18, 4) NULL DEFAULT 0 COMMENT '累计优惠',
  `order_count`            int            NULL DEFAULT 0 COMMENT '订单数',
  `coupon_count`           int            NULL DEFAULT 0 COMMENT '优惠券数',
  `comment_count`          int            NULL DEFAULT 0 COMMENT '评价数',
  `return_order_count`     int            NULL DEFAULT 0 COMMENT '退货数',
  `login_count`            int            NULL DEFAULT 0 COMMENT '登录次数',
  `attend_count`           int            NULL DEFAULT 0 COMMENT '关注数',
  `fans_count`             int            NULL DEFAULT 0 COMMENT '粉丝数',
  `collect_product_count`  int            NULL DEFAULT 0 COMMENT '收藏商品数',
  `collect_subject_count`  int            NULL DEFAULT 0 COMMENT '收藏专题数',
  `collect_comment_count`  int            NULL DEFAULT 0 COMMENT '收藏评论数',
  `invite_friend_count`    int            NULL DEFAULT 0 COMMENT '邀请好友数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_member_id` (`member_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员统计信息' ROW_FORMAT = Dynamic;

INSERT INTO `ums_member_level` (`id`, `name`, `growth_point`, `default_status`, `free_freight_point`, `comment_growth_point`, `privilege_free_freight`, `privilege_member_price`, `privilege_birthday`, `note`) VALUES
(1, '普通会员', 0, 1, 99.0000, 10, 0, 0, 0, '默认等级'),
(2, '银卡会员', 1000, 0, 79.0000, 20, 0, 1, 0, NULL),
(3, '金卡会员', 5000, 0, 49.0000, 30, 1, 1, 1, NULL),
(4, '钻石会员', 20000, 0, 0.0000, 50, 1, 1, 1, NULL);

SET FOREIGN_KEY_CHECKS = 1;
