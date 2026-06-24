-- =============================================================================
-- StarPivot 商城 - 订单模块 (OMS)
-- 目标库: star_pivot_mall
-- =============================================================================

USE `star_pivot_mall`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 本地消息表（可靠消息 / 事务消息）
-- ----------------------------
DROP TABLE IF EXISTS `mq_message`;
CREATE TABLE `mq_message` (
  `message_id`     char(32)     NOT NULL COMMENT '消息ID',
  `content`        text         NULL COMMENT '消息内容',
  `to_exchange`    varchar(255) NULL DEFAULT NULL COMMENT '目标交换机',
  `routing_key`    varchar(255) NULL DEFAULT NULL COMMENT '路由键',
  `class_type`     varchar(255) NULL DEFAULT NULL COMMENT '消息类型',
  `message_status` tinyint      NULL DEFAULT 0 COMMENT '0-新建 1-已发送 2-错误抵达 3-已抵达',
  `create_time`    datetime     NULL DEFAULT NULL,
  `update_time`    datetime     NULL DEFAULT NULL,
  PRIMARY KEY (`message_id`) USING BTREE,
  KEY `idx_mq_status_time` (`message_status`, `create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'MQ本地消息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 订单
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order` (
  `id`                      bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id`               bigint         NULL DEFAULT NULL COMMENT '会员id',
  `order_sn`                char(64)       NULL DEFAULT NULL COMMENT '订单号',
  `coupon_id`               bigint         NULL DEFAULT NULL COMMENT '使用的优惠券',
  `create_time`             datetime       NULL DEFAULT NULL COMMENT '创建时间',
  `member_username`         varchar(200)   NULL DEFAULT NULL COMMENT '用户名',
  `total_amount`            decimal(18, 4) NULL DEFAULT NULL COMMENT '订单总额',
  `pay_amount`              decimal(18, 4) NULL DEFAULT NULL COMMENT '应付总额',
  `freight_amount`          decimal(18, 4) NULL DEFAULT NULL COMMENT '运费金额',
  `promotion_amount`        decimal(18, 4) NULL DEFAULT NULL COMMENT '促销优化金额',
  `integration_amount`      decimal(18, 4) NULL DEFAULT NULL COMMENT '积分抵扣金额',
  `coupon_amount`           decimal(18, 4) NULL DEFAULT NULL COMMENT '优惠券抵扣金额',
  `discount_amount`         decimal(18, 4) NULL DEFAULT NULL COMMENT '后台调整折扣金额',
  `pay_type`                tinyint        NULL DEFAULT NULL COMMENT '1支付宝 2微信 3银联 4货到付款',
  `source_type`             tinyint        NULL DEFAULT NULL COMMENT '0-PC 1-app',
  `status`                  tinyint        NULL DEFAULT NULL COMMENT '0待付款 1待发货 2已发货 3已完成 4已关闭 5无效',
  `delivery_company`        varchar(64)    NULL DEFAULT NULL COMMENT '物流公司',
  `delivery_sn`             varchar(64)    NULL DEFAULT NULL COMMENT '物流单号',
  `auto_confirm_day`        int            NULL DEFAULT NULL COMMENT '自动确认收货天数',
  `integration`             int            NULL DEFAULT NULL COMMENT '可获得积分',
  `growth`                  int            NULL DEFAULT NULL COMMENT '可获得成长值',
  `bill_type`               tinyint        NULL DEFAULT NULL COMMENT '0不开发票 1电子 2纸质',
  `bill_header`             varchar(255)   NULL DEFAULT NULL COMMENT '发票抬头',
  `bill_content`            varchar(255)   NULL DEFAULT NULL COMMENT '发票内容',
  `bill_receiver_phone`     varchar(32)    NULL DEFAULT NULL COMMENT '收票人电话',
  `bill_receiver_email`     varchar(64)    NULL DEFAULT NULL COMMENT '收票人邮箱',
  `receiver_name`           varchar(100)   NULL DEFAULT NULL COMMENT '收货人姓名',
  `receiver_phone`          varchar(32)    NULL DEFAULT NULL COMMENT '收货人电话',
  `receiver_post_code`      varchar(32)    NULL DEFAULT NULL COMMENT '收货人邮编',
  `receiver_province`       varchar(32)    NULL DEFAULT NULL COMMENT '省份',
  `receiver_city`           varchar(32)    NULL DEFAULT NULL COMMENT '城市',
  `receiver_region`         varchar(32)    NULL DEFAULT NULL COMMENT '区',
  `receiver_detail_address` varchar(200)   NULL DEFAULT NULL COMMENT '详细地址',
  `note`                    varchar(500)   NULL DEFAULT NULL COMMENT '订单备注',
  `confirm_status`          tinyint        NULL DEFAULT NULL COMMENT '0未确认 1已确认',
  `delete_status`           tinyint        NULL DEFAULT 0 COMMENT '0未删除 1已删除',
  `use_integration`         int            NULL DEFAULT NULL COMMENT '下单使用积分',
  `payment_time`            datetime       NULL DEFAULT NULL COMMENT '支付时间',
  `delivery_time`           datetime       NULL DEFAULT NULL COMMENT '发货时间',
  `receive_time`            datetime       NULL DEFAULT NULL COMMENT '确认收货时间',
  `comment_time`            datetime       NULL DEFAULT NULL COMMENT '评价时间',
  `modify_time`             datetime       NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_order_sn` (`order_sn`) USING BTREE,
  KEY `idx_member_id` (`member_id`) USING BTREE,
  KEY `idx_status_create` (`status`, `create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 订单项
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item` (
  `id`                 bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id`           bigint         NULL DEFAULT NULL COMMENT '订单id',
  `order_sn`           char(64)       NULL DEFAULT NULL COMMENT '订单号',
  `spu_id`             bigint         NULL DEFAULT NULL COMMENT 'spu_id',
  `spu_name`           varchar(255)   NULL DEFAULT NULL COMMENT 'spu_name',
  `spu_pic`            varchar(500)   NULL DEFAULT NULL COMMENT 'spu_pic',
  `spu_brand`          varchar(200)   NULL DEFAULT NULL COMMENT '品牌',
  `category_id`        bigint         NULL DEFAULT NULL COMMENT '分类id',
  `sku_id`             bigint         NULL DEFAULT NULL COMMENT 'sku_id',
  `sku_name`           varchar(255)   NULL DEFAULT NULL COMMENT 'sku名称',
  `sku_pic`            varchar(500)   NULL DEFAULT NULL COMMENT 'sku图片',
  `sku_price`          decimal(18, 4) NULL DEFAULT NULL COMMENT 'sku价格',
  `sku_quantity`       int            NULL DEFAULT NULL COMMENT '购买数量',
  `sku_attrs_vals`     varchar(500)   NULL DEFAULT NULL COMMENT '销售属性JSON',
  `promotion_amount`   decimal(18, 4) NULL DEFAULT NULL COMMENT '促销分解金额',
  `coupon_amount`      decimal(18, 4) NULL DEFAULT NULL COMMENT '优惠券分解金额',
  `integration_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '积分分解金额',
  `real_amount`        decimal(18, 4) NULL DEFAULT NULL COMMENT '优惠后分解金额',
  `gift_integration`   int            NULL DEFAULT NULL COMMENT '赠送积分',
  `gift_growth`        int            NULL DEFAULT NULL COMMENT '赠送成长值',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_order_sn` (`order_sn`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单项信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 订单操作历史
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_operate_history`;
CREATE TABLE `oms_order_operate_history` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id`     bigint       NULL DEFAULT NULL COMMENT '订单id',
  `operate_man`  varchar(100) NULL DEFAULT NULL COMMENT '操作人',
  `create_time`  datetime     NULL DEFAULT NULL COMMENT '操作时间',
  `order_status` tinyint      NULL DEFAULT NULL COMMENT '订单状态',
  `note`         varchar(500) NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单操作历史记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 退货申请
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_return_apply`;
CREATE TABLE `oms_order_return_apply` (
  `id`               bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id`         bigint         NULL DEFAULT NULL COMMENT '订单id',
  `sku_id`           bigint         NULL DEFAULT NULL COMMENT '退货sku_id',
  `order_sn`         char(64)       NULL DEFAULT NULL COMMENT '订单号',
  `create_time`      datetime       NULL DEFAULT NULL COMMENT '申请时间',
  `member_username`  varchar(64)    NULL DEFAULT NULL COMMENT '会员用户名',
  `return_amount`    decimal(18, 4) NULL DEFAULT NULL COMMENT '退款金额',
  `return_name`      varchar(100)   NULL DEFAULT NULL COMMENT '退货人姓名',
  `return_phone`     varchar(20)    NULL DEFAULT NULL COMMENT '退货人电话',
  `status`           tinyint        NULL DEFAULT NULL COMMENT '0待处理 1退货中 2已完成 3已拒绝',
  `handle_time`      datetime       NULL DEFAULT NULL COMMENT '处理时间',
  `sku_img`          varchar(500)   NULL DEFAULT NULL COMMENT '商品图片',
  `sku_name`         varchar(200)   NULL DEFAULT NULL COMMENT '商品名称',
  `sku_brand`        varchar(200)   NULL DEFAULT NULL COMMENT '商品品牌',
  `sku_attrs_vals`   varchar(500)   NULL DEFAULT NULL COMMENT '销售属性JSON',
  `sku_count`        int            NULL DEFAULT NULL COMMENT '退货数量',
  `sku_price`        decimal(18, 4) NULL DEFAULT NULL COMMENT '商品单价',
  `sku_real_price`   decimal(18, 4) NULL DEFAULT NULL COMMENT '实付单价',
  `reason`           varchar(200)   NULL DEFAULT NULL COMMENT '原因',
  `description`      varchar(500)   NULL DEFAULT NULL COMMENT '描述',
  `desc_pics`        varchar(2000)  NULL DEFAULT NULL COMMENT '凭证图片',
  `handle_note`      varchar(500)   NULL DEFAULT NULL COMMENT '处理备注',
  `handle_man`       varchar(200)   NULL DEFAULT NULL COMMENT '处理人员',
  `receive_man`      varchar(100)   NULL DEFAULT NULL COMMENT '收货人',
  `receive_time`     datetime       NULL DEFAULT NULL COMMENT '收货时间',
  `receive_note`     varchar(500)   NULL DEFAULT NULL COMMENT '收货备注',
  `receive_phone`    varchar(20)    NULL DEFAULT NULL COMMENT '收货电话',
  `company_address`  varchar(500)   NULL DEFAULT NULL COMMENT '公司收货地址',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单退货申请' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 退货原因
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_return_reason`;
CREATE TABLE `oms_order_return_reason` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name`        varchar(200) NULL DEFAULT NULL COMMENT '退货原因',
  `sort`        int          NULL DEFAULT 0 COMMENT '排序',
  `status`      tinyint      NULL DEFAULT 1 COMMENT '0禁用 1启用',
  `create_time` datetime     NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '退货原因' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 订单配置
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_setting`;
CREATE TABLE `oms_order_setting` (
  `id`                   bigint  NOT NULL AUTO_INCREMENT COMMENT 'id',
  `flash_order_overtime` int     NULL DEFAULT 30 COMMENT '秒杀订单超时(分)',
  `normal_order_overtime` int  NULL DEFAULT 30 COMMENT '正常订单超时(分)',
  `confirm_overtime`     int     NULL DEFAULT 7 COMMENT '自动确认收货(天)',
  `finish_overtime`      int     NULL DEFAULT 7 COMMENT '自动完成交易(天)',
  `comment_overtime`     int     NULL DEFAULT 7 COMMENT '自动好评(天)',
  `member_level`         tinyint NULL DEFAULT 0 COMMENT '0不限等级',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '订单配置信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 支付信息
-- ----------------------------
DROP TABLE IF EXISTS `oms_payment_info`;
CREATE TABLE `oms_payment_info` (
  `id`               bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_sn`         char(64)       NULL DEFAULT NULL COMMENT '订单号',
  `order_id`         bigint         NULL DEFAULT NULL COMMENT '订单id',
  `alipay_trade_no`  varchar(50)    NULL DEFAULT NULL COMMENT '支付宝流水号',
  `total_amount`     decimal(18, 4) NULL DEFAULT NULL COMMENT '支付总金额',
  `subject`          varchar(200)   NULL DEFAULT NULL COMMENT '交易内容',
  `payment_status`   varchar(20)    NULL DEFAULT NULL COMMENT '支付状态',
  `create_time`      datetime       NULL DEFAULT NULL COMMENT '创建时间',
  `confirm_time`     datetime       NULL DEFAULT NULL COMMENT '确认时间',
  `callback_content` varchar(4000)  NULL DEFAULT NULL COMMENT '回调内容',
  `callback_time`    datetime       NULL DEFAULT NULL COMMENT '回调时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_payment_order_sn` (`order_sn`) USING BTREE,
  UNIQUE KEY `uk_alipay_trade_no` (`alipay_trade_no`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '支付信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 退款信息
-- ----------------------------
DROP TABLE IF EXISTS `oms_refund_info`;
CREATE TABLE `oms_refund_info` (
  `id`              bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_return_id` bigint         NULL DEFAULT NULL COMMENT '退货申请id',
  `refund`          decimal(18, 4) NULL DEFAULT NULL COMMENT '退款金额',
  `refund_sn`       varchar(64)    NULL DEFAULT NULL COMMENT '退款流水号',
  `refund_status`   tinyint        NULL DEFAULT NULL COMMENT '退款状态',
  `refund_channel`  tinyint        NULL DEFAULT NULL COMMENT '1支付宝 2微信 3银联 4汇款',
  `refund_content`  varchar(5000)  NULL DEFAULT NULL COMMENT '回调内容',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_order_return_id` (`order_return_id`) USING BTREE,
  KEY `idx_refund_sn` (`refund_sn`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '退款信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 初始数据
-- ----------------------------
INSERT INTO `oms_order_setting` (`id`, `flash_order_overtime`, `normal_order_overtime`, `confirm_overtime`, `finish_overtime`, `comment_overtime`, `member_level`) VALUES
(1, 15, 30, 7, 7, 7, 0);

INSERT INTO `oms_order_return_reason` (`name`, `sort`, `status`, `create_time`) VALUES
('商品质量问题', 1, 1, NOW()),
('商品与描述不符', 2, 1, NOW()),
('收到商品破损', 3, 1, NOW()),
('拍错/多拍/不想要', 4, 1, NOW()),
('其他原因', 99, 1, NOW());

SET FOREIGN_KEY_CHECKS = 1;
