-- =============================================================================

-- StarPivot 商城 - 菜单补充（写入系统库 star_pivot.sys_menu）

--

-- 前置: 已执行 ../star_pivot.sql（商城菜单 ID 以 180~210 为准，见 docs/mall.md）

-- 用途: 补全 perms 按钮权限、SKU/C 端入口、超级管理员授权

-- 说明: 勿再使用 200~210 作为新菜单 ID（与 star_pivot.sql 订单/会员/内容菜单冲突）

--

-- 用法: mysql -uroot -p star_pivot < init_mall_menus.sql

-- =============================================================================



USE `star_pivot`;



SET NAMES utf8mb4;



-- ----------------------------

-- 1. 页面菜单 perms（对齐 Controller @PreAuthorize）

-- ----------------------------



-- OMS 196~201

UPDATE `sys_menu` SET `perms` = 'mall:order:list',    `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 197 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:return:list',   `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 198 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:order:setting', `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 199;

UPDATE `sys_menu` SET `perms` = 'mall:payment:list',  `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 200 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:refund:list',   `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 201 AND (`perms` IS NULL OR `perms` = '');



-- SMS 187~195

UPDATE `sys_menu` SET `perms` = 'mall:coupon:list',       `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 188 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:coupon:history',   `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 189 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:subject:list',     `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 190 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:seckill:list',     `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 191 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:bounds:list',      `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 192 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:reduction:list',   `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 193 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:memberprice:list', `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 194 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:seckill:session',  `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 195 AND (`perms` IS NULL OR `perms` = '');



-- UMS 202~206

UPDATE `sys_menu` SET `perms` = 'mall:member:list',       `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 203 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:member:level',      `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 204 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:member:growth',     `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 205 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:member:statistics', `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 206 AND (`perms` IS NULL OR `perms` = '');



-- WMS 183~186

UPDATE `sys_menu` SET `perms` = 'mall:task:list',      `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 183 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:purchase:item',  `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 185 AND (`perms` IS NULL OR `perms` = '');

UPDATE `sys_menu` SET `perms` = 'mall:purchase:list',  `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 186 AND (`perms` IS NULL OR `perms` = '');



-- CMS 208~210（208/210 在 star_pivot 中可能已有 perms）

UPDATE `sys_menu` SET `perms` = 'mall:adv:list',     `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 208;

UPDATE `sys_menu` SET `remark` = '无独立表，引导至专题活动 sms_home_subject', `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 209;

UPDATE `sys_menu` SET `perms` = 'mall:comment:list', `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 210;



-- PMS 商品维护目录

UPDATE `sys_menu` SET `perms` = 'mall:product:list', `update_by` = 'admin', `update_time` = NOW() WHERE `menu_id` = 28 AND (`perms` IS NULL OR `perms` = '');



-- ----------------------------

-- 2. 新增页面菜单（ID > 210，避免与 star_pivot 冲突）

-- ----------------------------



-- C 端商城入口（parent_id = 4 商城系统）

