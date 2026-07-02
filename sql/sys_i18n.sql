/*
 StarPivot 多语言模块

 包含：
 1. sys_i18n       - 静态词条表（UI 文案、菜单 key、错误码文案等）
 2. sys_i18n_biz   - 动态业务翻译表（商品、公告、分类等 DB 内容）

 lang_key 约定：点分路径，与前端 vue-i18n 一致，如 common.confirm、menus.system.user
 动态菜单：sys_menu.menu_name 可改为 lang_key，前端 formatMenuTitle() 会自动 $t()
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_i18n
-- ----------------------------
DROP TABLE IF EXISTS `sys_i18n`;
CREATE TABLE `sys_i18n` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lang_key`    VARCHAR(200)  NOT NULL COMMENT '多语言唯一标识（如 common.confirm）',
  `module`      VARCHAR(32)   NOT NULL DEFAULT 'common' COMMENT '模块：common/system/mall/portal/error/http',
  `zh_cn`       VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '简体中文',
  `en_us`       VARCHAR(1000) NOT NULL DEFAULT '' COMMENT '英文',
  `sort`        INT           NULL DEFAULT 0 COMMENT '排序',
  `status`      CHAR(1)       NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `remark`      VARCHAR(500)  NULL DEFAULT '' COMMENT '备注说明',
  `create_by`   VARCHAR(64)   NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   VARCHAR(64)   NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_lang_key` (`lang_key`) USING BTREE,
  KEY `idx_module_status` (`module`, `status`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '系统多语言词条表'
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for sys_i18n_biz
-- ----------------------------
DROP TABLE IF EXISTS `sys_i18n_biz`;
CREATE TABLE `sys_i18n_biz` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `biz_type`    VARCHAR(32)   NOT NULL COMMENT '业务类型：spu/category/brand/notice/menu',
  `biz_id`      BIGINT        NOT NULL COMMENT '业务主键ID',
  `field_name`  VARCHAR(32)   NOT NULL COMMENT '字段名：name/title/content/description',
  `locale`      VARCHAR(10)   NOT NULL COMMENT '语言：zh-CN / en-US',
  `field_value` TEXT          NOT NULL COMMENT '翻译内容',
  `create_by`   VARCHAR(64)   NULL DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME      NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by`   VARCHAR(64)   NULL DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME      NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_biz_locale` (`biz_type`, `biz_id`, `field_name`, `locale`) USING BTREE,
  KEY `idx_biz_type_id` (`biz_type`, `biz_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = '业务多语言翻译表'
  ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 初始化数据：sys_i18n（示例词条，可按 module 分批扩展）
-- lang_key 与 star-pivot-ui/src/locales/langs/*.json 保持一致
-- =============================================================================

-- ---------- module: common ----------
INSERT INTO `sys_i18n` (`lang_key`, `module`, `zh_cn`, `en_us`, `sort`, `remark`, `create_by`) VALUES
('common.tips',         'common', '提示',           'Prompt',                              1,  '通用-提示',     'admin'),
('common.cancel',       'common', '取消',           'Cancel',                              2,  '通用-取消',     'admin'),
('common.confirm',      'common', '确定',           'Confirm',                             3,  '通用-确定',     'admin'),
('common.logOutTips',   'common', '您是否要退出登录?', 'Do you want to log out?',          4,  '通用-退出确认', 'admin');

-- ---------- module: http（HTTP 拦截器） ----------
INSERT INTO `sys_i18n` (`lang_key`, `module`, `zh_cn`, `en_us`, `sort`, `remark`, `create_by`) VALUES
('httpMsg.unauthorized',        'http', '未授权访问，请重新登录',           'Unauthorized access, please login again',              1,  'HTTP 401', 'admin'),
('httpMsg.forbidden',           'http', '禁止访问该资源',                   'Access to this resource is forbidden',                 2,  'HTTP 403', 'admin'),
('httpMsg.notFound',            'http', '请求的资源不存在',                 'The requested resource does not exist',                3,  'HTTP 404', 'admin'),
('httpMsg.internalServerError', 'http', '服务器内部错误，请稍后重试',       'Internal server error, please try again later',        4,  'HTTP 500', 'admin'),
('httpMsg.networkError',        'http', '网络连接异常，请检查网络连接',     'Network connection error, please check your connection', 5, '网络错误', 'admin'),
('httpMsg.requestFailed',       'http', '请求失败',                         'Request failed',                                       6,  '请求失败', 'admin'),
('httpMsg.requestCancelled',    'http', '请求已取消',                       'Request cancelled',                                    7,  '请求取消', 'admin');

-- ---------- module: login ----------
INSERT INTO `sys_i18n` (`lang_key`, `module`, `zh_cn`, `en_us`, `sort`, `remark`, `create_by`) VALUES
('login.title',                    'login', '欢迎回来',                     'Welcome back',                           1,  '登录页标题',   'admin'),
('login.subTitle',                 'login', '输入您的账号和密码登录',       'Enter your account and password to login', 2, '登录页副标题', 'admin'),
('login.placeholder.username',     'login', '请输入账号',                   'Please enter username',                  3,  '账号占位符',   'admin'),
('login.placeholder.password',     'login', '请输入密码',                   'Please enter password',                  4,  '密码占位符',   'admin'),
('login.placeholder.captcha',      'login', '请输入验证码',                 'Please enter captcha',                   5,  '验证码占位符', 'admin'),
('login.btnText',                  'login', '登录',                         'Login',                                  6,  '登录按钮',     'admin'),
('login.success.title',            'login', '登录成功',                     'Login successful',                       7,  '登录成功标题', 'admin'),
('login.success.message',          'login', '欢迎回来',                     'Welcome back',                           8,  '登录成功消息', 'admin');

-- ---------- module: menus（与 sys_menu 对应，menu_name 可改为 lang_key） ----------
INSERT INTO `sys_i18n` (`lang_key`, `module`, `zh_cn`, `en_us`, `sort`, `remark`, `create_by`) VALUES
('menus.login.title',              'menus', '登录',         'Login',              1,   '静态路由-登录',       'admin'),
('menus.register.title',           'menus', '注册',         'Register',           2,   '静态路由-注册',       'admin'),
('menus.dashboard.title',          'menus', '仪表盘',       'Dashboard',          3,   '静态路由-仪表盘',     'admin'),
('menus.dashboard.console',        'menus', '工作台',       'Console',            4,   '静态路由-工作台',     'admin'),
('menus.system.title',             'menus', '系统管理',     'System Settings',    10,  '菜单ID=1',            'admin'),
('menus.system.menu',              'menus', '菜单管理',     'Menu Manage',        11,  '菜单ID=9',            'admin'),
('menus.system.role',              'menus', '角色管理',     'Role Manage',        12,  '菜单ID=10',           'admin'),
('menus.system.user',              'menus', '用户管理',     'User Manage',        13,  '菜单ID=11',           'admin'),
('menus.system.dept',              'menus', '部门管理',     'Department Manage',  14,  '菜单ID=12',           'admin'),
('menus.system.post',              'menus', '岗位管理',     'Post Manage',        15,  '菜单ID=13',           'admin'),
('menus.system.dict',              'menus', '字典管理',     'Dictionary Manage',  16,  '菜单ID=14',           'admin'),
('menus.system.notice',            'menus', '通知公告',     'Notice',             17,  '菜单ID=16',           'admin'),
('menus.system.config',            'menus', '参数配置',     'Config',             18,  '菜单ID=17',           'admin'),
('menus.tools.title',              'menus', '系统工具',     'System Tools',       20,  '菜单ID=2',            'admin'),
('menus.tools.generator',          'menus', '代码生成',     'Code Generator',     21,  '菜单ID=20',           'admin'),
('menus.monitor.title',            'menus', '系统监控',     'System Monitor',     30,  '菜单ID=3',            'admin'),
('menus.monitor.server',           'menus', '服务器监控',   'Server Monitor',     31,  '菜单ID=22',           'admin'),
('menus.monitor.druid',            'menus', 'Druid监控',    'Druid Monitor',      32,  '菜单ID=23',           'admin'),
('menus.monitor.redis',            'menus', 'Redis缓存',    'Redis Monitor',      33,  '菜单ID=24',           'admin'),
('menus.file.title',               'menus', '文件中心',     'File Center',        40,  '菜单ID=4',            'admin'),
('menus.file.manage',              'menus', '文件管理',     'File Manage',        41,  '菜单ID=27',           'admin'),
('menus.mall.title',               'menus', '商城系统',     'Mall System',        50,  '菜单ID=5',            'admin'),
('menus.mall.portal',              'menus', 'C端商城',      'Customer Portal',    51,  '菜单ID=28',           'admin'),
('menus.approval.title',           'menus', '审批中心',     'Approval Center',    60,  '菜单ID=6',            'admin');

-- ---------- module: error（与 ErrorCode 数字码对应，前端/后端共用） ----------
INSERT INTO `sys_i18n` (`lang_key`, `module`, `zh_cn`, `en_us`, `sort`, `remark`, `create_by`) VALUES
('error.400',    'error', '请求参数错误',     'Bad request',                    1,   'PARAM_ERROR',           'admin'),
('error.401',    'error', '未授权，请先登录', 'Unauthorized, please login first', 2, 'UNAUTHORIZED',          'admin'),
('error.403',    'error', '没有访问权限',     'Access denied',                  3,   'FORBIDDEN',             'admin'),
('error.404',    'error', '资源不存在',       'Resource not found',             4,   'NOT_FOUND',             'admin'),
('error.500',    'error', '系统繁忙，请稍后重试', 'System busy, please try again later', 5, 'SYSTEM_ERROR',   'admin'),
('error.50001',  'error', '用户不存在',       'User not found',                 10,  'USER_NOT_FOUND',        'admin'),
('error.50002',  'error', '用户已存在',       'User already exists',            11,  'USER_EXISTS',           'admin'),
('error.50003',  'error', '密码错误',         'Incorrect password',             12,  'USER_PASSWORD_ERROR',   'admin'),
('error.50004',  'error', '账号已锁定',       'Account locked',                 13,  'USER_ACCOUNT_LOCKED',   'admin'),
('error.60001',  'error', '角色不存在',       'Role not found',                 20,  'ROLE_NOT_FOUND',        'admin'),
('error.70001',  'error', '菜单不存在',       'Menu not found',                 30,  'MENU_NOT_FOUND',        'admin'),
('error.80001',  'error', '部门不存在',       'Department not found',           40,  'DEPT_NOT_FOUND',        'admin'),
('error.90004',  'error', '权限不足',         'Permission denied',              50,  'PERMISSION_DENIED',     'admin');

-- ---------- module: table（表格/表单通用） ----------
INSERT INTO `sys_i18n` (`lang_key`, `module`, `zh_cn`, `en_us`, `sort`, `remark`, `create_by`) VALUES
('table.searchBar.reset',   'system', '重置', 'Reset',  1, '搜索栏-重置', 'admin'),
('table.searchBar.search',  'system', '查询', 'Search', 2, '搜索栏-查询', 'admin'),
('table.form.reset',        'system', '重置', 'Reset',  3, '表单-重置',   'admin'),
('table.form.submit',       'system', '提交', 'Submit', 4, '表单-提交',   'admin');

-- =============================================================================
-- 初始化数据：sys_i18n_biz（动态业务内容示例）
-- 主表保留中文默认值，翻译表存其他语言；接口按 locale 合并返回
-- =============================================================================

-- 示例：通知公告 ID=1 的多语言（假设 sys_notice.notice_id = 1）
INSERT INTO `sys_i18n_biz` (`biz_type`, `biz_id`, `field_name`, `locale`, `field_value`, `create_by`) VALUES
('notice', 1, 'title',   'zh-CN', '系统维护通知', 'admin'),
('notice', 1, 'title',   'en-US', 'System Maintenance Notice', 'admin'),
('notice', 1, 'content', 'zh-CN', '系统将于本周六 02:00-04:00 进行维护，期间可能无法访问，请提前保存工作。', 'admin'),
('notice', 1, 'content', 'en-US', 'The system will undergo maintenance on Saturday from 02:00 to 04:00. Service may be unavailable. Please save your work in advance.', 'admin');

-- 示例：商品 SPU ID=10001 的多语言（假设 pms_spu_info.spu_id = 10001）
INSERT INTO `sys_i18n_biz` (`biz_type`, `biz_id`, `field_name`, `locale`, `field_value`, `create_by`) VALUES
('spu', 10001, 'name',        'zh-CN', '星枢智能手环 Pro', 'admin'),
('spu', 10001, 'name',        'en-US', 'StarPivot Smart Band Pro', 'admin'),
('spu', 10001, 'description', 'zh-CN', '支持心率监测、睡眠分析与 NFC 支付。', 'admin'),
('spu', 10001, 'description', 'en-US', 'Heart rate monitoring, sleep analysis, and NFC payments.', 'admin');

-- 示例：商品分类 ID=10
INSERT INTO `sys_i18n_biz` (`biz_type`, `biz_id`, `field_name`, `locale`, `field_value`, `create_by`) VALUES
('category', 10, 'name', 'zh-CN', '数码配件', 'admin'),
('category', 10, 'name', 'en-US', 'Digital Accessories', 'admin');

-- =============================================================================
-- 可选：将 sys_menu.menu_name 批量改为 lang_key（执行前请备份）
-- 仅示例部分菜单，完整迁移需按 menu_id 逐一对应 menus.* key
-- =============================================================================
/*
UPDATE sys_menu SET menu_name = 'menus.system.title'  WHERE menu_id = 1;
UPDATE sys_menu SET menu_name = 'menus.tools.title'   WHERE menu_id = 2;
UPDATE sys_menu SET menu_name = 'menus.monitor.title' WHERE menu_id = 3;
UPDATE sys_menu SET menu_name = 'menus.file.title'    WHERE menu_id = 4;
UPDATE sys_menu SET menu_name = 'menus.mall.title'    WHERE menu_id = 5;
UPDATE sys_menu SET menu_name = 'menus.approval.title' WHERE menu_id = 6;
UPDATE sys_menu SET menu_name = 'menus.system.menu'   WHERE menu_id = 9;
UPDATE sys_menu SET menu_name = 'menus.system.role'   WHERE menu_id = 10;
UPDATE sys_menu SET menu_name = 'menus.system.user'   WHERE menu_id = 11;
UPDATE sys_menu SET menu_name = 'menus.system.dept'   WHERE menu_id = 12;
UPDATE sys_menu SET menu_name = 'menus.system.post'   WHERE menu_id = 13;
UPDATE sys_menu SET menu_name = 'menus.system.dict'   WHERE menu_id = 14;
UPDATE sys_menu SET menu_name = 'menus.system.notice' WHERE menu_id = 16;
UPDATE sys_menu SET menu_name = 'menus.system.config' WHERE menu_id = 17;
UPDATE sys_menu SET menu_name = 'menus.tools.generator' WHERE menu_id = 20;
UPDATE sys_menu SET menu_name = 'menus.monitor.server'  WHERE menu_id = 22;
UPDATE sys_menu SET menu_name = 'menus.monitor.druid'   WHERE menu_id = 23;
UPDATE sys_menu SET menu_name = 'menus.monitor.redis'   WHERE menu_id = 24;
UPDATE sys_menu SET menu_name = 'menus.file.manage'     WHERE menu_id = 27;
UPDATE sys_menu SET menu_name = 'menus.mall.portal'     WHERE menu_id = 28;
*/
