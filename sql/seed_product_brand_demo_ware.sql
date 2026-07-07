/*
 StarPivot 演示商品库存（配合 seed_product_brand_demo.sql）
 目标库：star_pivot_ware

 导入：
   mysql -h127.0.0.1 -P3307 -uroot -proot star_pivot_ware < sql/seed_product_brand_demo_ware.sql
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM `wms_ware_sku` WHERE `id` BETWEEN 100 AND 138;

INSERT INTO `wms_ware_sku` (`id`, `sku_id`, `ware_id`, `stock`, `sku_name`, `stock_locked`) VALUES
(100, 201, 1, 120, '华为 Mate 70 Pro 玄黑 12GB+512GB', 0),
(101, 202, 1, 80, '华为 Mate 70 Pro 雪域白 12GB+256GB', 0),
(102, 203, 1, 200, '小米 15 Ultra 黑色 16GB+512GB', 0),
(103, 204, 2, 150, '小米 15 Ultra 白色 12GB+256GB', 0),
(104, 205, 1, 300, 'Apple iPhone 16 Pro 黑色钛金属 256GB', 0),
(105, 206, 2, 100, 'Apple iPhone 16 Pro 原色钛金属 512GB', 0),
(106, 207, 1, 90, 'OPPO Find X8 Pro 漫步云端 16GB+512GB', 0),
(107, 208, 2, 70, 'OPPO Find X8 Pro 星野黑 12GB+256GB', 0),
(108, 209, 1, 110, 'vivo X200 Pro 钛色 16GB+512GB', 0),
(109, 210, 2, 60, '三星 Galaxy S25 Ultra 钛灰 12GB+256GB', 0),
(110, 211, 1, 85, '荣耀 Magic7 Pro 月影灰 12GB+512GB', 0),
(111, 212, 1, 40, '联想 拯救者 Y9000P i9/32G/1TB/RTX4060', 0),
(112, 213, 2, 35, '戴尔 灵越 14 Plus Ultra7/16G/512G', 0),
(113, 214, 1, 500, '索尼 WH-1000XM5 黑色', 0),
(114, 215, 2, 25, '海尔 481L 法式多门冰箱 星蕴银', 0),
(115, 216, 3, 40, '美的 1.5匹 风尊空调 KFR-35GW', 0),
(116, 217, 1, 55, '戴森 V12 Detect Slim 金色', 0),
(117, 218, 1, 200, '耐克 Pegasus 41 男款 42码 黑', 0),
(118, 219, 2, 800, '徕芬 LF03 星空蓝', 0);

SET FOREIGN_KEY_CHECKS = 1;
