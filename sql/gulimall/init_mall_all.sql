-- =============================================================================
-- StarPivot 商城 - 一键初始化
--
-- 库划分:
--   star_pivot      系统库（用户/角色/菜单），先执行 ../star_pivot.sql
--   star_pivot_mall 商城业务库，由本脚本导入
--
-- 用法（在 sql/gulimall 目录下）:
--   mysql -uroot -p < init_mall_all.sql
--
-- 或在 mysql 客户端:
--   source init_mall_all.sql;
-- =============================================================================

SOURCE 00_star_pivot_mall.sql;
SOURCE gulimall_undo_log.sql;
SOURCE gulimall_address.sql;
-- 可选：导入完整省市区数据（体积较大，首次初始化建议单独执行）
-- SOURCE gulimall_address_data.sql;
SOURCE gulimall_pms.sql;
SOURCE gulimall_oms.sql;
SOURCE gulimall_sms.sql;
SOURCE gulimall_ums.sql;
SOURCE gulimall_wms.sql;

-- 菜单写入系统库 star_pivot（与业务库分离）
USE `star_pivot`;
SOURCE init_mall_menus.sql;

-- 已有库若曾执行旧版 init_mall_menus（200~275 ID 冲突），请额外执行：
-- SOURCE migrate_mall_menus_v2.sql;
-- SOURCE init_mall_menus.sql;
