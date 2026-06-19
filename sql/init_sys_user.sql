-- 初始化管理员账号（admin / 123456）
-- README 默认账号与此脚本保持一致

USE starpivot;

UPDATE sys_user
SET password = '$2a$10$qkb8F/erWiJT9D2/ouAkk.PXJOTTQ/UxVqrWq6DL9k3/ealix1dui'
WHERE user_name = 'admin';
