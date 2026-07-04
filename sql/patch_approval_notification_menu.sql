-- 审批站内通知 RBAC 权限补丁（已有库执行）
-- 新增 approval:notification:query / approval:notification:edit

INSERT INTO `sys_menu` VALUES (313, '通知查询', 37, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:notification:query', '#', 'admin', '2026-07-04 20:00:00', '', NULL, '审批站内通知查询');
INSERT INTO `sys_menu` VALUES (314, '通知已读', 37, 5, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:notification:edit', '#', 'admin', '2026-07-04 20:00:00', '', NULL, '审批站内通知标记已读');
INSERT INTO `sys_menu` VALUES (315, '通知查询', 38, 3, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:notification:query', '#', 'admin', '2026-07-04 20:00:00', '', NULL, '审批站内通知查询');
INSERT INTO `sys_menu` VALUES (316, '通知已读', 38, 4, '', '', NULL, '', 1, 0, 'F', '0', '0', 'approval:notification:edit', '#', 'admin', '2026-07-04 20:00:00', '', NULL, '审批站内通知标记已读');

-- 已分配「待办审批」或「我发起的」的角色，自动勾选通知权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT rm.role_id, m.menu_id
FROM sys_role_menu rm
         CROSS JOIN (SELECT 313 AS menu_id UNION ALL SELECT 314 UNION ALL SELECT 315 UNION ALL SELECT 316) m
WHERE rm.menu_id IN (37, 38);
