-- SAS 审批中台初始化脚本
-- 用法: mysql -h127.0.0.1 -P3307 -uroot -proot starpivot < sql/init_approval.sql

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------------
-- 1. 表结构
-- ---------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `ap_template` (
  `template_id`     bigint       NOT NULL AUTO_INCREMENT,
  `template_code`   varchar(64)  NOT NULL COMMENT '模板编码',
  `template_name`   varchar(128) NOT NULL,
  `biz_module`      varchar(32)  NOT NULL COMMENT 'mall/system/…',
  `version`         int          NOT NULL DEFAULT 1,
  `status`          varchar(16)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/DISABLED',
  `remark`          varchar(500) NULL,
  `create_by`       varchar(64)  NULL,
  `create_time`     datetime     NULL,
  `update_by`       varchar(64)  NULL,
  `update_time`     datetime     NULL,
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_code_version` (`template_code`, `version`),
  KEY `idx_module_status` (`biz_module`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批模板';

CREATE TABLE IF NOT EXISTS `ap_template_step` (
  `step_id`           bigint       NOT NULL AUTO_INCREMENT,
  `template_id`       bigint       NOT NULL,
  `step_code`         varchar(64)  NOT NULL COMMENT '步骤编码，实例内唯一',
  `step_name`         varchar(128) NOT NULL,
  `step_order`        int          NOT NULL COMMENT '排序，从 1 递增',
  `assignee_type`     varchar(32)  NOT NULL,
  `assignee_value`    varchar(128) NULL,
  `approve_mode`      varchar(8)   NOT NULL DEFAULT 'ANY' COMMENT 'ANY/ALL',
  `skip_expression`   varchar(512) NULL COMMENT 'SpEL，true 则跳过本步',
  `create_time`       datetime     NULL,
  PRIMARY KEY (`step_id`),
  UNIQUE KEY `uk_tpl_step_code` (`template_id`, `step_code`),
  KEY `idx_tpl_order` (`template_id`, `step_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批模板步骤';

CREATE TABLE IF NOT EXISTS `ap_template_route` (
  `route_id`        bigint       NOT NULL AUTO_INCREMENT,
  `template_id`     bigint       NOT NULL,
  `from_step_id`    bigint       NOT NULL,
  `priority`        int          NOT NULL DEFAULT 0 COMMENT '越小优先',
  `condition_expr`  varchar(512) NULL COMMENT 'SpEL，空或 default 表示默认分支',
  `to_step_id`      bigint       NOT NULL,
  PRIMARY KEY (`route_id`),
  KEY `idx_tpl_from` (`template_id`, `from_step_id`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批步骤路由';

CREATE TABLE IF NOT EXISTS `ap_template_bind` (
  `bind_id`       bigint       NOT NULL AUTO_INCREMENT,
  `biz_module`    varchar(32)  NOT NULL,
  `biz_type`      varchar(64)  NOT NULL COMMENT 'purchase/return/coupon…',
  `match_expr`    varchar(512) NULL COMMENT 'SpEL，空=总是匹配',
  `template_code` varchar(64)  NOT NULL COMMENT '指向已发布模板',
  `priority`      int          NOT NULL DEFAULT 0,
  `status`        char(1)      NOT NULL DEFAULT '0' COMMENT '0启用 1停用',
  PRIMARY KEY (`bind_id`),
  KEY `idx_biz` (`biz_module`, `biz_type`, `status`, `priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批模板业务绑定';

CREATE TABLE IF NOT EXISTS `ap_instance` (
  `instance_id`     bigint       NOT NULL AUTO_INCREMENT,
  `template_id`     bigint       NOT NULL,
  `template_code`   varchar(64)  NOT NULL,
  `biz_module`      varchar(32)  NOT NULL,
  `biz_type`        varchar(64)  NOT NULL,
  `biz_key`         varchar(128) NOT NULL,
  `title`           varchar(256) NOT NULL,
  `starter_id`      bigint       NOT NULL,
  `status`          varchar(16)  NOT NULL DEFAULT 'RUNNING',
  `current_step_id` bigint       NULL COMMENT '当前步骤',
  `context_json`    json         NULL COMMENT '审批上下文',
  `create_time`     datetime     NULL,
  `finish_time`     datetime     NULL,
  PRIMARY KEY (`instance_id`),
  KEY `idx_starter` (`starter_id`),
  KEY `idx_biz` (`biz_module`, `biz_type`),
  KEY `idx_biz_key_status` (`biz_key`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批实例';

CREATE TABLE IF NOT EXISTS `ap_task` (
  `task_id`       bigint       NOT NULL AUTO_INCREMENT,
  `instance_id`   bigint       NOT NULL,
  `step_id`       bigint       NOT NULL,
  `step_code`     varchar(64)  NOT NULL,
  `step_name`     varchar(128) NOT NULL,
  `assignee_id`   bigint       NOT NULL,
  `status`        varchar(16)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/DONE/CANCELLED',
  `action`        varchar(16)  NULL COMMENT 'APPROVE/REJECT',
  `comment`       varchar(500) NULL,
  `create_time`   datetime     NULL,
  `finish_time`   datetime     NULL,
  PRIMARY KEY (`task_id`),
  KEY `idx_assignee_status` (`assignee_id`, `status`),
  KEY `idx_instance` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批待办';

CREATE TABLE IF NOT EXISTS `ap_record` (
  `record_id`     bigint       NOT NULL AUTO_INCREMENT,
  `instance_id`   bigint       NOT NULL,
  `task_id`       bigint       NULL,
  `step_code`     varchar(64)  NOT NULL,
  `step_name`     varchar(128) NOT NULL,
  `operator_id`   bigint       NOT NULL,
  `action`        varchar(16)  NOT NULL COMMENT 'SUBMIT/APPROVE/REJECT/WITHDRAW/SKIP',
  `comment`       varchar(500) NULL,
  `create_time`   datetime     NOT NULL,
  PRIMARY KEY (`record_id`),
  KEY `idx_instance` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录';

-- ---------------------------------------------------------------------------
-- 2. 系统配置：忘记密码开关（默认关闭，与注册开关一致）
-- ---------------------------------------------------------------------------

INSERT INTO `sys_config` (`config_id`, `config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`)
SELECT 6, '账号自助-是否开启忘记密码功能', 'sys.account.forgetPassword', 'false', 'Y', 'admin', NOW(), '是否开启忘记密码功能（true开启，false关闭）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_config` WHERE `config_key` = 'sys.account.forgetPassword');

-- ---------------------------------------------------------------------------
-- 3. 商城默认审批模板（采购 / 退货）
-- ---------------------------------------------------------------------------

DELETE FROM `ap_template_bind` WHERE `biz_module` = 'mall' AND `biz_type` IN ('purchase', 'return');
DELETE FROM `ap_template_route` WHERE `template_id` IN (1, 2);
DELETE FROM `ap_template_step` WHERE `template_id` IN (1, 2);
DELETE FROM `ap_template` WHERE `template_id` IN (1, 2);

INSERT INTO `ap_template` (`template_id`, `template_code`, `template_name`, `biz_module`, `version`, `status`, `remark`, `create_by`, `create_time`)
VALUES
  (1, 'mall_purchase_default', '采购单默认审批', 'mall', 1, 'PUBLISHED', '部门负责人 → 管理员复核', 'admin', NOW()),
  (2, 'mall_return_default', '退货申请默认审批', 'mall', 1, 'PUBLISHED', '部门负责人 → 管理员复核', 'admin', NOW());

INSERT INTO `ap_template_step` (`step_id`, `template_id`, `step_code`, `step_name`, `step_order`, `assignee_type`, `assignee_value`, `approve_mode`, `create_time`)
VALUES
  (1, 1, 'dept_leader', '部门负责人审批', 1, 'DEPT_LEADER', NULL, 'ANY', NOW()),
  (2, 1, 'admin_review', '管理员复核', 2, 'ROLE', 'admin', 'ANY', NOW()),
  (3, 2, 'dept_leader', '部门负责人审批', 1, 'DEPT_LEADER', NULL, 'ANY', NOW()),
  (4, 2, 'admin_review', '管理员复核', 2, 'ROLE', 'admin', 'ANY', NOW());

INSERT INTO `ap_template_route` (`route_id`, `template_id`, `from_step_id`, `priority`, `condition_expr`, `to_step_id`)
VALUES
  (1, 1, 1, 0, NULL, 2),
  (2, 2, 3, 0, NULL, 4);

INSERT INTO `ap_template_bind` (`bind_id`, `biz_module`, `biz_type`, `match_expr`, `template_code`, `priority`, `status`)
VALUES
  (1, 'mall', 'purchase', NULL, 'mall_purchase_default', 0, '0'),
  (2, 'mall', 'return', NULL, 'mall_return_default', 0, '0');
