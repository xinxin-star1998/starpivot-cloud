-- =============================================================================
-- 文件中心初始化脚本
-- 执行前请确认 sys_menu 最大 menu_id < 169，否则请调整下方菜单 ID
-- =============================================================================

-- ----------------------------
-- 1. 文件夹表（第二层）
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_folder`;
CREATE TABLE `sys_file_folder` (
  `folder_id`   bigint       NOT NULL AUTO_INCREMENT COMMENT '文件夹ID',
  `category`    varchar(20)  NOT NULL COMMENT '业务分类 SYSTEM/OA/CONTRACT/CERT/PROJECT/CUSTOMER/GOODS/FINANCE/HR/OTHER',
  `folder_name` varchar(100) NOT NULL COMMENT '文件夹名称',
  `parent_id`   bigint       NOT NULL DEFAULT 0 COMMENT '父文件夹ID（双层结构固定为0）',
  `order_num`   int          NULL DEFAULT 0 COMMENT '显示顺序',
  `status`      char(1)      NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `del_flag`    char(1)      NULL DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
  `create_by`   varchar(64)  NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime     NULL DEFAULT NULL COMMENT '创建时间',
  `update_by`   varchar(64)  NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime     NULL DEFAULT NULL COMMENT '更新时间',
  `remark`      varchar(255) NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`folder_id`) USING BTREE,
  UNIQUE KEY `uk_category_folder` (`category`, `folder_name`, `del_flag`) USING BTREE,
  KEY `idx_category_del` (`category`, `del_flag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件中心-文件夹' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 2. 文件元数据表
