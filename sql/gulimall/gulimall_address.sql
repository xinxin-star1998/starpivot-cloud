-- =============================================================================
-- StarPivot 商城 - 省市区地址库（WMS 地区管理，仅建表）
-- 目标库: star_pivot_mall
-- 完整数据: 执行同目录 gulimall_address_data.sql（约 5.6 万条，~7MB）
-- =============================================================================

USE `star_pivot_mall`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id`          int unsigned NOT NULL AUTO_INCREMENT,
  `code`        varchar(20)  NOT NULL COMMENT '地区编码',
  `parent_code` varchar(20)  NOT NULL COMMENT '父级编码',
  `name`        varchar(255) NOT NULL COMMENT '地区名称',
  `level`       tinyint      NOT NULL COMMENT '0省 1市 2区县 3乡镇',
  `create_time` datetime     NULL DEFAULT NULL,
  `update_time` datetime     NULL DEFAULT NULL,
  `is_delete`   datetime     NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_code_parent` (`code`, `parent_code`) USING BTREE,
  KEY `idx_parent_code` (`parent_code`) USING BTREE,
  KEY `idx_level` (`level`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '省市区地址' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
