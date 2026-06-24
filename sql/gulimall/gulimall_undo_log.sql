-- =============================================================================
-- StarPivot 商城 - Seata AT 模式 undo_log（商城库共用）
-- 目标库: star_pivot_mall
-- =============================================================================

USE `star_pivot_mall`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `id`            bigint       NOT NULL AUTO_INCREMENT,
  `branch_id`     bigint       NOT NULL,
  `xid`           varchar(100) NOT NULL,
  `context`       varchar(128) NOT NULL,
  `rollback_info` longblob     NOT NULL,
  `log_status`    int          NOT NULL,
  `log_created`   datetime(3)  NOT NULL,
  `log_modified`  datetime(3)  NOT NULL,
  `ext`           varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Seata AT 事务回滚日志' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