-- ----------------------------
DROP TABLE IF EXISTS `sys_file`;
CREATE TABLE `sys_file` (
  `file_id`          bigint        NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `folder_id`        bigint        NOT NULL COMMENT '所属文件夹ID',
  `category`         varchar(20)   NOT NULL COMMENT '业务分类（冗余，便于检索）',
  `media_type`       varchar(20)   NOT NULL DEFAULT 'OTHER' COMMENT '媒体类型 IMAGE/VIDEO/DOCUMENT/AUDIO/OTHER',
  `file_name`        varchar(255)  NOT NULL COMMENT '原始文件名',
  `file_ext`         varchar(20)   NULL DEFAULT NULL COMMENT '扩展名',
  `content_type`     varchar(100)  NULL DEFAULT NULL COMMENT 'MIME类型',
  `file_size`        bigint        NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `object_name`      varchar(500)  NOT NULL COMMENT 'OSS/本地对象路径',
  `file_hash`        varchar(64)   NULL DEFAULT NULL COMMENT 'SHA256（可选，去重用）',
  `storage_provider` varchar(20)   NULL DEFAULT 'OSS' COMMENT '存储驱动 OSS/LOCAL',
  `biz_type`         varchar(50)   NULL DEFAULT NULL COMMENT '业务类型 notice/contract 等',
  `biz_id`           varchar(64)   NULL DEFAULT NULL COMMENT '业务主键',
  `del_flag`         char(1)       NULL DEFAULT '0' COMMENT '删除标志（0正常 2已删除）',
  `delete_by`        varchar(64)   NULL DEFAULT NULL COMMENT '删除者',
  `delete_time`      datetime      NULL DEFAULT NULL COMMENT '删除时间',
  `create_by`        varchar(64)   NULL DEFAULT '' COMMENT '创建者',
  `create_time`      datetime      NULL DEFAULT NULL COMMENT '创建时间',
  `update_by`        varchar(64)   NULL DEFAULT '' COMMENT '更新者',
  `update_time`      datetime      NULL DEFAULT NULL COMMENT '更新时间',
  `remark`           varchar(255)  NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`file_id`) USING BTREE,
  UNIQUE KEY `uk_object_name` (`object_name`) USING BTREE,
  KEY `idx_folder_del` (`folder_id`, `del_flag`) USING BTREE,
  KEY `idx_media_type` (`media_type`, `del_flag`) USING BTREE,
  KEY `idx_category_time` (`category`, `create_time`) USING BTREE,
  KEY `idx_biz` (`biz_type`, `biz_id`) USING BTREE,
  KEY `idx_recycle` (`del_flag`, `delete_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '文件中心-文件元数据' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 3. 各 Category 默认文件夹（folder_id 1~10 固定，便于种子数据引用）
-- ----------------------------
INSERT INTO `sys_file_folder` (`folder_id`, `category`, `folder_name`, `parent_id`, `order_num`, `status`, `del_flag`, `create_by`, `create_time`, `remark`) VALUES
(1,  'SYSTEM',   '默认', 0, 1, '0', '0', 'admin', NOW(), '系统通用-默认文件夹'),
(2,  'OA',       '默认', 0, 1, '0', '0', 'admin', NOW(), '办公审批-默认文件夹'),
(3,  'CONTRACT', '默认', 0, 1, '0', '0', 'admin', NOW(), '合同档案-默认文件夹'),
(4,  'CERT',     '默认', 0, 1, '0', '0', 'admin', NOW(), '资质证件-默认文件夹'),
(5,  'PROJECT',  '默认', 0, 1, '0', '0', 'admin', NOW(), '项目资料-默认文件夹'),
(6,  'CUSTOMER', '默认', 0, 1, '0', '0', 'admin', NOW(), '客户资料-默认文件夹'),
(7,  'GOODS',    '默认', 0, 1, '0', '0', 'admin', NOW(), '商品素材-默认文件夹'),
(8,  'FINANCE',  '默认', 0, 1, '0', '0', 'admin', NOW(), '财务单据-默认文件夹'),
(9,  'HR',       '默认', 0, 1, '0', '0', 'admin', NOW(), '人事档案-默认文件夹'),
(10, 'OTHER',    '默认', 0, 1, '0', '0', 'admin', NOW(), '其他附件-默认文件夹');

-- ----------------------------
-- 4. 菜单与按钮权限（menu_id 169~180）
-- ----------------------------
INSERT INTO `sys_menu` VALUES (169, '文件中心', 0, 4, '/file', '', NULL, 'FileCenter', 1, 1, 'M', '0', '0', '', 'ep:folder-opened', 'admin', NOW(), '', NULL, '文件中心模块');
INSERT INTO `sys_menu` VALUES (170, '文件管理', 169, 1, 'index', '/file/index', NULL, 'FileManage', 1, 1, 'C', '0', '0', 'file:resource:list', 'ep:document', 'admin', NOW(), '', NULL, '文件管理页面');
INSERT INTO `sys_menu` VALUES (171, '文件查询', 170, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:query', '#', 'admin', NOW(), '', NULL, '文件列表/详情/预览');
INSERT INTO `sys_menu` VALUES (172, '文件上传', 170, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:add', '#', 'admin', NOW(), '', NULL, '上传文件');
INSERT INTO `sys_menu` VALUES (173, '文件删除', 170, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:delete', '#', 'admin', NOW(), '', NULL, '逻辑删除');
INSERT INTO `sys_menu` VALUES (174, '文件恢复', 170, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:restore', '#', 'admin', NOW(), '', NULL, '回收站恢复');
INSERT INTO `sys_menu` VALUES (175, '文件夹查询', 170, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:query', '#', 'admin', NOW(), '', NULL, '文件夹树');
INSERT INTO `sys_menu` VALUES (176, '文件夹新增', 170, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:add', '#', 'admin', NOW(), '', NULL, '新建文件夹');
INSERT INTO `sys_menu` VALUES (177, '文件夹编辑', 170, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:edit', '#', 'admin', NOW(), '', NULL, '重命名/排序');
INSERT INTO `sys_menu` VALUES (178, '文件夹删除', 170, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:delete', '#', 'admin', NOW(), '', NULL, '删除文件夹');
INSERT INTO `sys_menu` VALUES (179, '文件迁移', 170, 9, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:move', '#', 'admin', NOW(), '', NULL, '迁移到其他文件夹');

-- 超级管理员角色授权（role_id=1）；其他角色请在后台「角色管理」中分配
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 169), (1, 170), (1, 171), (1, 172), (1, 173), (1, 174),
(1, 175), (1, 176), (1, 177), (1, 178), (1, 179);

-- =============================================================================
-- 增量迁移（若已删除「默认」文件夹，可单独执行以下语句恢复）
-- =============================================================================
-- INSERT INTO `sys_file_folder` (`category`, `folder_name`, `parent_id`, `order_num`, `status`, `del_flag`, `create_by`, `create_time`, `remark`)
-- SELECT 'SYSTEM', '默认', 0, 1, '0', '0', 'admin', NOW(), '系统通用-默认文件夹' FROM DUAL
-- WHERE NOT EXISTS (SELECT 1 FROM `sys_file_folder` WHERE `category` = 'SYSTEM' AND `folder_name` = '默认' AND `del_flag` = '0');
-- （其余 OA / CONTRACT / CERT / PROJECT / CUSTOMER / GOODS / FINANCE / HR / OTHER 同理）

-- =============================================================================
-- 增量迁移（若已执行过旧版菜单脚本，可单独执行以下语句）
-- =============================================================================
-- INSERT INTO `sys_menu` VALUES (179, '文件迁移', 170, 9, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:move', '#', 'admin', NOW(), '', NULL, '迁移到其他文件夹');
-- INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 179);

-- =============================================================================
-- 增量迁移（若已执行过无 media_type 的旧版 DDL，可单独执行以下语句）
-- =============================================================================
-- ALTER TABLE `sys_file`
--   ADD COLUMN `media_type` varchar(20) NOT NULL DEFAULT 'OTHER'
--     COMMENT '媒体类型 IMAGE/VIDEO/DOCUMENT/AUDIO/OTHER' AFTER `category`,
--   ADD KEY `idx_media_type` (`media_type`, `del_flag`);
