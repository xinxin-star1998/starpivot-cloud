-- =============================================================================
-- StarPivot 商城 - 仓储模块 (WMS)
-- 目标库: star_pivot_mall
-- =============================================================================

USE `star_pivot_mall`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `wms_purchase`;
CREATE TABLE `wms_purchase` (
  `id`             bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `assignee_id`    bigint         NULL DEFAULT NULL COMMENT '采购人id',
  `assignee_name`  varchar(255)   NULL DEFAULT NULL COMMENT '采购人',
  `phone`          varchar(20)    NULL DEFAULT NULL COMMENT '联系电话',
  `priority`       int            NULL DEFAULT NULL COMMENT '优先级',
  `status`         int            NULL DEFAULT 0 COMMENT '状态',
  `ware_id`        bigint         NULL DEFAULT NULL COMMENT '仓库id',
  `amount`         decimal(18, 4) NULL DEFAULT NULL COMMENT '采购金额',
  `create_time`    datetime       NULL DEFAULT NULL COMMENT '创建时间',
  `update_time`    datetime       NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_ware_id` (`ware_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '采购单' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `wms_purchase_detail`;
CREATE TABLE `wms_purchase_detail` (
  `id`          bigint         NOT NULL AUTO_INCREMENT COMMENT 'id',
  `purchase_id` bigint         NULL DEFAULT NULL COMMENT '采购单id',
  `sku_id`      bigint         NULL DEFAULT NULL COMMENT 'sku_id',
  `sku_num`     int            NULL DEFAULT NULL COMMENT '采购数量',
  `sku_price`   decimal(18, 4) NULL DEFAULT NULL COMMENT '采购单价',
  `ware_id`     bigint         NULL DEFAULT NULL COMMENT '仓库id',
  `status`      int            NULL DEFAULT 0 COMMENT '0新建 1已分配 2采购中 3完成 4失败',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_purchase_id` (`purchase_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '采购明细' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `wms_ware_info`;
CREATE TABLE `wms_ware_info` (
  `id`       bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name`     varchar(255) NULL DEFAULT NULL COMMENT '仓库名',
  `address`  varchar(255) NULL DEFAULT NULL COMMENT '地址',
  `areacode` varchar(20)  NULL DEFAULT NULL COMMENT '区域编码',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '仓库信息' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `wms_ware_order_task`;
CREATE TABLE `wms_ware_order_task` (
  `id`               bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id`         bigint       NULL DEFAULT NULL COMMENT '订单id',
  `order_sn`         varchar(64)  NULL DEFAULT NULL COMMENT '订单号',
  `consignee`        varchar(100) NULL DEFAULT NULL COMMENT '收货人',
  `consignee_tel`    varchar(20)  NULL DEFAULT NULL COMMENT '收货电话',
  `delivery_address` varchar(500) NULL DEFAULT NULL COMMENT '配送地址',
  `order_comment`    varchar(200) NULL DEFAULT NULL COMMENT '订单备注',
  `payment_way`      tinyint      NULL DEFAULT NULL COMMENT '1在线 2货到付款',
  `task_status`      tinyint      NULL DEFAULT 0 COMMENT '任务状态',
  `order_body`       varchar(255) NULL DEFAULT NULL COMMENT '订单描述',
  `tracking_no`      varchar(30)  NULL DEFAULT NULL COMMENT '物流单号',
  `create_time`      datetime     NULL DEFAULT NULL COMMENT '创建时间',
  `ware_id`          bigint       NULL DEFAULT NULL COMMENT '仓库id',
  `task_comment`     varchar(500) NULL DEFAULT NULL COMMENT '工作单备注',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_order_id` (`order_id`) USING BTREE,
  KEY `idx_order_sn` (`order_sn`) USING BTREE,
  KEY `idx_task_status` (`task_status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存工作单' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `wms_ware_order_task_detail`;
CREATE TABLE `wms_ware_order_task_detail` (
  `id`          bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id`      bigint       NULL DEFAULT NULL COMMENT 'sku_id',
  `sku_name`    varchar(255) NULL DEFAULT NULL COMMENT 'sku名称',
  `sku_num`     int          NULL DEFAULT NULL COMMENT '数量',
  `task_id`     bigint       NULL DEFAULT NULL COMMENT '工作单id',
  `ware_id`     bigint       NULL DEFAULT NULL COMMENT '仓库id',
  `lock_status` int          NULL DEFAULT NULL COMMENT '1锁定 2解锁 3扣减',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_task_id` (`task_id`) USING BTREE,
  KEY `idx_sku_id` (`sku_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '库存工作单明细' ROW_FORMAT = Dynamic;

DROP TABLE IF EXISTS `wms_ware_sku`;
CREATE TABLE `wms_ware_sku` (
  `id`           bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id`       bigint       NULL DEFAULT NULL COMMENT 'sku_id',
  `ware_id`      bigint       NULL DEFAULT NULL COMMENT '仓库id',
  `stock`        int          NULL DEFAULT 0 COMMENT '库存',
  `sku_name`     varchar(200) NULL DEFAULT NULL COMMENT 'sku名称',
  `stock_locked` int          NULL DEFAULT 0 COMMENT '锁定库存',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_sku_ware` (`sku_id`, `ware_id`) USING BTREE,
  KEY `idx_ware_id` (`ware_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '商品库存' ROW_FORMAT = Dynamic;

INSERT INTO `wms_ware_info` (`id`, `name`, `address`, `areacode`) VALUES
(1, '北京仓', '北京市大兴区亦庄经济开发区', '110000'),
(2, '上海仓', '上海市嘉定区外冈镇', '310000'),
(3, '广州仓', '广东省广州市黄埔区', '440100');

SET FOREIGN_KEY_CHECKS = 1;
