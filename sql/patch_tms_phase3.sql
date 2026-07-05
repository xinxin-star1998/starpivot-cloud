-- TMS Phase 3: 运费规则表
USE `star_pivot_tms`;

CREATE TABLE IF NOT EXISTS `tms_freight_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `rule_name` varchar(64) NOT NULL COMMENT '规则名称',
  `rule_type` varchar(16) NOT NULL COMMENT 'FIXED/WEIGHT',
  `default_flag` char(1) NOT NULL DEFAULT '0' COMMENT '1默认规则',
  `base_fee` decimal(10,2) DEFAULT NULL COMMENT '固定运费(FIXED)',
  `first_weight_kg` decimal(10,3) DEFAULT NULL COMMENT '首重kg(WEIGHT)',
  `first_fee` decimal(10,2) DEFAULT NULL COMMENT '首重费用(WEIGHT)',
  `continue_fee_per_kg` decimal(10,2) DEFAULT NULL COMMENT '续重单价/kg(WEIGHT)',
  `status` char(1) NOT NULL DEFAULT '0' COMMENT '0正常 1停用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `del_flag` char(1) NOT NULL DEFAULT '0' COMMENT '0存在 2删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='TMS运费规则';

INSERT INTO `tms_freight_rule` (`rule_name`, `rule_type`, `default_flag`, `base_fee`, `first_weight_kg`, `first_fee`, `continue_fee_per_kg`, `status`, `sort_order`, `remark`)
SELECT '默认固定运费', 'FIXED', '1', 10.00, NULL, NULL, NULL, '0', 1, '与商城原 defaultFreight 一致'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `tms_freight_rule` WHERE `default_flag` = '1' LIMIT 1);

INSERT INTO `tms_freight_rule` (`rule_name`, `rule_type`, `default_flag`, `base_fee`, `first_weight_kg`, `first_fee`, `continue_fee_per_kg`, `status`, `sort_order`, `remark`)
SELECT '按重量计费', 'WEIGHT', '0', NULL, 1.000, 8.00, 2.00, '0', 2, '首重1kg 8元，续重2元/kg'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `tms_freight_rule` WHERE `rule_type` = 'WEIGHT' LIMIT 1);

-- 菜单（star_pivot 库）
USE `star_pivot`;

INSERT INTO `sys_menu` VALUES (331, '运费规则', 323, 3, 'freight', '/tms/freight/index', NULL, 'TmsFreight', 1, 1, 'C', '0', '0', 'tms:freight:query', 'mdi:scale-balance', 'admin', NOW(), '', NULL, 'TMS运费规则')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (332, '运费查询', 331, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:freight:query', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (333, '运费编辑', 331, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:freight:edit', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES (1, 331), (1, 332), (1, 333);
