/*
 StarPivot 商品 / 品牌演示数据（2025-2026 真实型号风格）
 目标库：star_pivot_product

 导入：
   mysql -h127.0.0.1 -P3307 -uroot -proot star_pivot_product < sql/seed_product_brand_demo.sql

 库存（需另导入 ware 库）：
   mysql -h127.0.0.1 -P3307 -uroot -proot star_pivot_ware < sql/seed_product_brand_demo_ware.sql

 说明：可重复执行（会先清理本脚本写入的 ID 段）；图片路径复用项目已有 OSS 文件。
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ========== 清理（仅本脚本 ID 段）==========
DELETE FROM pms_sku_sale_attr_value WHERE id BETWEEN 300 AND 399;
DELETE FROM pms_sku_images WHERE id BETWEEN 300 AND 399;
DELETE FROM pms_spu_images WHERE id BETWEEN 200 AND 299;
DELETE FROM pms_spu_info_desc WHERE spu_id BETWEEN 101 AND 115;
DELETE FROM pms_sku_info WHERE sku_id BETWEEN 201 AND 219;
DELETE FROM pms_spu_info WHERE id BETWEEN 101 AND 115;
DELETE FROM pms_category_brand_relation WHERE id BETWEEN 50 AND 99;
DELETE FROM pms_brand WHERE brand_id BETWEEN 13 AND 26;

-- ========== 品牌（14 个）==========
INSERT INTO `pms_brand` (`brand_id`, `name`, `logo`, `descript`, `show_status`, `first_letter`, `sort`) VALUES
(13, 'vivo', 'file/goods/7/2026/06/24/92528f97-af90-4483-838f-bf4c013ad490.png', 'vivo 专注影像与快充的智能手机品牌', 1, 'V', 2),
(14, '三星', 'file/goods/7/2026/06/24/c438169e-e441-47c0-9730-1d126298f5bb.png', 'Samsung 盖乐世系列旗舰手机与消费电子', 1, 'S', 3),
(15, '荣耀', 'file/goods/7/2026/06/24/8d0c6883-6e19-4b23-ac40-4d4634e3f738.png', 'HONOR 荣耀数字与 Magic 系列智能手机', 1, 'R', 4),
(16, '联想', 'file/goods/7/2026/06/24/50c9014f-3529-44ff-b493-e10d4436c310.png', 'Lenovo 拯救者游戏本与 Think 系列笔记本', 1, 'L', 5),
(17, '戴尔', 'file/goods/7/2026/06/24/50c9014f-3529-44ff-b493-e10d4436c310.png', 'Dell 灵越与 XPS 系列笔记本电脑', 1, 'D', 6),
(18, '海尔', 'file/goods/7/2026/06/24/50c9014f-3529-44ff-b493-e10d4436c310.png', 'Haier 冰箱、洗衣机等全屋家电', 1, 'H', 7),
(19, '美的', 'file/goods/7/2026/06/24/50c9014f-3529-44ff-b493-e10d4436c310.png', 'Midea 空调、厨电与生活电器', 1, 'M', 8),
(20, '戴森', 'file/goods/7/2026/06/24/50c9014f-3529-44ff-b493-e10d4436c310.png', 'Dyson 吸尘器、吹风机等高端个护电器', 1, 'Y', 9),
(21, '索尼', 'file/goods/7/2026/06/24/c438169e-e441-47c0-9730-1d126298f5bb.png', 'Sony WH-1000 系列降噪耳机与影像设备', 1, 'S', 10),
(22, '耐克', 'file/goods/7/2026/06/24/92528f97-af90-4483-838f-bf4c013ad490.png', 'Nike 跑步鞋与运动装备', 1, 'N', 11),
(23, '阿迪达斯', 'file/goods/7/2026/06/24/92528f97-af90-4483-838f-bf4c013ad490.png', 'adidas Ultraboost 等经典跑鞋系列', 1, 'A', 12),
(24, '徕芬', 'file/goods/7/2026/06/24/50c9014f-3529-44ff-b493-e10d4436c310.png', 'Laifen 高速吹风机与个护小家电', 1, 'F', 13),
(25, 'realme', 'file/goods/7/2026/06/24/92528f97-af90-4483-838f-bf4c013ad490.png', 'realme GT 系列性能旗舰手机', 1, 'E', 14),
(26, '一加', 'file/goods/7/2026/06/24/92528f97-af90-4483-838f-bf4c013ad490.png', 'OnePlus 数字系列旗舰智能手机', 1, 'O', 15);

-- 修正已有品牌展示名
UPDATE `pms_brand` SET `name` = 'OPPO', `descript` = 'OPPO Find 系列影像旗舰手机' WHERE `brand_id` = 11;

-- ========== 分类-品牌关联 ==========
INSERT INTO `pms_category_brand_relation` (`id`, `brand_id`, `catelog_id`, `brand_name`, `catelog_name`) VALUES
(50, 13, 225, 'vivo', '手机'),
(51, 14, 225, '三星', '手机'),
(52, 15, 225, '荣耀', '手机'),
(53, 25, 225, 'realme', '手机'),
(54, 26, 225, '一加', '手机'),
(55, 16, 449, '联想', '笔记本'),
(56, 17, 449, '戴尔', '笔记本'),
(57, 21, 233, '索尼', '蓝牙耳机'),
(58, 9, 233, '华为', '蓝牙耳机'),
(59, 18, 252, '海尔', '冰箱'),
(60, 19, 251, '美的', '空调'),
(61, 20, 294, '戴森', '吸尘器'),
(62, 24, 41, '徕芬', '个护健康'),
(63, 22, 1161, '耐克', '跑步鞋'),
(64, 23, 1161, '阿迪达斯', '跑步鞋'),
(65, 10, 452, '小米', '平板电脑');

-- ========== SPU（15 款，均已上架且审批通过）==========
INSERT INTO `pms_spu_info` (`id`, `spu_name`, `spu_description`, `catalog_id`, `brand_id`, `weight`, `publish_status`, `approval_instance_id`, `audit_status`, `create_time`, `update_time`) VALUES
(101, '华为 Mate 70 Pro', '鸿蒙 AI 旗舰 · 超光变 XMAGE 影像', 225, 9, 0.2210, 1, NULL, 'APPROVED', '2025-11-20 10:00:00', '2026-07-07 12:00:00'),
(102, '小米 15 Ultra', '徕卡四摄 · 骁龙 8 Elite 性能旗舰', 225, 10, 0.2260, 1, NULL, 'APPROVED', '2025-12-01 10:00:00', '2026-07-07 12:00:00'),
(103, 'Apple iPhone 16 Pro', 'A18 Pro 芯片 · 钛金属机身 · 4800 万像素', 225, 12, 0.1990, 1, NULL, 'APPROVED', '2025-09-15 10:00:00', '2026-07-07 12:00:00'),
(104, 'OPPO Find X8 Pro', '双潜望长焦 · 天玑 9400 影像旗舰', 225, 11, 0.2150, 1, NULL, 'APPROVED', '2025-10-18 10:00:00', '2026-07-07 12:00:00'),
(105, 'vivo X200 Pro', '蔡司 2 亿像素 · 蓝海续航系统', 225, 13, 0.2230, 1, NULL, 'APPROVED', '2025-10-25 10:00:00', '2026-07-07 12:00:00'),
(106, '三星 Galaxy S25 Ultra', 'Galaxy AI · 2 亿像素主摄 · S Pen', 225, 14, 0.2330, 1, NULL, 'APPROVED', '2025-01-28 10:00:00', '2026-07-07 12:00:00'),
(107, '荣耀 Magic7 Pro', 'AI 鹰眼相机 · 第三代青海湖电池', 225, 15, 0.2230, 1, NULL, 'APPROVED', '2025-11-08 10:00:00', '2026-07-07 12:00:00'),
(108, '联想 拯救者 Y9000P 2025', 'i9-14900HX · RTX 4060 · 2.5K 240Hz', 449, 16, 2.5800, 1, NULL, 'APPROVED', '2025-03-10 10:00:00', '2026-07-07 12:00:00'),
(109, '戴尔 灵越 14 Plus', 'Ultra 7 155H · 2.8K OLED · 轻至 1.26kg', 449, 17, 1.2600, 1, NULL, 'APPROVED', '2025-04-12 10:00:00', '2026-07-07 12:00:00'),
(110, '索尼 WH-1000XM5', '旗舰降噪耳机 · 30 小时续航', 233, 21, 0.2500, 1, NULL, 'APPROVED', '2025-02-20 10:00:00', '2026-07-07 12:00:00'),
(111, '海尔 481L 法式多门冰箱', '一级能效 · 风冷无霜 · 干湿分储', 252, 18, 85.0000, 1, NULL, 'APPROVED', '2025-05-06 10:00:00', '2026-07-07 12:00:00'),
(112, '美的 1.5匹 风尊空调', '新一级能效 · 全直流变频 · 防直吹', 251, 19, 12.0000, 1, NULL, 'APPROVED', '2025-05-20 10:00:00', '2026-07-07 12:00:00'),
(113, '戴森 V12 Detect Slim', '激光探测 · 轻量无绳吸尘器', 294, 20, 1.5000, 1, NULL, 'APPROVED', '2025-06-01 10:00:00', '2026-07-07 12:00:00'),
(114, '耐克 Air Zoom Pegasus 41', '日常训练跑鞋 · ReactX 缓震', 1161, 22, 0.2800, 1, NULL, 'APPROVED', '2025-07-15 10:00:00', '2026-07-07 12:00:00'),
(115, '徕芬 LF03 高速吹风机', '11 万转马达 · 负离子护发', 41, 24, 0.4100, 1, NULL, 'APPROVED', '2025-08-01 10:00:00', '2026-07-07 12:00:00');

-- 顺带修正旧数据：小米 promax 审批通过以便前台展示
UPDATE `pms_spu_info` SET `audit_status` = 'APPROVED', `approval_instance_id` = NULL WHERE `id` = 14;

-- ========== SPU 详情图 ==========
INSERT INTO `pms_spu_info_desc` (`spu_id`, `decript`) VALUES
(101, 'file/goods/7/2026/06/24/2fce83b3-d30d-4d2b-8ea0-9b470c3e9aba.jpg'),
(102, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg'),
(103, 'file/goods/7/2026/06/24/64c4ac0c-539d-4e98-a406-364908c2e436.jpg'),
(104, 'file/goods/7/2026/06/24/2fce83b3-d30d-4d2b-8ea0-9b470c3e9aba.jpg'),
(105, 'file/goods/7/2026/06/24/2fce83b3-d30d-4d2b-8ea0-9b470c3e9aba.jpg'),
(106, 'file/goods/7/2026/06/24/64c4ac0c-539d-4e98-a406-364908c2e436.jpg'),
(107, 'file/goods/7/2026/06/24/2fce83b3-d30d-4d2b-8ea0-9b470c3e9aba.jpg'),
(108, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg'),
(109, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg'),
(110, 'file/goods/7/2026/06/24/64c4ac0c-539d-4e98-a406-364908c2e436.jpg'),
(111, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg'),
(112, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg'),
(113, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg'),
(114, 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg'),
(115, 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg');

-- ========== SPU 主图 ==========
INSERT INTO `pms_spu_images` (`id`, `spu_id`, `img_name`, `img_url`, `img_sort`, `default_img`) VALUES
(200, 101, NULL, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', 0, 1),
(201, 102, NULL, 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', 0, 1),
(202, 103, NULL, 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 0, 1),
(203, 104, NULL, 'file/goods/7/2026/06/24/92528f97-af90-4483-838f-bf4c013ad490.png', 0, 1),
(204, 105, NULL, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', 0, 1),
(205, 106, NULL, 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 0, 1),
(206, 107, NULL, 'file/goods/7/2026/06/24/8d0c6883-6e19-4b23-ac40-4d4634e3f738.png', 0, 1),
(207, 108, NULL, 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', 0, 1),
(208, 109, NULL, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg', 0, 1),
(209, 110, NULL, 'file/goods/7/2026/06/24/64c4ac0c-539d-4e98-a406-364908c2e436.jpg', 0, 1),
(210, 111, NULL, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg', 0, 1),
(211, 112, NULL, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg', 0, 1),
(212, 113, NULL, 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg', 0, 1),
(213, 114, NULL, 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg', 0, 1),
(214, 115, NULL, 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg', 0, 1);

-- ========== SKU ==========
INSERT INTO `pms_sku_info` (`sku_id`, `spu_id`, `sku_name`, `sku_desc`, `catalog_id`, `brand_id`, `sku_default_img`, `sku_title`, `sku_subtitle`, `price`, `sale_count`, `stock_warning`) VALUES
(201, 101, '华为 Mate 70 Pro 玄黑 12GB+512GB', NULL, 225, 9, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', '华为 Mate 70 Pro 玄黑 12GB+512GB', '鸿蒙 AI 旗舰 · 现货发售', 6999.0000, 1280, 10),
(202, 101, '华为 Mate 70 Pro 雪域白 12GB+256GB', NULL, 225, 9, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', '华为 Mate 70 Pro 雪域白 12GB+256GB', '鸿蒙 AI 旗舰 · 12 期免息', 6499.0000, 860, 10),
(203, 102, '小米 15 Ultra 黑色 16GB+512GB', NULL, 225, 10, 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', '小米 15 Ultra 黑色 16GB+512GB', '徕卡四摄 · 骁龙 8 Elite', 6499.0000, 2100, 10),
(204, 102, '小米 15 Ultra 白色 12GB+256GB', NULL, 225, 10, 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', '小米 15 Ultra 白色 12GB+256GB', '影像旗舰 · 限时直降', 5999.0000, 1560, 10),
(205, 103, 'Apple iPhone 16 Pro 黑色钛金属 256GB', NULL, 225, 12, 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 'Apple iPhone 16 Pro 黑色钛金属 256GB', 'A18 Pro · 官方正品', 8999.0000, 3200, 10),
(206, 103, 'Apple iPhone 16 Pro 原色钛金属 512GB', NULL, 225, 12, 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 'Apple iPhone 16 Pro 原色钛金属 512GB', 'A18 Pro · 顺丰包邮', 9999.0000, 980, 10),
(207, 104, 'OPPO Find X8 Pro 漫步云端 16GB+512GB', NULL, 225, 11, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', 'OPPO Find X8 Pro 漫步云端 16GB+512GB', '双潜望长焦 · 影像旗舰', 5499.0000, 760, 10),
(208, 104, 'OPPO Find X8 Pro 星野黑 12GB+256GB', NULL, 225, 11, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', 'OPPO Find X8 Pro 星野黑 12GB+256GB', '天玑 9400 · 24 期免息', 4999.0000, 540, 10),
(209, 105, 'vivo X200 Pro 钛色 16GB+512GB', NULL, 225, 13, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', 'vivo X200 Pro 钛色 16GB+512GB', '蔡司 2 亿像素 · 蓝海续航', 5299.0000, 890, 10),
(210, 106, '三星 Galaxy S25 Ultra 钛灰 12GB+256GB', NULL, 225, 14, 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', '三星 Galaxy S25 Ultra 钛灰 12GB+256GB', 'Galaxy AI · S Pen 内置', 9699.0000, 430, 10),
(211, 107, '荣耀 Magic7 Pro 月影灰 12GB+512GB', NULL, 225, 15, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', '荣耀 Magic7 Pro 月影灰 12GB+512GB', 'AI 鹰眼相机 · 青海湖电池', 5699.0000, 670, 10),
(212, 108, '联想 拯救者 Y9000P i9/32G/1TB/RTX4060', NULL, 449, 16, 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', '联想 拯救者 Y9000P i9/32G/1TB/RTX4060', '2.5K 240Hz 电竞屏', 9999.0000, 320, 5),
(213, 109, '戴尔 灵越 14 Plus Ultra7/16G/512G', NULL, 449, 17, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg', '戴尔 灵越 14 Plus Ultra7/16G/512G', '2.8K OLED · 轻薄办公', 5999.0000, 210, 5),
(214, 110, '索尼 WH-1000XM5 黑色', NULL, 233, 21, 'file/goods/7/2026/06/24/64c4ac0c-539d-4e98-a406-364908c2e436.jpg', '索尼 WH-1000XM5 黑色', '旗舰降噪 · 30 小时续航', 2299.0000, 4500, 20),
(215, 111, '海尔 481L 法式多门冰箱 星蕴银', NULL, 252, 18, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg', '海尔 481L 法式多门冰箱 星蕴银', '一级能效 · 以旧换新补贴', 4599.0000, 180, 5),
(216, 112, '美的 1.5匹 风尊空调 KFR-35GW', NULL, 251, 19, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg', '美的 1.5匹 风尊空调 KFR-35GW', '新一级 · 包安装', 3299.0000, 920, 5),
(217, 113, '戴森 V12 Detect Slim 金色', NULL, 294, 20, 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg', '戴森 V12 Detect Slim 金色', '激光探测 · 轻量无绳', 3999.0000, 680, 10),
(218, 114, '耐克 Pegasus 41 男款 42码 黑', NULL, 1161, 22, 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg', '耐克 Pegasus 41 男款 42码 黑', 'ReactX 缓震 · 日常训练', 899.0000, 1200, 30),
(219, 115, '徕芬 LF03 星空蓝', NULL, 41, 24, 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg', '徕芬 LF03 星空蓝', '11 万转 · 负离子护发', 599.0000, 8600, 50);

-- ========== SKU 图 ==========
INSERT INTO `pms_sku_images` (`id`, `sku_id`, `img_url`, `img_sort`, `default_img`) VALUES
(300, 201, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', 0, 1),
(301, 202, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', 0, 1),
(302, 203, 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', 0, 1),
(303, 204, 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', 0, 1),
(304, 205, 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 0, 1),
(305, 206, 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 0, 1),
(306, 207, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', 0, 1),
(307, 208, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', 0, 1),
(308, 209, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', 0, 1),
(309, 210, 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', 0, 1),
(310, 211, 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', 0, 1),
(311, 212, 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', 0, 1),
(312, 213, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg', 0, 1),
(313, 214, 'file/goods/7/2026/06/24/64c4ac0c-539d-4e98-a406-364908c2e436.jpg', 0, 1),
(314, 215, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg', 0, 1),
(315, 216, 'file/goods/7/2026/07/04/7d73dd14-f66f-4df5-a2f2-f2d68a143bf7.jpg', 0, 1),
(316, 217, 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg', 0, 1),
(317, 218, 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg', 0, 1),
(318, 219, 'file/goods/7/2026/06/24/5d4e179a-f937-4280-bbc7-5b583ef2e2ad.jpg', 0, 1);

-- ========== 手机 SKU 销售属性 ==========
INSERT INTO `pms_sku_sale_attr_value` (`id`, `sku_id`, `attr_id`, `attr_name`, `attr_value`, `attr_sort`) VALUES
(300, 201, 66, '机身颜色', '玄黑', 0), (301, 201, 67, '运行内存(RAM)', '12GB', 1), (302, 201, 68, '机身存储(ROM)', '512GB', 2),
(303, 202, 66, '机身颜色', '雪域白', 0), (304, 202, 67, '运行内存(RAM)', '12GB', 1), (305, 202, 68, '机身存储(ROM)', '256GB', 2),
(306, 203, 66, '机身颜色', '黑色', 0), (307, 203, 67, '运行内存(RAM)', '16GB', 1), (308, 203, 68, '机身存储(ROM)', '512GB', 2),
(309, 204, 66, '机身颜色', '白色', 0), (310, 204, 67, '运行内存(RAM)', '12GB', 1), (311, 204, 68, '机身存储(ROM)', '256GB', 2),
(312, 205, 66, '机身颜色', '黑色', 0), (313, 205, 68, '机身存储(ROM)', '256GB', 1),
(314, 206, 66, '机身颜色', '原色钛金属', 0), (315, 206, 68, '机身存储(ROM)', '512GB', 1),
(316, 207, 66, '机身颜色', '漫步云端', 0), (317, 207, 67, '运行内存(RAM)', '16GB', 1), (318, 207, 68, '机身存储(ROM)', '512GB', 2),
(319, 208, 66, '机身颜色', '星野黑', 0), (320, 208, 67, '运行内存(RAM)', '12GB', 1), (321, 208, 68, '机身存储(ROM)', '256GB', 2),
(322, 209, 66, '机身颜色', '钛色', 0), (323, 209, 67, '运行内存(RAM)', '16GB', 1), (324, 209, 68, '机身存储(ROM)', '512GB', 2),
(325, 210, 66, '机身颜色', '钛灰', 0), (326, 210, 67, '运行内存(RAM)', '12GB', 1), (327, 210, 68, '机身存储(ROM)', '256GB', 2),
(328, 211, 66, '机身颜色', '月影灰', 0), (329, 211, 67, '运行内存(RAM)', '12GB', 1), (330, 211, 68, '机身存储(ROM)', '512GB', 2);

SET FOREIGN_KEY_CHECKS = 1;
