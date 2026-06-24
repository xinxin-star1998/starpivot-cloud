-- =============================================================================
-- 创建商城独立库 star_pivot_mall
-- 系统库 star_pivot：用户/角色/菜单（star_pivot.sql）
-- 商城库 star_pivot_mall：PMS/OMS/SMS/UMS/WMS 等业务表
-- =============================================================================

CREATE DATABASE IF NOT EXISTS `star_pivot_mall`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `star_pivot_mall`;
