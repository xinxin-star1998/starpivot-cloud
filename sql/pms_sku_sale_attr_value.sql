/*
 SKU 销售属性种子数据（已迁移至新属性体系）

 对应 pms_attr.sql 中销售属性：
   66 机身颜色
   67 运行内存(RAM)   — 仅 Mate 30 Pro 等含「8GB+256GB」组合的 SKU
   68 机身存储(ROM)

 若从谷粒商城旧库升级，请改用 pms_sku_sale_attr_value_migrate.sql。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `pms_sku_sale_attr_value`;
CREATE TABLE `pms_sku_sale_attr_value` (
  `id`         bigint       NOT NULL AUTO_INCREMENT COMMENT 'id',
  `sku_id`     bigint       NULL DEFAULT NULL COMMENT 'sku_id',
  `attr_id`    bigint       NULL DEFAULT NULL COMMENT 'attr_id',
  `attr_name`  varchar(200) NULL DEFAULT NULL COMMENT '销售属性名',
  `attr_value` varchar(200) NULL DEFAULT NULL COMMENT '销售属性值',
  `attr_sort`  int          NULL DEFAULT NULL COMMENT '顺序',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 61 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'sku销售属性&值' ROW_FORMAT = Dynamic;

-- Mate 30 Pro (sku 1~8)：颜色 + 运行内存 + 机身存储
INSERT INTO `pms_sku_sale_attr_value` (`id`, `sku_id`, `attr_id`, `attr_name`, `attr_value`, `attr_sort`) VALUES
  ( 1,  1, 66, '机身颜色',      '星河银', 0),
  ( 2,  1, 67, '运行内存(RAM)', '8GB',    1),
  ( 3,  1, 68, '机身存储(ROM)', '256GB',  2),
  ( 4,  2, 66, '机身颜色',      '星河银', 0),
  ( 5,  2, 67, '运行内存(RAM)', '8GB',    1),
  ( 6,  2, 68, '机身存储(ROM)', '128GB',  2),
  ( 7,  3, 66, '机身颜色',      '亮黑色', 0),
  ( 8,  3, 67, '运行内存(RAM)', '8GB',    1),
  ( 9,  3, 68, '机身存储(ROM)', '256GB',  2),
  (10,  4, 66, '机身颜色',      '亮黑色', 0),
  (11,  4, 67, '运行内存(RAM)', '8GB',    1),
  (12,  4, 68, '机身存储(ROM)', '128GB',  2),
  (13,  5, 66, '机身颜色',      '翡冷翠', 0),
  (14,  5, 67, '运行内存(RAM)', '8GB',    1),
  (15,  5, 68, '机身存储(ROM)', '256GB',  2),
  (16,  6, 66, '机身颜色',      '翡冷翠', 0),
  (17,  6, 67, '运行内存(RAM)', '8GB',    1),
  (18,  6, 68, '机身存储(ROM)', '128GB',  2),
  (19,  7, 66, '机身颜色',      '罗兰紫', 0),
  (20,  7, 67, '运行内存(RAM)', '8GB',    1),
  (21,  7, 68, '机身存储(ROM)', '256GB',  2),
  (22,  8, 66, '机身颜色',      '罗兰紫', 0),
  (23,  8, 67, '运行内存(RAM)', '8GB',    1),
  (24,  8, 68, '机身存储(ROM)', '128GB',  2);

-- iPhone 11 (sku 9~26)：颜色 + 机身存储
INSERT INTO `pms_sku_sale_attr_value` (`id`, `sku_id`, `attr_id`, `attr_name`, `attr_value`, `attr_sort`) VALUES
  (25,  9, 66, '机身颜色',      '黑色', 0),
  (26,  9, 68, '机身存储(ROM)', '128GB', 1),
  (27, 10, 66, '机身颜色',      '黑色', 0),
  (28, 10, 68, '机身存储(ROM)', '256GB', 1),
  (29, 11, 66, '机身颜色',      '黑色', 0),
  (30, 11, 68, '机身存储(ROM)', '64GB',  1),
  (31, 12, 66, '机身颜色',      '白色', 0),
  (32, 12, 68, '机身存储(ROM)', '128GB', 1),
  (33, 13, 66, '机身颜色',      '白色', 0),
  (34, 13, 68, '机身存储(ROM)', '256GB', 1),
  (35, 14, 66, '机身颜色',      '白色', 0),
  (36, 14, 68, '机身存储(ROM)', '64GB',  1),
  (37, 15, 66, '机身颜色',      '绿色', 0),
  (38, 15, 68, '机身存储(ROM)', '128GB', 1),
  (39, 16, 66, '机身颜色',      '绿色', 0),
  (40, 16, 68, '机身存储(ROM)', '256GB', 1),
  (41, 17, 66, '机身颜色',      '绿色', 0),
  (42, 17, 68, '机身存储(ROM)', '64GB',  1),
  (43, 18, 66, '机身颜色',      '黄色', 0),
  (44, 18, 68, '机身存储(ROM)', '128GB', 1),
  (45, 19, 66, '机身颜色',      '黄色', 0),
  (46, 19, 68, '机身存储(ROM)', '256GB', 1),
  (47, 20, 66, '机身颜色',      '黄色', 0),
  (48, 20, 68, '机身存储(ROM)', '64GB',  1),
  (49, 21, 66, '机身颜色',      '红色', 0),
  (50, 21, 68, '机身存储(ROM)', '128GB', 1),
  (51, 22, 66, '机身颜色',      '红色', 0),
  (52, 22, 68, '机身存储(ROM)', '256GB', 1),
  (53, 23, 66, '机身颜色',      '红色', 0),
  (54, 23, 68, '机身存储(ROM)', '64GB',  1),
  (55, 24, 66, '机身颜色',      '紫色', 0),
  (56, 24, 68, '机身存储(ROM)', '128GB', 1),
  (57, 25, 66, '机身颜色',      '紫色', 0),
  (58, 25, 68, '机身存储(ROM)', '256GB', 1),
  (59, 26, 66, '机身颜色',      '紫色', 0),
  (60, 26, 68, '机身存储(ROM)', '64GB',  1);

SET FOREIGN_KEY_CHECKS = 1;
