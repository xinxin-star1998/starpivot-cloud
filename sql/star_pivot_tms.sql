-- TMS 运输/物流管理 — 业务库 star_pivot_tms
CREATE DATABASE IF NOT EXISTS `star_pivot_tms` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `star_pivot_tms`;

-- 承运商主数据
DROP TABLE IF EXISTS `tms_track_event`;
DROP TABLE IF EXISTS `tms_shipment`;
DROP TABLE IF EXISTS `tms_carrier`;

CREATE TABLE `tms_carrier` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `carrier_code` varchar(32) NOT NULL COMMENT '承运商编码',
  `carrier_name` varchar(64) NOT NULL COMMENT '承运商名称',
  `kuaidi100_com` varchar(32) NOT NULL COMMENT '快递100 com 编码',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '0正常 1停用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '0存在 2删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_carrier_code` (`carrier_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TMS承运商';

CREATE TABLE `tms_shipment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shipment_sn` varchar(64) NOT NULL COMMENT '运单业务号',
  `biz_module` varchar(32) NOT NULL COMMENT '业务域 mall',
  `biz_type` varchar(32) NOT NULL COMMENT '业务类型 order',
  `biz_id` bigint NOT NULL COMMENT '业务主键',
  `biz_key` varchar(128) NOT NULL COMMENT '业务唯一键',
  `order_sn` varchar(64) DEFAULT NULL COMMENT '关联订单号',
  `carrier_id` bigint DEFAULT NULL COMMENT '承运商ID',
  `carrier_name` varchar(64) DEFAULT NULL COMMENT '承运商名称',
  `kuaidi100_com` varchar(32) DEFAULT NULL COMMENT '快递100编码',
  `tracking_no` varchar(64) NOT NULL COMMENT '物流单号',
  `status` varchar(32) NOT NULL DEFAULT 'SHIPPED' COMMENT 'SHIPPED/IN_TRANSIT/DELIVERED/EXCEPTION',
  `receiver_name` varchar(64) DEFAULT NULL COMMENT '收件人',
  `receiver_phone` varchar(32) DEFAULT NULL COMMENT '收件电话',
  `receiver_address` varchar(512) DEFAULT NULL COMMENT '收件地址',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `deliver_time` datetime DEFAULT NULL COMMENT '签收时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '0存在 2删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_shipment_sn` (`shipment_sn`),
  KEY `idx_biz` (`biz_module`, `biz_type`, `biz_id`),
  KEY `idx_tracking_no` (`tracking_no`),
  KEY `idx_order_sn` (`order_sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TMS运单';

CREATE TABLE `tms_track_event` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `shipment_id` bigint NOT NULL COMMENT '运单ID',
  `event_time` datetime NOT NULL COMMENT '事件时间',
  `event_status` varchar(64) DEFAULT NULL COMMENT '状态码',
  `event_desc` varchar(512) NOT NULL COMMENT '描述',
  `location` varchar(128) DEFAULT NULL COMMENT '地点',
  `source` varchar(32) NOT NULL DEFAULT 'SYSTEM' COMMENT 'SYSTEM/KUAIDI100/MANUAL',
  `raw_json` text COMMENT '原始报文',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_shipment_id` (`shipment_id`),
  KEY `idx_event_time` (`event_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TMS物流轨迹';

-- 常用承运商种子数据
INSERT INTO `tms_carrier` (`carrier_code`, `carrier_name`, `kuaidi100_com`, `sort_order`, `status`, `remark`) VALUES
('SF', '顺丰速运', 'shunfeng', 1, '0', '快递100: shunfeng'),
('YTO', '圆通速递', 'yuantong', 2, '0', '快递100: yuantong'),
('ZTO', '中通快递', 'zhongtong', 3, '0', '快递100: zhongtong'),
('YD', '韵达快递', 'yunda', 4, '0', '快递100: yunda'),
('STO', '申通快递', 'shentong', 5, '0', '快递100: shentong'),
('JD', '京东物流', 'jd', 6, '0', '快递100: jd'),
('EMS', 'EMS', 'ems', 7, '0', '快递100: ems'),
('HTKY', '百世快递', 'huitongkuaidi', 8, '0', '快递100: huitongkuaidi');
