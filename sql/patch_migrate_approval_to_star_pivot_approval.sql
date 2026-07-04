-- 将审批业务表从 star_pivot 迁移至独立库 star_pivot_approval（已有环境一次性执行，可重复运行）
-- 用法：先执行本脚本迁移数据，再更新 Nacos starpivot-approval.yaml 指向 star_pivot_approval

CREATE DATABASE IF NOT EXISTS `star_pivot_approval`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- 若 star_pivot_approval 尚无表结构，请先导入 sql/star_pivot_approval.sql

INSERT INTO star_pivot_approval.ap_instance
SELECT s.* FROM star_pivot.ap_instance s
WHERE NOT EXISTS (
    SELECT 1 FROM star_pivot_approval.ap_instance t WHERE t.instance_id = s.instance_id
);

INSERT INTO star_pivot_approval.ap_notification
SELECT s.* FROM star_pivot.ap_notification s
WHERE NOT EXISTS (
    SELECT 1 FROM star_pivot_approval.ap_notification t WHERE t.notify_id = s.notify_id
);

INSERT INTO star_pivot_approval.ap_record
SELECT s.* FROM star_pivot.ap_record s
WHERE NOT EXISTS (
    SELECT 1 FROM star_pivot_approval.ap_record t WHERE t.record_id = s.record_id
);

INSERT INTO star_pivot_approval.ap_task
SELECT s.* FROM star_pivot.ap_task s
WHERE NOT EXISTS (
    SELECT 1 FROM star_pivot_approval.ap_task t WHERE t.task_id = s.task_id
);

INSERT INTO star_pivot_approval.ap_template
SELECT s.* FROM star_pivot.ap_template s
WHERE NOT EXISTS (
    SELECT 1 FROM star_pivot_approval.ap_template t WHERE t.template_id = s.template_id
);

INSERT INTO star_pivot_approval.ap_template_bind
SELECT s.* FROM star_pivot.ap_template_bind s
WHERE NOT EXISTS (
    SELECT 1 FROM star_pivot_approval.ap_template_bind t WHERE t.bind_id = s.bind_id
);

INSERT INTO star_pivot_approval.ap_template_route
SELECT s.* FROM star_pivot.ap_template_route s
WHERE NOT EXISTS (
    SELECT 1 FROM star_pivot_approval.ap_template_route t WHERE t.route_id = s.route_id
);

INSERT INTO star_pivot_approval.ap_template_step
SELECT s.* FROM star_pivot.ap_template_step s
WHERE NOT EXISTS (
    SELECT 1 FROM star_pivot_approval.ap_template_step t WHERE t.step_id = s.step_id
);

-- 验证无误后，从 star_pivot 删除审批表（按需取消注释）
-- DROP TABLE IF EXISTS star_pivot.ap_instance;
-- DROP TABLE IF EXISTS star_pivot.ap_notification;
-- DROP TABLE IF EXISTS star_pivot.ap_record;
-- DROP TABLE IF EXISTS star_pivot.ap_task;
-- DROP TABLE IF EXISTS star_pivot.ap_template;
-- DROP TABLE IF EXISTS star_pivot.ap_template_bind;
-- DROP TABLE IF EXISTS star_pivot.ap_template_route;
-- DROP TABLE IF EXISTS star_pivot.ap_template_step;
