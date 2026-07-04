-- 将审批业务表从 star_pivot 迁移至独立库 star_pivot_approval（已有环境一次性执行）
-- 用法：先执行本脚本迁移数据，再更新 Nacos starpivot-approval.yaml 指向 star_pivot_approval

CREATE DATABASE IF NOT EXISTS `star_pivot_approval`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 若 star_pivot_approval 尚无表结构，请先导入 sql/star_pivot_approval.sql

INSERT INTO star_pivot_approval.ap_instance SELECT * FROM star_pivot.ap_instance;
INSERT INTO star_pivot_approval.ap_notification SELECT * FROM star_pivot.ap_notification;
INSERT INTO star_pivot_approval.ap_record SELECT * FROM star_pivot.ap_record;
INSERT INTO star_pivot_approval.ap_task SELECT * FROM star_pivot.ap_task;
INSERT INTO star_pivot_approval.ap_template SELECT * FROM star_pivot.ap_template;
INSERT INTO star_pivot_approval.ap_template_bind SELECT * FROM star_pivot.ap_template_bind;
INSERT INTO star_pivot_approval.ap_template_route SELECT * FROM star_pivot.ap_template_route;
INSERT INTO star_pivot_approval.ap_template_step SELECT * FROM star_pivot.ap_template_step;

-- 验证无误后，从 star_pivot 删除审批表（按需取消注释）
-- DROP TABLE IF EXISTS star_pivot.ap_instance;
-- DROP TABLE IF EXISTS star_pivot.ap_notification;
-- DROP TABLE IF EXISTS star_pivot.ap_record;
-- DROP TABLE IF EXISTS star_pivot.ap_task;
-- DROP TABLE IF EXISTS star_pivot.ap_template;
-- DROP TABLE IF EXISTS star_pivot.ap_template_bind;
-- DROP TABLE IF EXISTS star_pivot.ap_template_route;
-- DROP TABLE IF EXISTS star_pivot.ap_template_step;