INSERT INTO `sys_menu` (

  `menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`,

  `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,

  `create_by`, `create_time`, `update_by`, `update_time`, `remark`

)

SELECT 280, 'C端商城', 4, 0, '/portal', '', 'MallPortal',

       1, 1, 'C', '0', '0', '', 'ep:shopping-bag',

       'admin', NOW(), '', NULL, '打开 C 端商城前台'

FROM DUAL

WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 280);



-- SKU 管理（挂商品维护 parent_id=28）

INSERT INTO `sys_menu` (

  `menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`,

  `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`,

  `create_by`, `create_time`, `update_by`, `update_time`, `remark`

)

SELECT 281, 'SKU 管理', 28, 4, 'sku', '/mall/pms/sku/index', 'PmsSkuManager',

       1, 1, 'C', '0', '0', 'mall:product:list', 'mdi:barcode',

       'admin', NOW(), '', NULL, 'PMS SKU 独立管理'

FROM DUAL

WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 281);



-- ----------------------------

-- 3. 按钮权限（F，menu_id 282 起）

-- ----------------------------



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 282, '订单查询', 197, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:order:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 282);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 283, '订单发货', 197, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:order:deliver', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 283);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 284, '关闭订单', 197, 3, '', '', '', 1, 0, 'F', '0', '0', 'mall:order:close', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 284);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 285, '退货审核', 198, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:return:audit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 285);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 286, '退货查询', 198, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:return:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 286);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 287, '优惠券新增', 188, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:coupon:add', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 287);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 288, '优惠券修改', 188, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:coupon:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 288);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 289, '优惠券删除', 188, 3, '', '', '', 1, 0, 'F', '0', '0', 'mall:coupon:delete', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 289);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 290, '会员查询', 203, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:member:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 290);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 291, '会员修改', 203, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:member:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 291);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 292, '采购单查询', 186, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:purchase:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 292);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 293, '工作单处理', 183, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:task:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 293);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 294, '商品查询', 180, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:product:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 294);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 295, '商品新增', 180, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:product:add', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 295);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 296, '商品修改', 180, 3, '', '', '', 1, 0, 'F', '0', '0', 'mall:product:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 296);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 297, '商品删除', 180, 4, '', '', '', 1, 0, 'F', '0', '0', 'mall:product:delete', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 297);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 298, '轮播新增', 208, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:adv:add', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 298);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 299, '轮播修改', 208, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:adv:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 299);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 300, '轮播删除', 208, 3, '', '', '', 1, 0, 'F', '0', '0', 'mall:adv:delete', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 300);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 301, '评论查询', 210, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:comment:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 301);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 302, '评论修改', 210, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:comment:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 302);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 303, '评论删除', 210, 3, '', '', '', 1, 0, 'F', '0', '0', 'mall:comment:delete', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 303);



-- ----------------------------

-- 3.1 营销/仓储等模块按钮权限（menu_id 304 起）

-- ----------------------------



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 304, '积分查询', 192, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:bounds:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 304);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 305, '积分新增', 192, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:bounds:add', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 305);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 306, '积分修改', 192, 3, '', '', '', 1, 0, 'F', '0', '0', 'mall:bounds:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 306);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 307, '积分删除', 192, 4, '', '', '', 1, 0, 'F', '0', '0', 'mall:bounds:delete', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 307);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 308, '会员价查询', 194, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:memberprice:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 308);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 309, '会员价新增', 194, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:memberprice:add', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 309);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 310, '会员价修改', 194, 3, '', '', '', 1, 0, 'F', '0', '0', 'mall:memberprice:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 310);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 311, '会员价删除', 194, 4, '', '', '', 1, 0, 'F', '0', '0', 'mall:memberprice:delete', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 311);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 312, '满减查询', 193, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:reduction:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 312);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 313, '满减新增', 193, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:reduction:add', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 313);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 314, '满减修改', 193, 3, '', '', '', 1, 0, 'F', '0', '0', 'mall:reduction:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 314);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 315, '满减删除', 193, 4, '', '', '', 1, 0, 'F', '0', '0', 'mall:reduction:delete', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 315);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 316, '秒杀查询', 191, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:seckill:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 316);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 317, '秒杀新增', 191, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:seckill:add', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 317);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 318, '秒杀修改', 191, 3, '', '', '', 1, 0, 'F', '0', '0', 'mall:seckill:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 318);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 319, '秒杀删除', 191, 4, '', '', '', 1, 0, 'F', '0', '0', 'mall:seckill:delete', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 319);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 320, '场次新增', 195, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:seckill:session:add', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 320);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 321, '场次修改', 195, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:seckill:session:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 321);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 322, '场次删除', 195, 3, '', '', '', 1, 0, 'F', '0', '0', 'mall:seckill:session:delete', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 322);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 323, '专题查询', 190, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:subject:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 323);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 324, '专题新增', 190, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:subject:add', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 324);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 325, '专题修改', 190, 3, '', '', '', 1, 0, 'F', '0', '0', 'mall:subject:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 325);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 326, '专题删除', 190, 4, '', '', '', 1, 0, 'F', '0', '0', 'mall:subject:delete', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 326);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 327, '支付流水查询', 200, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:payment:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 327);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 328, '退款流水查询', 201, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:refund:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 328);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 329, '等级新增', 204, 1, '', '', '', 1, 0, 'F', '0', '0', 'mall:member:level:add', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 329);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 330, '等级修改', 204, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:member:level:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 330);

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 331, '等级删除', 204, 3, '', '', '', 1, 0, 'F', '0', '0', 'mall:member:level:delete', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 331);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 332, '采购单处理', 186, 2, '', '', '', 1, 0, 'F', '0', '0', 'mall:purchase:edit', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 332);



INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `route_name`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)

SELECT 333, '优惠券查询', 188, 4, '', '', '', 1, 0, 'F', '0', '0', 'mall:coupon:query', '#', 'admin', NOW(), '' FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `menu_id` = 333);



-- ----------------------------

-- 4. 超级管理员授权（role_id = 1）

-- ----------------------------



INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)

SELECT 1, m.`menu_id`

FROM `sys_menu` m

WHERE m.`menu_id` IN (

  180, 181, 182, 183, 184, 185, 186,

  187, 188, 189, 190, 191, 192, 193, 194, 195,

  196, 197, 198, 199, 200, 201,

  202, 203, 204, 205, 206,

  207, 208, 209, 210,

  280, 281,

  282, 283, 284, 285, 286,

  287, 288, 289,

  290, 291,

  292, 293,

  294, 295, 296, 297,

  298, 299, 300,

  301, 302, 303,

  304, 305, 306, 307,

  308, 309, 310, 311,

  312, 313, 314, 315,

  316, 317, 318, 319,

  320, 321, 322,

  323, 324, 325, 326,

  327, 328,

  329, 330, 331,

  332, 333

)

AND NOT EXISTS (

  SELECT 1 FROM `sys_role_menu` rm WHERE rm.`role_id` = 1 AND rm.`menu_id` = m.`menu_id`

);


