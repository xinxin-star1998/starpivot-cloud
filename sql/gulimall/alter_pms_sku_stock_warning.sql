-- SKU 库存预警阈值（谷粒商城：可售库存低于阈值时自动生成采购需求）
USE `star_pivot_mall`;

ALTER TABLE `pms_sku_info`
    ADD COLUMN `stock_warning` int NULL DEFAULT NULL COMMENT '库存预警阈值' AFTER `sale_count`;
