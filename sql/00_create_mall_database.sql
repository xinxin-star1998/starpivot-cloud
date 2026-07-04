-- MySQL 容器首次初始化时创建商城各域库（star_pivot 由 MYSQL_DATABASE 自动创建）
CREATE DATABASE IF NOT EXISTS `star_pivot_product`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS `star_pivot_ware`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS `star_pivot_order`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS `star_pivot_member`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS `star_pivot_promotion`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
