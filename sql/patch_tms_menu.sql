-- TMS 菜单与权限（执行于 star_pivot 库）
USE `star_pivot`;

INSERT INTO `sys_menu` VALUES (323, '物流管理', 0, 7, '/tms', '', NULL, 'TmsCenter', 1, 1, 'M', '0', '0', '', 'mdi:truck-delivery-outline', 'admin', NOW(), '', NULL, 'TMS 运输物流管理')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (324, '承运商配置', 323, 1, 'carrier', '/tms/carrier/index', NULL, 'TmsCarrier', 1, 1, 'C', '0', '0', 'tms:carrier:query', 'ep:van', 'admin', NOW(), '', NULL, '承运商主数据')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (325, '运单管理', 323, 2, 'shipment', '/tms/shipment/index', NULL, 'TmsShipment', 1, 1, 'C', '0', '0', 'tms:shipment:query', 'mdi:package-variant-closed', 'admin', NOW(), '', NULL, '运单与轨迹')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (326, '承运商查询', 324, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:carrier:query', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (327, '承运商编辑', 324, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:carrier:edit', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (328, '运单查询', 325, 1, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:shipment:query', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (329, '运单发货', 325, 2, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:shipment:ship', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

INSERT INTO `sys_menu` VALUES (330, '轨迹刷新', 325, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'tms:shipment:track', '#', 'admin', NOW(), '', NULL, '')
ON DUPLICATE KEY UPDATE menu_name = VALUES(menu_name);

-- admin 角色绑定 TMS 菜单
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 323), (1, 324), (1, 325), (1, 326), (1, 327), (1, 328), (1, 329), (1, 330);
