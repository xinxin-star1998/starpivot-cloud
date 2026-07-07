/*
 Navicat Premium Data Transfer

 Source Server         : docker
 Source Server Type    : MySQL
 Source Server Version : 80046
 Source Host           : localhost:3307
 Source Schema         : star_pivot

 Target Server Type    : MySQL
 Target Server Version : 80046
 File Encoding         : 65001

 Date: 07/07/2026 19:31:13
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '父菜单ID',
  `order_num` int(0) NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '组件路径',
  `query` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '路由参数',
  `route_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '路由名称',
  `is_frame` int(0) NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `is_cache` int(0) NULL DEFAULT 0 COMMENT '是否缓存（0缓存 1不缓存）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `visible` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '菜单状态（0显示 1隐藏）',
  `status` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '0' COMMENT '菜单状态（0正常 1停用）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 348 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, '/system', '', NULL, 'System', 1, 1, 'M', '0', '0', '', 'ep:setting', 'system', '2025-12-31 16:34:16', 'admin', '2026-07-06 10:39:37', '系统管理模块111');
INSERT INTO `sys_menu` VALUES (2, '系统工具', 0, 2, '/tools', '', NULL, 'SystemTools', 1, 1, 'M', '0', '0', '', 'ri:tools-line', 'admin', '2026-01-20 13:08:43', 'admin', '2026-04-19 19:09:23', '系统工具');
INSERT INTO `sys_menu` VALUES (3, '系统监控', 0, 3, '/monitor', '', NULL, 'Monitor', 1, 1, 'M', '0', '0', '', 'ri:computer-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-05-18 15:42:45', '系统监控模块');
INSERT INTO `sys_menu` VALUES (4, '文件中心', 0, 4, '/file', '', NULL, 'FileCenter', 1, 1, 'M', '0', '0', '', 'ep:folder-opened', 'admin', '2026-06-22 19:45:35', 'admin', '2026-07-04 20:16:43', '文件中心模块');
INSERT INTO `sys_menu` VALUES (5, '商城系统', 0, 5, '/mall', '', NULL, 'MallSystem', 1, 1, 'M', '0', '0', '', 'ep:goods', 'admin', '2026-04-23 20:23:16', 'admin', '2026-07-05 11:00:25', '商城系统');
INSERT INTO `sys_menu` VALUES (6, '审批中心', 0, 6, '/approval', '', NULL, 'ApprovalCenter', 1, 1, 'M', '0', '0', '', 'mdi:clipboard-check-outline', 'admin', '2026-06-26 10:49:49', '', NULL, 'SAS 审批中台');
INSERT INTO `sys_menu` VALUES (7, '星枢项目', 0, 99, 'https://gitee.com/xin1998/StarPivot', '', NULL, '', 0, 1, 'M', '0', '0', '', 'ri:gitee-fill', 'admin', '2026-04-21 12:48:19', 'admin', '2026-05-18 22:44:22', '星枢项目');
INSERT INTO `sys_menu` VALUES (8, 'art-design-pro', 0, 100, 'https://gitee.com/lingchen163/art-design-pro', '', NULL, '', 0, 1, 'M', '0', '0', '', 'ri:guide-fill', 'admin', '2026-04-19 19:07:54', 'admin', '2026-04-23 20:26:22', '');
INSERT INTO `sys_menu` VALUES (9, '菜单管理', 1, 1, 'menu', '/system/menu', NULL, 'SystemMenu', 1, 1, 'C', '0', '0', 'system:menu:list', 'ep:menu', 'system', '2025-12-31 16:34:16', '', '2026-01-02 21:12:33', '菜单管理模块');
INSERT INTO `sys_menu` VALUES (10, '角色管理', 1, 2, 'role', '/system/role', NULL, 'SystemRole', 1, 1, 'C', '0', '0', 'system:role:list', 'ri:shield-user-line', 'system', '2025-12-31 16:34:16', 'admin', '2026-06-08 16:46:55', '角色管理模块');
INSERT INTO `sys_menu` VALUES (11, '用户管理', 1, 3, 'user', '/system/user', NULL, 'SystemUser', 1, 1, 'C', '0', '0', 'system:user:list', 'mdi:user', 'system', '2025-12-31 16:34:16', '', '2026-01-02 21:31:51', '用户管理模块');
INSERT INTO `sys_menu` VALUES (12, '部门管理', 1, 4, 'dept', '/system/dept', NULL, 'SystemDept', 1, 1, 'C', '0', '0', 'system:dept:list', 'ri:organization-chart', '', '2026-01-02 21:04:34', '', '2026-01-02 21:36:43', '部门管理模块');
INSERT INTO `sys_menu` VALUES (13, '岗位管理', 1, 5, 'post', '/system/post/index', NULL, 'PostManage', 1, 1, 'C', '0', '0', 'system:post:list', 'ri:briefcase-line', 'xinxin', '2026-01-04 19:23:51', 'xinxin', '2026-01-04 19:25:02', '岗位管理模块');
INSERT INTO `sys_menu` VALUES (14, '字典管理', 1, 6, 'dict', '/system/dict/index', NULL, 'DictManage', 1, 1, 'C', '0', '0', 'system:type:list', 'ep:notebook', 'admin', '2026-01-05 12:28:54', 'admin', '2026-01-19 21:37:20', '字典管理模块。有：字典数据   字典类型');
INSERT INTO `sys_menu` VALUES (15, '日志管理', 1, 7, 'log', '', NULL, 'LogManager', 1, 1, 'M', '0', '0', '', 'mdi:math-log', 'admin', '2026-01-23 13:37:05', 'admin', '2026-05-15 09:09:47', '');
INSERT INTO `sys_menu` VALUES (16, '通知公告', 1, 8, 'notice', '/system/notice/index', NULL, 'NoticeManage', 1, 0, 'C', '0', '0', 'system:notice:list', 'ri:notification-3-line', 'admin', '2026-02-05 17:38:35', 'admin', '2026-03-31 22:03:49', '通知公告菜单');
INSERT INTO `sys_menu` VALUES (17, '参数配置', 1, 9, 'config', '/system/config/index', NULL, 'ConfigManage', 1, 1, 'C', '0', '0', 'system:config:list', 'ep:setting', 'admin', '2026-03-31 22:03:28', 'admin', '2026-03-31 22:05:20', '参数配置菜单');
INSERT INTO `sys_menu` VALUES (18, '操作日志', 15, 1, 'oper', '/system/log/oper/index', NULL, 'OperLog', 1, 1, 'C', '0', '0', 'system:log:list', 'ri:file-list-3-line', 'admin', '2026-01-23 13:40:41', '', NULL, '操作日志');
INSERT INTO `sys_menu` VALUES (19, '登录日志', 15, 2, 'login', '/system/log/login/index', NULL, 'LoginInfoLog', 1, 1, 'C', '0', '0', 'system:login:list', 'ri:login-box-line', 'admin', '2026-01-23 13:51:37', '', NULL, '登录日志');
INSERT INTO `sys_menu` VALUES (20, '代码生成', 2, 1, 'generator', '/tools/generator/index', NULL, 'GenerateTools', 1, 1, 'C', '0', '0', 'tools:generator:list', 'mdi:generator-mobile', 'admin', '2026-01-20 13:15:59', 'admin', '2026-01-20 13:25:42', '代码生成');
INSERT INTO `sys_menu` VALUES (21, '外部库代码生成', 2, 2, 'external', '/tools/generator-external/index', NULL, '', 1, 1, 'C', '0', '0', '', 'ep:document-add', 'admin', '2026-06-02 18:13:11', 'admin', '2026-06-02 18:30:43', '');
INSERT INTO `sys_menu` VALUES (22, '服务器监控', 3, 1, 'server', '/monitor/server/index', NULL, 'ServerMonitor', 1, 0, 'C', '0', '0', 'monitor:server:query', 'ri:server-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-01-25 18:33:45', '服务器信息监控');
INSERT INTO `sys_menu` VALUES (23, 'Druid监控', 3, 2, 'druid', '/monitor/druid/index', NULL, 'DruidMonitor', 1, 0, 'C', '0', '0', 'monitor:druid:query', 'ri:database-2-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-01-25 18:32:03', 'Druid数据库连接池监控');
INSERT INTO `sys_menu` VALUES (24, 'Redis缓存', 3, 3, 'redis', '/monitor/redis/index', NULL, 'RedisMonitor', 1, 0, 'C', '0', '0', 'monitor:redis:query', 'ri:database-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-03-01 13:37:51', 'Redis缓存监控');
INSERT INTO `sys_menu` VALUES (25, '在线用户', 3, 4, 'online', '/monitor/online/index', NULL, 'OnlineUser', 1, 0, 'C', '0', '0', 'monitor:online:query', 'ri:user-line', 'admin', '2026-01-25 18:00:43', 'admin', '2026-01-25 18:32:45', '在线用户监控');
INSERT INTO `sys_menu` VALUES (26, '定时任务', 3, 5, 'job', '/monitor/job/index', NULL, 'MonitorJob', 1, 1, 'C', '0', '0', 'monitor:job:query', 'ri:time-line', 'admin', '2026-02-06 19:58:43', 'admin', '2026-02-06 20:34:11', '定时任务调度');
INSERT INTO `sys_menu` VALUES (27, '文件管理', 4, 1, 'index', '/file/index', NULL, 'FileManage', 1, 1, 'C', '0', '0', 'file:resource:list', 'ep:document', 'admin', '2026-06-22 19:45:35', '', NULL, '文件管理页面');
INSERT INTO `sys_menu` VALUES (28, 'C端商城', 5, 0, '/portal', '', NULL, 'MallPortal', 1, 1, 'C', '0', '0', '', 'ep:shopping-bag', 'admin', '2026-06-24 13:11:09', '', NULL, '打开 C 端商城前台');
INSERT INTO `sys_menu` VALUES (29, '商品管理', 5, 1, 'pms', '', NULL, 'GoodManager', 1, 1, 'M', '0', '0', '', 'ep:goods-filled', 'admin', '2026-05-15 10:38:29', 'admin', '2026-06-23 19:58:02', '');
INSERT INTO `sys_menu` VALUES (30, '仓库系统', 5, 1, 'wms', '', NULL, 'WmsManager', 1, 1, 'M', '0', '0', '', 'mdi:warehouse', 'admin', '2026-05-19 15:26:07', 'admin', '2026-06-23 16:45:46', '仓储服务');
INSERT INTO `sys_menu` VALUES (31, '优惠营销', 5, 1, 'sms', '', NULL, 'PromotionMarketing', 1, 1, 'M', '0', '0', '', 'ep:promotion', 'admin', '2026-06-23 16:55:30', 'admin', '2026-06-23 19:58:27', '');
INSERT INTO `sys_menu` VALUES (32, '订单系统', 5, 4, 'oms', '', NULL, 'OrderManager', 1, 1, 'M', '0', '0', '', 'mdi:order-bool-descending-variant', 'admin', '2026-06-23 17:07:36', 'admin', '2026-06-23 19:59:00', '');
INSERT INTO `sys_menu` VALUES (33, '用户系统', 5, 5, 'ums', '', NULL, 'MemberSystem', 1, 1, 'M', '0', '0', '', 'mdi:wallet-membership', 'admin', '2026-06-23 17:12:59', 'admin', '2026-06-23 19:59:22', '');
INSERT INTO `sys_menu` VALUES (34, '内容管理', 5, 6, 'cms', '', NULL, 'ContentManage', 1, 1, 'M', '0', '0', '', 'mdi:content-paste', 'admin', '2026-06-23 17:17:02', 'admin', '2026-06-23 19:59:30', '');
INSERT INTO `sys_menu` VALUES (35, '审批模板', 6, 1, 'template', '/approval/template/index', NULL, 'ApprovalTemplate', 1, 1, 'C', '0', '0', 'approval:template:query', 'ep:document', 'admin', '2026-06-26 10:49:49', '', NULL, '审批模板配置');
INSERT INTO `sys_menu` VALUES (36, '业务绑定', 6, 2, 'bind', '/approval/bind/index', NULL, 'ApprovalBind', 1, 1, 'C', '0', '0', 'approval:bind:edit', 'ep:link', 'admin', '2026-06-26 10:49:49', '', NULL, '业务类型绑定规则');
INSERT INTO `sys_menu` VALUES (37, '待办审批', 6, 3, 'todo', '/approval/todo/index', NULL, 'ApprovalTodo', 1, 1, 'C', '0', '0', 'approval:task:query', 'ri:bell-line', 'admin', '2026-06-26 10:49:49', '', NULL, '待办与已办');
INSERT INTO `sys_menu` VALUES (38, '我发起的', 6, 4, 'mine', '/approval/mine/index', NULL, 'ApprovalMine', 1, 1, 'C', '0', '0', 'approval:instance:query', 'mdi:user-arrow-right', 'admin', '2026-06-26 10:49:49', '', NULL, '我发起的审批');
INSERT INTO `sys_menu` VALUES (39, '菜单查询', 9, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:menu:query', '#', 'admin', '2026-01-12 17:39:26', '', NULL, '菜单查询');
INSERT INTO `sys_menu` VALUES (40, '菜单添加', 9, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:menu:add', '#', 'admin', '2026-01-12 17:39:50', '', NULL, '菜单添加');
INSERT INTO `sys_menu` VALUES (41, '菜单修改', 9, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:menu:edit', '#', 'admin', '2026-01-12 17:40:05', '', NULL, '菜单修改');
INSERT INTO `sys_menu` VALUES (42, '菜单删除', 9, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:menu:delete', '#', 'admin', '2026-01-12 17:40:27', '', NULL, '菜单删除');
INSERT INTO `sys_menu` VALUES (43, '新增角色', 10, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:add', '#', 'admin', '2026-01-12 17:32:13', '', NULL, '新增角色按钮');
INSERT INTO `sys_menu` VALUES (44, '修改角色', 10, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:edit', '#', 'admin', '2026-01-12 17:32:45', '', NULL, '修改角色按钮');
INSERT INTO `sys_menu` VALUES (45, '删除角色', 10, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:delete', '#', 'admin', '2026-01-12 17:33:14', '', NULL, '删除角色按钮');
INSERT INTO `sys_menu` VALUES (46, '角色查询', 10, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:query', '#', 'admin', '2026-01-12 17:34:57', '', NULL, '角色查询 按钮');
INSERT INTO `sys_menu` VALUES (47, '分配数据权限', 10, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:assignDataScope', '#', 'admin', '2026-01-16 19:04:22', 'admin', '2026-01-23 20:57:15', '分配角色');
INSERT INTO `sys_menu` VALUES (48, '已分配用户', 10, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:allocatedList', '#', 'admin', '2026-01-23 20:55:10', 'admin', '2026-01-23 22:41:41', '已分配用户');
INSERT INTO `sys_menu` VALUES (49, '未分配用户', 10, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:unallocatedList', '#', 'admin', '2026-01-23 22:41:08', '', NULL, '未分配用户');
INSERT INTO `sys_menu` VALUES (50, '分配用户', 10, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:assignUser', '#', 'admin', '2026-01-23 23:18:49', '', NULL, '分配用户：添加用户角色关联表');
INSERT INTO `sys_menu` VALUES (51, '取消授权', 10, 9, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:role:cancelUser', '#', 'admin', '2026-01-23 23:26:23', '', NULL, '取消授权');
INSERT INTO `sys_menu` VALUES (52, '新增用户', 11, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:add', '#', 'admin', '2026-01-12 16:42:30', 'admin', '2026-01-16 20:42:35', '新增用户按钮');
INSERT INTO `sys_menu` VALUES (53, '修改状态', 11, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:changeStatus', '#', 'admin', '2026-01-16 18:11:22', 'admin', '2026-01-16 20:42:31', '修改状态');
INSERT INTO `sys_menu` VALUES (54, '修改用户', 11, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:edit', '#', 'admin', '2026-01-12 17:30:37', 'admin', '2026-01-16 20:42:26', '修改用户  按钮');
INSERT INTO `sys_menu` VALUES (55, '删除用户', 11, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:delete', '#', 'admin', '2026-01-12 17:31:11', 'admin', '2026-01-16 20:42:39', '删除用户按钮');
INSERT INTO `sys_menu` VALUES (56, '用户查询', 11, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:query', '#', 'admin', '2026-01-12 17:35:27', 'admin', '2026-01-16 20:42:43', '用户查询按钮');
INSERT INTO `sys_menu` VALUES (57, '修改密码', 11, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', '2026-01-16 18:10:55', 'admin', '2026-01-16 20:42:48', '修改密码');
INSERT INTO `sys_menu` VALUES (58, '解除锁定', 11, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:unLock', '#', 'admin', '2026-01-23 18:49:56', '', NULL, '解除锁定');
INSERT INTO `sys_menu` VALUES (59, '用户导入', 11, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:import', '#', 'admin', '2026-01-24 21:03:18', '', NULL, '用户导入');
INSERT INTO `sys_menu` VALUES (60, '用户导出', 11, 9, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:user:export', '#', 'admin', '2026-01-24 21:03:52', '', NULL, '用户导出');
INSERT INTO `sys_menu` VALUES (61, '部门查询', 12, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:dept:query', '#', 'admin', '2026-01-12 17:35:58', '', NULL, '部门查询按钮');
INSERT INTO `sys_menu` VALUES (62, '部门新增', 12, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:dept:add', '#', 'admin', '2026-01-12 17:36:28', '', NULL, '部门新增');
INSERT INTO `sys_menu` VALUES (63, '部门修改', 12, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:dept:edit', '#', 'admin', '2026-01-12 17:36:49', '', NULL, '部门修改');
INSERT INTO `sys_menu` VALUES (64, '部门删除', 12, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:dept:delete', '#', 'admin', '2026-01-12 17:37:21', '', NULL, '部门删除');
INSERT INTO `sys_menu` VALUES (65, '岗位查询', 13, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:post:query', '#', 'admin', '2026-01-12 17:37:47', '', NULL, '岗位查询');
INSERT INTO `sys_menu` VALUES (66, '岗位新增', 13, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:post:add', '#', 'admin', '2026-01-12 17:38:09', '', NULL, '岗位新增');
INSERT INTO `sys_menu` VALUES (67, '岗位修改', 13, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:post:edit', '#', 'admin', '2026-01-12 17:38:46', '', NULL, '岗位修改');
INSERT INTO `sys_menu` VALUES (68, '岗位删除', 13, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:post:delete', '#', 'admin', '2026-01-12 17:39:04', '', NULL, '岗位删除');
INSERT INTO `sys_menu` VALUES (69, '字典类型添加', 14, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:type:add', '#', 'admin', '2026-01-16 19:08:40', 'admin', '2026-01-16 19:33:35', '字典类型添加');
INSERT INTO `sys_menu` VALUES (70, '字典类型修改', 14, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:type:edit', '#', 'admin', '2026-01-16 19:09:04', 'admin', '2026-01-16 19:33:43', '字典类型修改');
INSERT INTO `sys_menu` VALUES (71, '字典类型删除', 14, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:type:delete', '#', 'admin', '2026-01-16 19:09:27', 'admin', '2026-01-16 19:33:48', '字典类型删除');
INSERT INTO `sys_menu` VALUES (72, '字典类型查询', 14, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:type:query', '#', 'admin', '2026-01-19 21:33:21', '', NULL, '字典类型查询');
INSERT INTO `sys_menu` VALUES (73, '字典数据添加', 14, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:data:add', '#', 'admin', '2026-01-16 19:31:42', '', NULL, '字典数据添加');
INSERT INTO `sys_menu` VALUES (74, '字典数据修改', 14, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:data:edit', '#', 'admin', '2026-01-16 19:32:19', '', NULL, '字典数据修改');
INSERT INTO `sys_menu` VALUES (75, '字典数据删除', 14, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:data:delete', '#', 'admin', '2026-01-16 19:32:51', '', NULL, '字典数据删除');
INSERT INTO `sys_menu` VALUES (76, '字典数据查询', 14, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:data:query', '#', 'admin', '2026-01-19 21:33:59', '', NULL, '字典数据查询');
INSERT INTO `sys_menu` VALUES (77, '日志查询', 18, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:operlog:query', '#', 'admin', '2026-01-23 13:58:02', '', NULL, '日志查询');
INSERT INTO `sys_menu` VALUES (78, '清空日志', 18, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:operlog:delete', '#', 'admin', '2026-01-23 13:57:43', '', NULL, '清空日志');
INSERT INTO `sys_menu` VALUES (79, '日志查询', 19, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:logininfor:query', '#', 'admin', '2026-01-23 14:24:26', '', NULL, '日志查询');
INSERT INTO `sys_menu` VALUES (80, '日志删除', 19, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'system:logininfor:delete', '#', 'admin', '2026-01-23 14:24:41', '', NULL, '日志删除');
INSERT INTO `sys_menu` VALUES (81, '通知公告查询', 16, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:query', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (82, '通知公告新增', 16, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:add', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (83, '通知公告修改', 16, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:edit', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (84, '通知公告删除', 16, 4, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:delete', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (85, '通知公告导出', 16, 5, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:notice:export', '#', 'admin', '2026-02-05 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (86, '参数配置查询', 17, 1, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:query', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:23', '');
INSERT INTO `sys_menu` VALUES (87, '参数配置新增', 17, 2, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:add', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:27', '');
INSERT INTO `sys_menu` VALUES (88, '参数配置修改', 17, 3, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:edit', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:31', '');
INSERT INTO `sys_menu` VALUES (89, '参数配置删除', 17, 4, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:delete', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:36', '');
INSERT INTO `sys_menu` VALUES (90, '参数配置导出', 17, 5, '#', '', NULL, '', 1, 1, 'F', '0', '0', 'system:config:export', '#', 'admin', '2026-03-31 22:03:29', 'admin', '2026-03-31 22:04:39', '');
INSERT INTO `sys_menu` VALUES (91, '添加', 20, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:add', '#', 'admin', '2026-01-23 15:56:17', 'admin', '2026-01-23 19:01:31', '添加');
INSERT INTO `sys_menu` VALUES (92, '列表查询', 20, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:query', '#', 'admin', '2026-01-20 14:56:43', '', NULL, '列表查询');
INSERT INTO `sys_menu` VALUES (93, '预览', 20, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:preview', '#', 'admin', '2026-01-23 15:53:25', 'admin', '2026-01-23 19:01:40', '预览');
INSERT INTO `sys_menu` VALUES (94, '编辑', 20, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:edit', '#', 'admin', '2026-01-23 15:53:47', 'admin', '2026-01-23 19:01:44', '');
INSERT INTO `sys_menu` VALUES (95, '删除', 20, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:delete', '#', 'admin', '2026-01-23 15:54:05', 'admin', '2026-01-23 19:01:48', '');
INSERT INTO `sys_menu` VALUES (96, '同步数据库', 20, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:sync', '#', 'admin', '2026-01-23 15:55:18', 'admin', '2026-01-23 19:01:52', '同步数据库');
INSERT INTO `sys_menu` VALUES (97, '生成代码', 20, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:create', '#', 'admin', '2026-01-23 15:55:59', 'admin', '2026-01-23 19:01:56', '生成代码');
INSERT INTO `sys_menu` VALUES (98, '导入', 20, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:gen:import', '#', 'xinxin', '2026-01-24 00:08:41', '', NULL, '导入');
INSERT INTO `sys_menu` VALUES (99, '查询', 21, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:external:query', '#', 'admin', '2026-06-02 19:14:07', '', NULL, '');
INSERT INTO `sys_menu` VALUES (100, '预览', 21, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:external:preview', '#', 'admin', '2026-06-02 19:14:43', '', NULL, '');
INSERT INTO `sys_menu` VALUES (101, '生成', 21, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'tool:external:create', '#', 'admin', '2026-06-02 19:15:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (102, '删除缓存', 24, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:cache:remove', '#', 'admin', '2026-01-25 22:19:38', '', NULL, 'monitor:cache:remove');
INSERT INTO `sys_menu` VALUES (103, '清空缓存', 24, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:cache:clear', '#', 'admin', '2026-01-25 22:20:00', '', NULL, '清空缓存');
INSERT INTO `sys_menu` VALUES (104, '缓存列表', 24, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:cache:query', '#', 'admin', '2026-01-27 18:21:55', '', NULL, '缓存列表');
INSERT INTO `sys_menu` VALUES (105, '强制下线', 25, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:online:force-logout', '', 'admin', '2026-01-25 18:00:43', 'admin', '2026-01-25 18:30:55', '强制用户下线');
INSERT INTO `sys_menu` VALUES (106, '任务查询', 26, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:job:query', '#', 'admin', '2026-02-06 20:01:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (107, '任务新增', 26, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:job:add', '#', 'admin', '2026-02-06 20:01:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (108, '任务修改', 26, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:job:edit', '#', 'admin', '2026-02-06 20:01:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (109, '任务删除', 26, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'monitor:job:delete', '#', 'admin', '2026-02-06 20:01:06', '', NULL, '');
INSERT INTO `sys_menu` VALUES (110, '文件查询', 27, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:query', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '文件列表/详情/预览');
INSERT INTO `sys_menu` VALUES (111, '文件上传', 27, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:add', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '上传文件');
INSERT INTO `sys_menu` VALUES (112, '文件删除', 27, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:delete', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '逻辑删除');
INSERT INTO `sys_menu` VALUES (113, '文件恢复', 27, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:restore', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '回收站恢复');
INSERT INTO `sys_menu` VALUES (114, '文件夹查询', 27, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:query', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (115, '文件夹新增', 27, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:add', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (116, '文件夹编辑', 27, 7, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:edit', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (117, '文件夹删除', 27, 8, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:folder:delete', '#', 'admin', '2026-06-22 19:45:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (118, '文件迁移', 27, 9, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:move', '#', 'admin', '2026-06-22 20:54:50', '', NULL, '迁移到其他文件夹');
INSERT INTO `sys_menu` VALUES (119, '文件重命名', 27, 10, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:edit', '#', 'admin', '2026-06-26 13:05:02', 'admin', '2026-06-26 13:05:12', '');
INSERT INTO `sys_menu` VALUES (120, '永久删除', 27, 11, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:resource:purge', '#', 'admin', '2026-06-29 18:18:07', '', NULL, '回收站永久删除');
INSERT INTO `sys_menu` VALUES (121, '全部分类权限', 27, 12, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:category:all', '#', 'admin', '2026-06-29 18:18:07', '', NULL, '可访问全部文件分类');
INSERT INTO `sys_menu` VALUES (122, '人事档案分类', 27, 13, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:category:HR', '#', 'admin', '2026-06-29 18:18:07', '', NULL, 'HR 分类数据权限');
INSERT INTO `sys_menu` VALUES (123, '财务单据分类', 27, 14, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:category:FINANCE', '#', 'admin', '2026-06-29 18:18:07', '', NULL, 'FINANCE 分类数据权限');
INSERT INTO `sys_menu` VALUES (124, '合同档案分类', 27, 15, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:category:CONTRACT', '#', 'admin', '2026-06-29 18:18:07', '', NULL, 'CONTRACT 分类数据权限');
INSERT INTO `sys_menu` VALUES (125, '资质证件分类', 27, 16, '', '', NULL, '', 1, 1, 'F', '0', '0', 'file:category:CERT', '#', 'admin', '2026-06-29 18:18:07', '', NULL, 'CERT 分类数据权限');
INSERT INTO `sys_menu` VALUES (126, '分类维护', 29, 1, 'category', '/mall/pms/category/index', NULL, 'CategoryManager', 1, 1, 'C', '0', '0', 'mall:category:list', 'ri:node-tree', 'admin', '2026-05-15 10:43:03', 'admin', '2026-06-23 21:13:33', '');
INSERT INTO `sys_menu` VALUES (127, '品牌列表', 29, 2, 'brand', '/mall/pms/brand/index', NULL, 'BrandManager', 1, 1, 'C', '0', '0', 'mall:brand:list', 'ep:shop', 'admin', '2026-04-23 20:25:38', 'admin', '2026-06-23 21:13:40', '品牌管理');
INSERT INTO `sys_menu` VALUES (128, '平台属性', 29, 3, 'platform-attributes', '', NULL, 'PlatformAttributes', 1, 1, 'M', '0', '0', '', 'mdi:tools', 'admin', '2026-05-18 14:43:45', 'admin', '2026-05-18 15:04:12', '');
INSERT INTO `sys_menu` VALUES (129, '商品维护', 29, 4, 'maintain', '', NULL, 'GoodsMaintain', 1, 1, 'M', '0', '0', 'mall:product:list', 'ep:goods-filled', 'admin', '2026-04-23 20:27:38', 'admin', '2026-06-24 13:11:09', '商品管理');
INSERT INTO `sys_menu` VALUES (130, '分类维护查询', 126, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:category:query', '#', 'admin', '2026-05-18 22:50:11', '', NULL, '分类维护查询');
INSERT INTO `sys_menu` VALUES (131, '分类维护添加', 126, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:category:add', '#', 'admin', '2026-05-18 22:50:41', '', NULL, '分类维护添加');
INSERT INTO `sys_menu` VALUES (132, '分类维护删除', 126, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:category:delete', '#', 'admin', '2026-05-18 22:51:06', '', NULL, '分类维护删除');
INSERT INTO `sys_menu` VALUES (133, '分类维护修改', 126, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:category:edit', '#', 'admin', '2026-05-18 22:51:30', '', NULL, '分类维护修改');
INSERT INTO `sys_menu` VALUES (134, '品牌列表查询', 127, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:brand:query', '#', 'admin', '2026-05-18 22:46:48', '', NULL, '品牌列表查询');
INSERT INTO `sys_menu` VALUES (135, '品牌添加', 127, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:brand:add', '#', 'admin', '2026-05-18 22:47:16', '', NULL, '品牌添加');
INSERT INTO `sys_menu` VALUES (136, '品牌修改', 127, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:brand:edit', '#', 'admin', '2026-05-18 22:47:44', '', NULL, '品牌修改');
INSERT INTO `sys_menu` VALUES (137, '品牌删除', 127, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:brand:delete', '#', 'admin', '2026-05-18 22:48:06', '', NULL, '品牌删除');
INSERT INTO `sys_menu` VALUES (138, '属性分组', 128, 1, 'group', '/mall/pms/group/index', NULL, 'AttrgroupManager', 1, 1, 'C', '0', '0', '', 'ep:histogram', 'admin', '2026-05-18 14:59:27', 'admin', '2026-06-23 21:13:54', '');
INSERT INTO `sys_menu` VALUES (139, '规格参数', 128, 2, 'base', '/mall/pms/attr/base/index', NULL, 'BaseParam', 1, 1, 'C', '0', '0', '', 'ep:document', 'admin', '2026-05-18 15:06:50', 'admin', '2026-06-23 21:14:05', '');
INSERT INTO `sys_menu` VALUES (140, '销售属性', 128, 3, 'sale', '/mall/pms/attr/sale/index', NULL, 'SalesAttributes', 1, 1, 'C', '0', '0', '', 'ep:present', 'admin', '2026-05-18 15:12:11', 'admin', '2026-06-23 21:14:16', '');
INSERT INTO `sys_menu` VALUES (141, 'spu管理', 129, 1, 'spu', '/mall/pms/product/spu/index', NULL, 'SpuManager', 1, 1, 'C', '0', '0', '', 'ri:shining-2-line', 'admin', '2026-06-23 16:40:46', 'admin', '2026-07-01 17:27:32', 'SPU 列表 + 分类树');
INSERT INTO `sys_menu` VALUES (142, '发布商品', 129, 2, 'publish', '/mall/pms/product/publish/index', NULL, 'PublishSPU', 1, 1, 'C', '0', '0', '', 'heroicons-outline:annotation', 'admin', '2026-06-23 16:43:05', 'admin', '2026-07-01 17:27:32', '跳转 SPU 发布向导');
INSERT INTO `sys_menu` VALUES (143, '商品管理', 129, 3, 'manager', '/mall/pms/product/manager/index', NULL, 'GoodsManager', 1, 1, 'C', '0', '0', 'mall:product:query', 'ep:apple', 'admin', '2026-06-23 16:44:11', 'admin', '2026-07-01 17:27:32', 'SKU 只读检索');
INSERT INTO `sys_menu` VALUES (144, 'SKU 管理', 129, 4, 'sku', '/mall/pms/sku/index', NULL, 'PmsSkuManager', 1, 1, 'C', '1', '1', 'mall:product:list', 'mdi:barcode', 'admin', '2026-06-24 13:11:09', 'admin', '2026-07-01 17:28:25', 'PMS SKU 独立管理 [已合并至「商品管理」]');
INSERT INTO `sys_menu` VALUES (145, '属性分组查询', 138, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:query', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu` VALUES (146, '属性分组新增', 138, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:add', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu` VALUES (147, '属性分组修改', 138, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:edit', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu` VALUES (148, '属性分组删除', 138, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:delete', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu` VALUES (149, '属性分组导出', 138, 5, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:group:export', '#', 'admin', '2026-05-18 15:42:26', '', NULL, '');
INSERT INTO `sys_menu` VALUES (150, '属性分组导入', 138, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:group:import', '#', 'admin', '2026-05-18 19:46:08', '', NULL, '属性分组导入');
INSERT INTO `sys_menu` VALUES (151, '基本属性查询', 139, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:query', '#', 'admin', '2026-05-18 16:29:55', 'admin', '2026-05-18 16:45:18', '基本属性查询');
INSERT INTO `sys_menu` VALUES (152, '基础属性添加', 139, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:add', '#', 'admin', '2026-05-18 16:45:44', '', NULL, '基础属性添加');
INSERT INTO `sys_menu` VALUES (153, '基础属性删除', 139, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:delete', '#', 'admin', '2026-05-18 16:46:07', '', NULL, '基础属性删除');
INSERT INTO `sys_menu` VALUES (154, '基础属性修改', 139, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:edit', '#', 'admin', '2026-05-18 16:46:27', 'admin', '2026-05-18 16:55:39', '基础属性修改');
INSERT INTO `sys_menu` VALUES (155, '基本规格导入', 139, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:import', '#', 'admin', '2026-05-18 19:47:10', '', NULL, '基本规格导入');
INSERT INTO `sys_menu` VALUES (156, '基本规格导出', 139, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:base:export', '#', 'admin', '2026-05-18 19:47:35', '', NULL, '基本规格导出');
INSERT INTO `sys_menu` VALUES (157, '基本属性查询', 140, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:query', '#', 'admin', '2026-05-18 16:30:37', 'admin', '2026-05-18 16:46:53', '基本属性查询');
INSERT INTO `sys_menu` VALUES (158, '基本属性添加', 140, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:add', '#', 'admin', '2026-05-18 16:47:23', '', NULL, '基本属性添加');
INSERT INTO `sys_menu` VALUES (159, '基本属性删除', 140, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:delete', '#', 'admin', '2026-05-18 16:47:47', '', NULL, '基本属性删除');
INSERT INTO `sys_menu` VALUES (160, '基本属性修改', 140, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:edit', '#', 'admin', '2026-05-18 16:48:12', 'admin', '2026-05-18 16:55:45', '基本属性修改');
INSERT INTO `sys_menu` VALUES (161, '销售属性导入', 140, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:import', '#', 'admin', '2026-05-18 19:48:01', '', NULL, '销售属性导入');
INSERT INTO `sys_menu` VALUES (162, '销售属性导出', 140, 6, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:sale:export', '#', 'admin', '2026-05-18 19:48:22', '', NULL, '销售属性导出');
INSERT INTO `sys_menu` VALUES (163, '商品查询', 141, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (164, '商品新增', 141, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:add', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (165, '商品修改', 141, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (166, '商品删除', 141, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:delete', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (167, '仓库维护', 30, 1, 'warehouse', '/mall/wms/warehouse/index', NULL, 'WareHouse', 1, 1, 'C', '0', '0', '', 'ep:office-building', 'admin', '2026-05-19 15:29:10', 'admin', '2026-06-23 16:45:58', '仓库管理');
INSERT INTO `sys_menu` VALUES (168, '地区管理', 30, 2, 'address', '/mall/wms/address/index', NULL, 'AddressManager', 1, 1, 'C', '0', '0', '', 'ep:position', 'admin', '2026-05-19 15:53:01', 'admin', '2026-05-22 17:39:15', '地区管理');
INSERT INTO `sys_menu` VALUES (169, '商品库存', 30, 3, 'sku', '/mall/wms/sku/index', NULL, '', 1, 0, 'C', '0', '0', 'mall:sku:list', 'mdi:alpha-s-box-outline', 'admin', '2026-05-22 17:38:35', 'admin', '2026-05-22 17:41:05', '商品库存菜单');
INSERT INTO `sys_menu` VALUES (170, '库存工作单', 30, 4, 'task', '/mall/wms/task/index', NULL, 'TaskManager', 1, 1, 'C', '0', '0', 'mall:task:list', 'ri:task-fill', 'admin', '2026-06-23 16:47:13', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (171, '采购单维护', 30, 5, 'PurchaseOrder', '', NULL, '', 1, 1, 'M', '0', '0', '', 'mdi:clipboard-list-outline', 'admin', '2026-06-23 16:48:04', 'admin', '2026-06-23 16:50:35', '');
INSERT INTO `sys_menu` VALUES (172, '仓库信息查询', 167, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:query', '#', 'admin', '2026-05-19 17:10:59', 'admin', '2026-05-19 17:13:03', '');
INSERT INTO `sys_menu` VALUES (173, '仓库信息新增', 167, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:add', '#', 'admin', '2026-05-19 17:11:00', 'admin', '2026-05-19 17:13:10', '');
INSERT INTO `sys_menu` VALUES (174, '仓库信息修改', 167, 3, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:edit', '#', 'admin', '2026-05-19 17:11:00', 'admin', '2026-05-19 17:13:16', '');
INSERT INTO `sys_menu` VALUES (175, '仓库信息删除', 167, 4, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:delete', '#', 'admin', '2026-05-19 17:11:00', 'admin', '2026-05-19 17:13:22', '');
INSERT INTO `sys_menu` VALUES (176, '仓库信息导出', 167, 5, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:ware:export', '#', 'admin', '2026-05-19 17:11:00', 'admin', '2026-05-19 17:13:29', '');
INSERT INTO `sys_menu` VALUES (177, 'address查询', 168, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:query', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (178, 'address新增', 168, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:add', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (179, 'address修改', 168, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:edit', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (180, 'address删除', 168, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:address:delete', '#', 'admin', '2026-05-19 17:30:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (182, '商品库存查询', 169, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:query', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (183, '商品库存新增', 169, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:add', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (184, '商品库存修改', 169, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:edit', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (185, '商品库存删除', 169, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:delete', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (186, '商品库存导出', 169, 5, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:sku:export', '#', 'admin', '2026-05-22 17:38:35', '', NULL, '');
INSERT INTO `sys_menu` VALUES (187, '工作单处理', 170, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:task:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (188, '工作单列表', 170, 2, '', '', NULL, '', 1, 1, 'F', '0', '0', 'mall:task:list', '#', 'admin', '2026-06-24 13:16:20', '', NULL, '');
INSERT INTO `sys_menu` VALUES (189, '采购需求', 171, 1, 'purchaseitem', '/mall/wms/purchaseitem/index', NULL, '', 1, 1, 'C', '0', '0', 'mall:purchase:item', 'ri:file-list-3-line', 'admin', '2026-06-23 16:49:05', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (190, '采购单', 171, 2, 'purchaseorder', '/mall/wms/purchaseorder/index', NULL, '', 1, 1, 'C', '0', '0', 'mall:purchase:list', 'ep:shopping-cart-full', 'admin', '2026-06-23 16:52:05', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (191, '采购单查询', 190, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:purchase:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (192, '采购单处理', 190, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:purchase:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (193, '优惠券管理', 31, 1, 'coupon', '/mall/sms/coupon/coupon/index', NULL, 'CouponManager', 1, 1, 'C', '0', '0', 'mall:coupon:list', 'mdi:candy', 'admin', '2026-06-23 16:56:59', 'admin', '2026-06-24 15:00:55', '');
INSERT INTO `sys_menu` VALUES (194, '发放记录', 31, 2, 'history', '/mall/sms/coupon/history/index', NULL, 'HistoryManager', 1, 1, 'C', '0', '0', 'mall:coupon:history', 'ri:coupon-2-line', 'admin', '2026-06-23 16:57:58', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (195, '专题活动', 31, 3, 'subject', '/mall/sms/coupon/subject/index', NULL, 'SubjectManager', 1, 1, 'C', '0', '0', 'mall:subject:list', 'ep:aim', 'admin', '2026-06-23 16:59:42', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (196, '秒杀活动', 31, 4, 'seckill', '/mall/sms/coupon/seckill/index', NULL, 'SeckillManager', 1, 1, 'C', '0', '0', 'mall:seckill:list', 'ri:flashlight-line', 'admin', '2026-06-23 17:00:55', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (197, '积分维护', 31, 5, 'bounds', '/mall/sms/coupon/bounds/index', NULL, 'BoundsManager', 1, 1, 'C', '0', '0', 'mall:bounds:list', 'ri:copper-coin-line', 'admin', '2026-06-23 17:02:00', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (198, '满减折扣', 31, 6, 'full', '/mall/sms/coupon/full/index', NULL, 'FullManager', 1, 1, 'C', '0', '0', 'mall:reduction:list', 'ep:discount', 'admin', '2026-06-23 17:02:43', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (199, '会员价格', 31, 7, 'memberprice', '/mall/sms/coupon/memberprice/index', NULL, 'Memberprice', 1, 1, 'C', '0', '0', 'mall:memberprice:list', 'ri:vip-crown-line', 'admin', '2026-06-23 17:03:36', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (200, '每日秒杀', 31, 8, 'seckillsession', '/mall/sms/coupon/seckillsession/index', NULL, 'Seckillsession', 1, 1, 'C', '0', '0', 'mall:seckill:session', 'ri:timer-flash-line', 'admin', '2026-06-23 17:05:50', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (201, '优惠券新增', 193, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:coupon:add', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (202, '优惠券修改', 193, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:coupon:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (203, '优惠券删除', 193, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:coupon:delete', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (204, '优惠券查询', 193, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:coupon:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (205, '专题查询', 195, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:subject:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (206, '专题新增', 195, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:subject:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (207, '专题修改', 195, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:subject:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (208, '专题删除', 195, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:subject:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (209, '秒杀查询', 196, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (210, '秒杀新增', 196, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (211, '秒杀修改', 196, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (212, '秒杀删除', 196, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (213, '积分新增', 197, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bounds:add', '#', 'admin', '2026-06-24 15:36:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (214, '积分修改', 197, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bounds:edit', '#', 'admin', '2026-06-24 15:36:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (215, '积分删除', 197, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:bounds:delete', '#', 'admin', '2026-06-24 15:36:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (216, '满减查询', 198, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:reduction:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (217, '满减新增', 198, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:reduction:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (218, '满减修改', 198, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:reduction:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (219, '满减删除', 198, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:reduction:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (220, '会员价查询', 199, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:memberprice:query', '#', 'admin', '2026-06-24 15:36:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (221, '会员价新增', 199, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:memberprice:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (222, '会员价修改', 199, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:memberprice:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (223, '会员价删除', 199, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:memberprice:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (224, '场次新增', 200, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:session:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (225, '场次修改', 200, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:session:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (226, '场次删除', 200, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:seckill:session:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (227, '订单查询', 32, 1, 'order', '/mall/oms/order/order/index', NULL, 'OrderQuery', 1, 1, 'C', '0', '0', 'mall:order:list', 'ep:shopping-cart', 'admin', '2026-06-23 17:08:34', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (228, '退货单处理', 32, 2, 'return', '/mall/oms/order/return/index', NULL, 'OrderReturn', 1, 1, 'C', '0', '0', 'mall:return:list', 'ep:refresh-left', 'admin', '2026-06-23 17:09:12', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (229, '等级规则', 32, 3, 'level', '/mall/oms/order/level/index', NULL, 'LevelSettings', 1, 1, 'C', '0', '0', 'mall:order:setting', 'ri:medal-line', 'admin', '2026-06-23 17:10:11', 'admin', '2026-06-24 15:36:48', '');
INSERT INTO `sys_menu` VALUES (230, '支付流水查询', 32, 4, 'payment', '/mall/oms/order/payment/index', NULL, 'PaymentLog', 1, 1, 'C', '0', '0', 'mall:payment:list', 'ep:wallet', 'admin', '2026-06-23 17:11:01', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (231, '退款流水查询', 32, 5, 'refund', '/mall/oms/order/refund/index', NULL, 'RefundLog', 1, 1, 'C', '0', '0', 'mall:refund:list', 'ri:refund-2-line', 'admin', '2026-06-23 17:11:42', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (232, '订单查询', 227, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:order:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (233, '订单发货', 227, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:order:deliver', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (234, '关闭订单', 227, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:order:close', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (235, '退货审核', 228, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:return:audit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (236, '退货查询', 228, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:return:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (237, '支付流水查询', 230, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:payment:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (238, '退款流水查询', 231, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:refund:query', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (239, '会员列表', 33, 1, 'member', '/mall/ums/member/member/index', NULL, 'MemberList', 1, 1, 'C', '0', '0', 'mall:member:list', 'ri:group-line', 'admin', '2026-06-23 17:13:52', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (240, '会员等级', 33, 2, 'level', '/mall/ums/member/level/index', NULL, 'MemberLevel', 1, 1, 'C', '0', '0', 'mall:member:level', 'ri:vip-line', 'admin', '2026-06-23 17:14:34', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (241, '积分变化', 33, 3, 'growth', '/mall/ums/member/growth/index', NULL, 'GrowthRecord', 1, 1, 'C', '0', '0', 'mall:member:growth', 'ri:exchange-line', 'admin', '2026-06-23 17:15:27', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (242, '统计信息', 33, 4, 'statistics', '/mall/ums/member/statistics/index', NULL, 'MemberStatistics', 1, 1, 'C', '0', '0', 'mall:member:statistics', 'ri:bar-chart-box-line', 'admin', '2026-06-23 17:16:07', 'admin', '2026-06-24 13:11:09', '');
INSERT INTO `sys_menu` VALUES (243, '会员查询', 239, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (244, '会员修改', 239, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (245, '等级新增', 240, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:level:add', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (246, '等级修改', 240, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:level:edit', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (247, '等级删除', 240, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:member:level:delete', '#', 'admin', '2026-06-24 15:36:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (248, '首页推荐', 34, 1, 'home', '/mall/cms/content/home/index', NULL, 'HomeRecommend', 1, 1, 'C', '0', '0', 'mall:adv:list', 'ri:home-smile-line', 'admin', '2026-06-23 17:18:06', 'admin', '2026-06-24 15:36:48', '');
INSERT INTO `sys_menu` VALUES (249, '分类热门', 34, 2, 'category', '/mall/cms/content/category/index', NULL, 'CategoryHot', 1, 1, 'C', '0', '0', 'mall:categoryHot:list', 'ri:fire-line', 'admin', '2026-06-23 17:18:49', 'admin', '2026-07-01 18:06:19', '首页分类热门配置');
INSERT INTO `sys_menu` VALUES (250, '评论管理', 34, 3, 'comments', '/mall/cms/content/comments/index', NULL, 'CommentManage', 1, 1, 'C', '0', '0', 'mall:comment:list', 'ri:chat-3-line', 'admin', '2026-06-23 17:19:35', 'admin', '2026-06-24 15:36:48', '');
INSERT INTO `sys_menu` VALUES (251, '轮播新增', 248, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:adv:add', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (252, '轮播修改', 248, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:adv:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (253, '轮播删除', 248, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:adv:delete', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (254, '评论查询', 250, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:comment:query', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (255, '评论修改', 250, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:comment:edit', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (256, '评论删除', 250, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:comment:delete', '#', 'admin', '2026-06-24 13:11:09', '', NULL, '');
INSERT INTO `sys_menu` VALUES (257, '模板编辑', 35, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:template:edit', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (258, '模板发布', 35, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:template:publish', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (259, '绑定编辑', 36, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:bind:edit', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (260, '待办查询', 37, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:task:query', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (261, '审批操作', 37, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:task:action', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (262, '提交审批', 37, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:instance:submit', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (263, '发起查询', 38, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:instance:query', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (264, '撤回审批', 38, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:instance:withdraw', '#', 'admin', '2026-06-26 10:49:49', '', NULL, '');
INSERT INTO `sys_menu` VALUES (270, '审批统计', 6, 5, 'statistics', '/approval/statistics/index', NULL, 'ApprovalStatistics', 1, 1, 'C', '0', '0', 'approval:statistics:query', 'mdi:chart-bar', 'admin', '2026-06-30 18:04:12', '', NULL, '审批数据看板');
INSERT INTO `sys_menu` VALUES (271, '登录日志', 33, 5, 'login-log', '/mall/ums/member/login-log/index', NULL, 'MemberLoginLog', 1, 1, 'C', '0', '0', 'mall:member:query', 'ri:login-box-line', 'admin', '2026-07-01 16:14:36', '', NULL, 'C端会员登录审计');
INSERT INTO `sys_menu` VALUES (272, '会员收藏', 33, 6, 'collect', '/mall/ums/member/collect/index', NULL, 'MemberCollect', 1, 1, 'C', '0', '0', 'mall:member:query', 'ri:heart-line', 'admin', '2026-07-01 16:14:36', '', NULL, '商品/专题收藏查询');
INSERT INTO `sys_menu` VALUES (306, '商品查询', 143, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:product:query', '#', 'admin', '2026-07-01 17:22:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (310, '分类热门新增', 249, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:categoryHot:add', '#', 'admin', '2026-07-01 17:57:19', '', NULL, '');
INSERT INTO `sys_menu` VALUES (311, '分类热门修改', 249, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:categoryHot:edit', '#', 'admin', '2026-07-01 17:57:19', '', NULL, '');
INSERT INTO `sys_menu` VALUES (312, '分类热门删除', 249, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:categoryHot:delete', '#', 'admin', '2026-07-01 17:57:19', '', NULL, '');
INSERT INTO `sys_menu` VALUES (313, '通知查询', 37, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:notification:query', '#', 'admin', '2026-07-04 20:00:00', '', NULL, '审批站内通知查询');
INSERT INTO `sys_menu` VALUES (314, '通知已读', 37, 5, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:notification:edit', '#', 'admin', '2026-07-04 20:00:00', '', NULL, '审批站内通知标记已读');
INSERT INTO `sys_menu` VALUES (315, '通知查询', 38, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:notification:query', '#', 'admin', '2026-07-04 20:00:00', '', NULL, '审批站内通知查询');
INSERT INTO `sys_menu` VALUES (316, '通知已读', 38, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:notification:edit', '#', 'admin', '2026-07-04 20:00:00', '', NULL, '审批站内通知标记已读');
INSERT INTO `sys_menu` VALUES (317, '收货地址', 33, 7, 'receive-address', '/mall/ums/member/receive-address/index', NULL, 'MemberReceiveAddress', 1, 1, 'C', '0', '0', 'mall:member:query', 'ri:map-pin-line', 'admin', '2026-07-05 11:00:04', '', NULL, 'C端会员收货地址查询');
INSERT INTO `sys_menu` VALUES (318, '退款操作', 231, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'mall:refund:edit', '#', 'admin', '2026-07-05 13:30:00', '', NULL, '同步/重试退款及告警确认');
INSERT INTO `sys_menu` VALUES (319, '消息中心', 1, 9, 'message', '/system/message/index', NULL, 'MessageCenter', 1, 0, 'C', '0', '0', 'system:message:list', 'ri:mail-line', 'admin', '2026-07-05 14:15:38', '', NULL, '全平台站内消息收件箱');
INSERT INTO `sys_menu` VALUES (320, '消息查询', 319, 1, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:message:query', '#', 'admin', '2026-07-05 14:15:38', '', NULL, '站内消息查询');
INSERT INTO `sys_menu` VALUES (321, '消息已读', 319, 2, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:message:edit', '#', 'admin', '2026-07-05 14:15:38', '', NULL, '站内消息标记已读');
INSERT INTO `sys_menu` VALUES (322, '消息群发', 319, 3, '#', '', NULL, '', 1, 0, 'F', '0', '0', 'system:message:send', '#', 'admin', '2026-07-05 14:31:54', '', NULL, '管理员群发站内消息');
INSERT INTO `sys_menu` VALUES (323, '物流管理', 0, 7, '/tms', '', NULL, 'TmsCenter', 1, 1, 'M', '0', '0', '', 'mdi:truck-delivery-outline', 'admin', '2026-07-05 18:07:55', '', NULL, 'TMS 运输物流管理');
INSERT INTO `sys_menu` VALUES (324, '承运商配置', 323, 1, 'carrier', '/tms/carrier/index', NULL, 'TmsCarrier', 1, 1, 'C', '0', '0', 'tms:carrier:query', 'ep:van', 'admin', '2026-07-05 18:07:55', '', NULL, '承运商主数据');
INSERT INTO `sys_menu` VALUES (325, '运单管理', 323, 2, 'shipment', '/tms/shipment/index', NULL, 'TmsShipment', 1, 1, 'C', '0', '0', 'tms:shipment:query', 'mdi:package-variant-closed', 'admin', '2026-07-05 18:07:55', '', NULL, '运单与轨迹');
INSERT INTO `sys_menu` VALUES (326, '承运商查询', 324, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:carrier:query', '#', 'admin', '2026-07-05 18:07:56', '', NULL, '');
INSERT INTO `sys_menu` VALUES (327, '承运商编辑', 324, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:carrier:edit', '#', 'admin', '2026-07-05 18:07:56', '', NULL, '');
INSERT INTO `sys_menu` VALUES (328, '运单查询', 325, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:shipment:query', '#', 'admin', '2026-07-05 18:07:56', '', NULL, '');
INSERT INTO `sys_menu` VALUES (329, '运单发货', 325, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:shipment:ship', '#', 'admin', '2026-07-05 18:07:56', '', NULL, '');
INSERT INTO `sys_menu` VALUES (330, '轨迹刷新', 325, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:shipment:track', '#', 'admin', '2026-07-05 18:07:56', '', NULL, '');
INSERT INTO `sys_menu` VALUES (331, '运费规则', 323, 3, 'freight', '/tms/freight/index', NULL, 'TmsFreight', 1, 1, 'C', '0', '0', 'tms:freight:query', 'mdi:scale-balance', 'admin', '2026-07-05 18:18:54', '', NULL, 'TMS运费规则');
INSERT INTO `sys_menu` VALUES (332, '运费查询', 331, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:freight:query', '#', 'admin', '2026-07-05 18:18:54', '', NULL, '');
INSERT INTO `sys_menu` VALUES (333, '运费编辑', 331, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:freight:edit', '#', 'admin', '2026-07-05 18:18:54', '', NULL, '');
INSERT INTO `sys_menu` VALUES (334, 'AI 中心', 0, 8, '/ai', '', NULL, 'AiCenter', 1, 1, 'M', '0', '0', '', 'ri:robot-2-line', 'admin', '2026-07-06 10:24:17', '', NULL, 'AI 智能对话管理');
INSERT INTO `sys_menu` VALUES (335, '基础配置', 334, 1, 'config', '/ai/config/index', NULL, 'AiConfig', 1, 1, 'C', '0', '0', 'ai:config:query', 'ep:setting', 'admin', '2026-07-06 10:24:17', '', NULL, 'AI 助手基础配置');
INSERT INTO `sys_menu` VALUES (336, '配置查询', 335, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:config:query', '#', 'admin', '2026-07-06 10:24:17', '', NULL, '');
INSERT INTO `sys_menu` VALUES (337, '配置编辑', 335, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:config:edit', '#', 'admin', '2026-07-06 10:24:17', '', NULL, '');
INSERT INTO `sys_menu` VALUES (338, '配置删除', 335, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:config:delete', '#', 'admin', '2026-07-06 10:24:17', '', NULL, '');
INSERT INTO `sys_menu` VALUES (339, '会话管理', 334, 2, 'session', '/ai/session/index', NULL, 'AiSession', 1, 1, 'C', '0', '0', 'ai:session:query', 'ri:chat-history-line', 'admin', '2026-07-06 10:30:18', '', NULL, 'AI 对话会话管理');
INSERT INTO `sys_menu` VALUES (340, '会话查询', 339, 1, '', '', NULL, '', 1, 1, 'F', '0', '0', 'ai:session:query', '#', 'admin', '2026-07-06 10:30:18', 'admin', '2026-07-06 10:37:14', '');
INSERT INTO `sys_menu` VALUES (341, '会话删除', 339, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:session:delete', '#', 'admin', '2026-07-06 10:30:18', '', NULL, '');
INSERT INTO `sys_menu` VALUES (342, '知识库', 334, 3, 'knowledge', '/ai/knowledge/index', NULL, 'AiKnowledge', 1, 1, 'C', '0', '0', 'ai:knowledge:query', 'ri:book-open-line', 'admin', '2026-07-06 10:39:22', '', NULL, 'AI 知识库管理');
INSERT INTO `sys_menu` VALUES (343, '知识库查询', 342, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:knowledge:query', '#', 'admin', '2026-07-06 10:39:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (344, '知识库编辑', 342, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:knowledge:edit', '#', 'admin', '2026-07-06 10:39:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (345, '知识库删除', 342, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:knowledge:delete', '#', 'admin', '2026-07-06 10:39:22', '', NULL, '');
INSERT INTO `sys_menu` VALUES (346, '用量统计', 334, 4, 'statistics', '/ai/statistics/index', NULL, 'AiStatistics', 1, 1, 'C', '0', '0', 'ai:statistics:query', 'ri:bar-chart-box-line', 'admin', '2026-07-06 10:39:22', '', NULL, 'AI 调用用量统计');
INSERT INTO `sys_menu` VALUES (347, '统计查询', 346, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'ai:statistics:query', '#', 'admin', '2026-07-06 10:39:22', '', NULL, '');

SET FOREIGN_KEY_CHECKS = 1;
