/*
 StarPivot 营销演示数据（轮播 + 首页专题，配合商品演示数据）
 目标库：star_pivot_promotion

 导入：
   mysql -h127.0.0.1 -P3307 -uroot -proot star_pivot_promotion < sql/seed_promotion_demo.sql
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 轮播（长期有效）
DELETE FROM `sms_home_adv` WHERE `id` BETWEEN 10 AND 19;
INSERT INTO `sms_home_adv` (`id`, `name`, `pic`, `start_time`, `end_time`, `status`, `click_count`, `url`, `note`, `sort`, `publisher_id`, `auth_id`) VALUES
(10, '华为 Mate 70 Pro 新品首发', 'file/goods/7/2026/06/24/a951dc27-c51c-494b-bb52-12e70da3d0fd.jpg', NULL, NULL, 1, 0, '/portal/product/101', '演示轮播', 1, NULL, NULL),
(11, '小米 15 Ultra 影像旗舰', 'file/goods/7/2026/07/04/200dd4c0-6dc8-4f2b-ae3b-65dd439896a2.jpg', NULL, NULL, 1, 0, '/portal/product/102', '演示轮播', 2, NULL, NULL),
(12, 'iPhone 16 Pro 官方正品', 'file/goods/7/2026/06/24/f974367a-2602-4eff-9839-df8a75a31cc8.jpg', NULL, NULL, 1, 0, '/portal/product/103', '演示轮播', 3, NULL, NULL);

-- 首页专题商品（家电焕新季 → 关联新 SPU）
DELETE FROM `sms_home_subject_spu` WHERE `id` BETWEEN 10 AND 19;
INSERT INTO `sms_home_subject_spu` (`id`, `name`, `subject_id`, `spu_id`, `sort`) VALUES
(10, '海尔 481L 法式冰箱', 1, 111, 1),
(11, '美的 1.5匹 风尊空调', 1, 112, 2),
(12, '戴森 V12 吸尘器', 1, 113, 3),
(13, '徕芬 LF03 吹风机', 1, 115, 4);

SET FOREIGN_KEY_CHECKS = 1;
