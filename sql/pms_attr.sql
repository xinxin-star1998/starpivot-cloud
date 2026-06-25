/*
 商品属性种子数据 — 手机三级分类 (catelog_id = 225)

 约定：
   - attr_type：0=销售属性，1=规格参数（基本属性）
   - value_type：0=手动输入，1=多选
   - value_select：可选值以分号分隔（与前端 attr-value-select.ts 一致）
   - show_desc：是否在 SPU 介绍页快速展示（0=否，1=是）

 销售属性（66~69）不参与属性分组；规格参数通过 pms_attr_attrgroup_relation 关联分组。

 关联脚本：
   - pms_sku_sale_attr_value.sql         — 新环境种子数据（已迁移）
   - pms_sku_sale_attr_value_migrate.sql — 旧库增量迁移（attr_id 9/12 → 66/67/68）
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for pms_attr
-- ----------------------------
DROP TABLE IF EXISTS `pms_attr`;
CREATE TABLE `pms_attr` (
  `attr_id`      bigint       NOT NULL AUTO_INCREMENT COMMENT '属性id',
  `attr_name`    char(30)     NULL DEFAULT NULL COMMENT '属性名',
  `search_type`  tinyint      NULL DEFAULT NULL COMMENT '是否需要检索[0-不需要，1-需要]',
  `value_type`   tinyint      NULL DEFAULT NULL COMMENT '值类型[0-手动输入，1-多选]',
  `icon`         varchar(255) NULL DEFAULT NULL COMMENT '属性图标',
  `value_select` char(255)    NULL DEFAULT NULL COMMENT '可选值列表[用分号分隔]',
  `attr_type`    tinyint      NULL DEFAULT NULL COMMENT '属性类型[0-销售属性，1-规格参数]',
  `enable`       bigint       NULL DEFAULT NULL COMMENT '启用状态[0-禁用，1-启用]',
  `catelog_id`   bigint       NULL DEFAULT NULL COMMENT '所属分类',
  `show_desc`    tinyint      NULL DEFAULT NULL COMMENT '快速展示[0-否，1-是]',
  PRIMARY KEY (`attr_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 70 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '商品属性' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 规格参数（基本属性）attr_type = 1
-- ----------------------------

-- 基本信息
INSERT INTO `pms_attr`
  (`attr_id`, `attr_name`, `search_type`, `value_type`, `icon`, `value_select`, `attr_type`, `enable`, `catelog_id`, `show_desc`)
VALUES
  (17, '入网型号',       0, 0, '', NULL,                                              1, 1, 225, 0),
  (18, '上市年份',       0, 1, '', '2020;2021;2022;2023;2024;2025;2026',              1, 1, 225, 0),
  (19, '上市月份',       0, 1, '', '1月;2月;3月;4月;5月;6月;7月;8月;9月;10月;11月;12月', 1, 1, 225, 0);

-- 外观与尺寸
INSERT INTO `pms_attr`
  (`attr_id`, `attr_name`, `search_type`, `value_type`, `icon`, `value_select`, `attr_type`, `enable`, `catelog_id`, `show_desc`)
VALUES
  (20, '机身材质工艺',   0, 1, '', '玻璃;金属;素皮;陶瓷;塑料',                        1, 1, 225, 0),
  (21, '机身长度',       0, 0, '', NULL,                                              1, 1, 225, 0),
  (22, '机身宽度',       0, 0, '', NULL,                                              1, 1, 225, 0),
  (23, '机身厚度',       0, 0, '', NULL,                                              1, 1, 225, 0),
  (24, '机身重量',       0, 0, '', NULL,                                              1, 1, 225, 0),
  (25, '防水等级',       0, 1, '', 'IP53;IP67;IP68;不支持',                           1, 1, 225, 0);

-- 主芯片
INSERT INTO `pms_attr`
  (`attr_id`, `attr_name`, `search_type`, `value_type`, `icon`, `value_select`, `attr_type`, `enable`, `catelog_id`, `show_desc`)
VALUES
  (26, 'CPU品牌',        1, 1, '', '高通;联发科;华为麒麟;苹果A系列',                  1, 1, 225, 1),
  (27, 'CPU型号',        1, 1, '', '骁龙8 Gen3;骁龙8 Gen2;天玑9300;天玑9200;麒麟9000;A17;A16', 1, 1, 225, 1),
  (28, 'CPU核心数',      0, 1, '', '八核;六核',                                       1, 1, 225, 0),
  (29, 'CPU主频',        0, 0, '', NULL,                                              1, 1, 225, 0),
  (30, 'GPU型号',        0, 0, '', NULL,                                              1, 1, 225, 0);

-- 屏幕
INSERT INTO `pms_attr`
  (`attr_id`, `attr_name`, `search_type`, `value_type`, `icon`, `value_select`, `attr_type`, `enable`, `catelog_id`, `show_desc`)
VALUES
  (31, '屏幕尺寸',       1, 0, '', NULL,                                              1, 1, 225, 1),
  (32, '屏幕类型',       0, 1, '', 'OLED;AMOLED;LCD',                                 1, 1, 225, 0),
  (33, '分辨率',         1, 0, '', NULL,                                              1, 1, 225, 1),
  (34, '屏幕刷新率',     1, 1, '', '60Hz;90Hz;120Hz;144Hz',                           1, 1, 225, 1),
  (35, '触控采样率',     0, 1, '', '120Hz;240Hz;480Hz',                               1, 1, 225, 0),
  (36, '屏幕亮度',       0, 0, '', NULL,                                              1, 1, 225, 0);

-- 电池与充电
INSERT INTO `pms_attr`
  (`attr_id`, `attr_name`, `search_type`, `value_type`, `icon`, `value_select`, `attr_type`, `enable`, `catelog_id`, `show_desc`)
VALUES
  (37, '电池容量',       1, 0, '', NULL,                                              1, 1, 225, 1),
  (38, '有线快充功率',   1, 1, '', '20W;33W;67W;80W;100W;120W',                       1, 1, 225, 1),
  (39, '无线快充功率',   0, 1, '', '15W;30W;50W;不支持',                              1, 1, 225, 0),
  (40, '电池类型',       0, 1, '', '不可拆卸',                                          1, 1, 225, 0),
  (41, '反向充电',       0, 1, '', '支持;不支持',                                     1, 1, 225, 0);

-- 摄像头
INSERT INTO `pms_attr`
  (`attr_id`, `attr_name`, `search_type`, `value_type`, `icon`, `value_select`, `attr_type`, `enable`, `catelog_id`, `show_desc`)
VALUES
  (42, '后置主摄像素',   1, 0, '', NULL,                                              1, 1, 225, 1),
  (43, '后置超广角像素', 0, 0, '', NULL,                                              1, 1, 225, 0),
  (44, '后置长焦像素',   0, 0, '', NULL,                                              1, 1, 225, 0),
  (45, '前置摄像头像素', 1, 0, '', NULL,                                              1, 1, 225, 1),
  (46, '视频拍摄规格',   0, 1, '', '1080P;4K;8K',                                     1, 1, 225, 0),
  (47, '闪光灯类型',     0, 1, '', 'LED;双LED',                                       1, 1, 225, 0);

-- 网络与连接
INSERT INTO `pms_attr`
  (`attr_id`, `attr_name`, `search_type`, `value_type`, `icon`, `value_select`, `attr_type`, `enable`, `catelog_id`, `show_desc`)
VALUES
  (48, '网络类型',       1, 1, '', '5G;4G;3G',                                        1, 1, 225, 1),
  (49, '运营商支持',     1, 1, '', '全网通;移动;联通;电信',                            1, 1, 225, 1),
  (50, 'SIM卡类型',      0, 1, '', '单卡;双卡',                                        1, 1, 225, 0),
  (51, '蓝牙版本',       0, 1, '', '5.0;5.2;5.3;5.4',                                 1, 1, 225, 0),
  (52, 'Wi-Fi规格',      0, 1, '', 'Wi-Fi 5;Wi-Fi 6;Wi-Fi 6E',                        1, 1, 225, 0),
  (53, 'NFC功能',        0, 1, '', '支持;不支持',                                     1, 1, 225, 0),
  (54, '红外遥控',       0, 1, '', '支持;不支持',                                     1, 1, 225, 0),
  (55, 'USB接口类型',    0, 1, '', 'Type-C;Lightning',                                1, 1, 225, 0),
  (56, '定位系统',       0, 1, '', 'GPS;北斗;GLONASS',                                1, 1, 225, 0);

-- 系统与功能
INSERT INTO `pms_attr`
  (`attr_id`, `attr_name`, `search_type`, `value_type`, `icon`, `value_select`, `attr_type`, `enable`, `catelog_id`, `show_desc`)
VALUES
  (57, '操作系统',       1, 1, '', 'iOS;Android;HarmonyOS',                           1, 1, 225, 1),
  (58, '系统UI',         0, 1, '', 'MIUI;ColorOS;OriginOS;EMUI',                      1, 1, 225, 0),
  (59, '指纹识别',       0, 1, '', '屏下;侧边;后置',                                    1, 1, 225, 0),
  (60, '面部识别',       0, 1, '', '支持;不支持',                                     1, 1, 225, 0),
  (61, '扬声器类型',     0, 1, '', '单扬声器;双扬声器',                                 1, 1, 225, 0),
  (62, '3.5mm耳机孔',    0, 1, '', '支持;不支持',                                     1, 1, 225, 0);

-- 其他
INSERT INTO `pms_attr`
  (`attr_id`, `attr_name`, `search_type`, `value_type`, `icon`, `value_select`, `attr_type`, `enable`, `catelog_id`, `show_desc`)
VALUES
  (63, '散热方式',       0, 0, '', NULL,                                              1, 1, 225, 0),
  (64, '产地',           0, 0, '', NULL,                                              1, 1, 225, 0),
  (65, '保修期',         0, 1, '', '1年;2年',                                          1, 1, 225, 0);

-- ----------------------------
-- 销售属性 attr_type = 0
-- 对应旧谷粒商城 attr_id=9「颜色」、12「版本(存储)」的规范化替代：
--   9  颜色        → 66 机身颜色
--   12 版本(存储)  → 68 机身存储(ROM)；运行内存见 67
-- ----------------------------
INSERT INTO `pms_attr`
  (`attr_id`, `attr_name`, `search_type`, `value_type`, `icon`, `value_select`, `attr_type`, `enable`, `catelog_id`, `show_desc`)
VALUES
  (66, '机身颜色',       1, 1, '', '黑色;白色;蓝色;绿色;紫色;金色;粉色;灰色;素皮黑;黄色;红色;星河银;亮黑色;翡冷翠;罗兰紫', 0, 1, 225, 1),
  (67, '运行内存(RAM)',  1, 1, '', '4GB;6GB;8GB;12GB;16GB;18GB',                      0, 1, 225, 1),
  (68, '机身存储(ROM)',  1, 1, '', '64GB;128GB;256GB;512GB;1TB',                      0, 1, 225, 1),
  (69, '机型版本',       1, 1, '', '标准版;Pro版;Pro+;Ultra版;SE版',                  0, 1, 225, 0);

SET FOREIGN_KEY_CHECKS = 1;
