-- =============================================================================

-- 商城菜单修正（已有 star_pivot 库执行）

-- 用途：对齐 perms/备注；清理旧版 init_mall_menus 错误 ID

-- 用法：

--   mysql -uroot -p star_pivot < migrate_mall_menus_v2.sql

--   mysql -uroot -p star_pivot < init_mall_menus.sql

--

-- 菜单 ID 以 star_pivot.sql 为准（180~210），扩展 ID：280 C端、281 SKU、282~303 按钮

-- =============================================================================



USE `star_pivot`;



SET NAMES utf8mb4;



-- 订单设置（menu 199）

UPDATE `sys_menu`

SET `menu_name` = '订单设置',

    `perms`     = 'mall:order:setting',

    `remark`    = 'oms_order_setting 超时与自动确认规则',

    `update_by` = 'admin',

    `update_time` = NOW()

WHERE `menu_id` = 199;



-- 首页轮播（menu 208）

UPDATE `sys_menu`

SET `menu_name` = '首页轮播',

    `perms`     = 'mall:adv:list',

    `remark`    = 'sms_home_adv 首页广告',

    `update_by` = 'admin',

    `update_time` = NOW()

WHERE `menu_id` = 208;



-- 分类热门说明（menu 209）

UPDATE `sys_menu`

SET `remark` = '无独立表，引导至专题活动 sms_home_subject',

    `update_by` = 'admin',

    `update_time` = NOW()

WHERE `menu_id` = 209;



-- 清理旧版 init_mall_menus.sql 误插入的冲突菜单（220~275 段）

DELETE rm FROM `sys_role_menu` rm

INNER JOIN `sys_menu` m ON rm.`menu_id` = m.`menu_id`

WHERE m.`menu_id` BETWEEN 220 AND 275

  AND m.`menu_name` IN (

    '营销中心', '优惠券管理', '发放记录', '专题活动', '秒杀活动', '秒杀场次',

    '积分维护', '满减折扣', '会员价格', '优惠券新增', '优惠券修改', '优惠券删除',

    '会员中心', '会员列表', '会员等级', '积分成长', '统计信息', '会员查询', '会员修改',

    '内容运营', '首页轮播', '评论管理', '分类热门',

    '采购管理', '采购需求', '采购单', '库存工作单', '采购单查询', '工作单处理'

  );



DELETE FROM `sys_menu`

WHERE `menu_id` BETWEEN 220 AND 275

  AND `menu_name` IN (

    '营销中心', '优惠券管理', '发放记录', '专题活动', '秒杀活动', '秒杀场次',

    '积分维护', '满减折扣', '会员价格', '优惠券新增', '优惠券修改', '优惠券删除',

    '会员中心', '会员列表', '会员等级', '积分成长', '统计信息', '会员查询', '会员修改',

    '内容运营', '首页轮播', '评论管理', '分类热门',

    '采购管理', '采购需求', '采购单', '库存工作单', '采购单查询', '工作单处理'

  );



-- 若旧脚本覆盖了 200（应为支付流水），恢复 star_pivot 定义

UPDATE `sys_menu`

SET `menu_name` = '支付流水查询',

    `parent_id` = 196,

    `order_num` = 4,

    `path` = 'payment',

    `component` = '/mall/oms/order/payment/index',

    `route_name` = 'PaymentLog',

    `menu_type` = 'C',

    `visible` = '0',

    `status` = '0',

    `perms` = 'mall:payment:list',

    `icon` = '#',

    `update_by` = 'admin',

    `update_time` = NOW()

WHERE `menu_id` = 200 AND `menu_name` <> '支付流水查询';



-- 执行完本脚本后，请继续执行 init_mall_menus.sql（补按钮权限与授权）


