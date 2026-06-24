-- =============================================================================
-- 创建商城独立库（Docker 首次启动时自动执行）
-- 业务表脚本位于 sql/gulimall/，需手动执行 init_mall_all.sql 导入表结构与数据
-- 菜单权限仍在 star_pivot.sys_menu，通过 init_mall_menus.sql 写入
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `star_pivot_mall`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
