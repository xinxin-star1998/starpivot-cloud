package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.system.domain.dto.RoleDTO;
import cn.org.starpivot.system.domain.dto.RolePermissionAssignDTO;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.mapper.*;
import cn.org.starpivot.system.service.UserPermissionCacheService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link SysRoleServiceImpl} 单元测试。
 * <p>覆盖角色 CRUD、超管保护、关联用户校验及数据权限分配等核心场景。</p>
 */
@ExtendWith(MockitoExtension.class)
class SysRoleServiceImplTest {

    @Mock
    private SysRoleMapper sysRoleMapper;
    @Mock
    private RoleMenuMapper roleMenuMapper;
    @Mock
    private RoleDeptMapper roleDeptMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private SysDeptMapper sysDeptMapper;
    @Mock
    private SysMenuMapper sysMenuMapper;
    @Mock
    private UserPermissionCacheService userPermissionCacheService;

    @InjectMocks
    private SysRoleServiceImpl sysRoleService;

    @Nested
    @DisplayName("insertRole 新增角色")
    class InsertRoleTests {

        @Test
        @DisplayName("roleKey 已存在时抛出 ROLE_KEY_EXISTS 异常")
        void duplicateRoleKey_throwsException() {
            RoleDTO dto = new RoleDTO();
            dto.setRoleName("Test Role");
            dto.setRoleKey("test_role");

            when(sysRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

            BizException ex = assertThrows(BizException.class, () -> sysRoleService.insertRole(dto));
            assertEquals(ErrorCode.ROLE_KEY_EXISTS, ex.getCode());
        }

        @Test
        @DisplayName("正常新增角色（无菜单）")
        void normalInsert_succeeds() {
            RoleDTO dto = new RoleDTO();
            dto.setRoleName("Test Role");
            dto.setRoleKey("test_role");

            when(sysRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
            when(sysRoleMapper.insert(any(SysRole.class))).thenReturn(1);

            boolean result = sysRoleService.insertRole(dto);

            assertTrue(result);
            verify(sysRoleMapper).insert(any(SysRole.class));
        }
    }

    @Nested
    @DisplayName("updateRole 更新角色")
    class UpdateRoleTests {

        @Test
        @DisplayName("角色不存在时抛出 ROLE_NOT_FOUND 异常")
        void roleNotFound_throwsException() {
            RoleDTO dto = new RoleDTO();
            dto.setRoleId(999L);
            dto.setRoleKey("test");

            when(sysRoleMapper.selectById(999L)).thenReturn(null);

            BizException ex = assertThrows(BizException.class, () -> sysRoleService.updateRole(dto));
            assertEquals(ErrorCode.ROLE_NOT_FOUND, ex.getCode());
        }

        @Test
        @DisplayName("修改超级管理员 roleKey 时抛出 ROLE_ADMIN_PROTECTED 异常")
        void modifyAdminRoleKey_throwsException() {
            RoleDTO dto = new RoleDTO();
            dto.setRoleId(1L);
            dto.setRoleKey("new_key"); // 试图修改 admin 的 roleKey

            SysRole adminRole = new SysRole();
            adminRole.setRoleId(1L);
            adminRole.setRoleKey(AppConstants.ADMIN_ROLE_KEY);
            adminRole.setDelFlag("0");

            when(sysRoleMapper.selectById(1L)).thenReturn(adminRole);
            when(sysRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

            BizException ex = assertThrows(BizException.class, () -> sysRoleService.updateRole(dto));
            assertEquals(ErrorCode.ROLE_ADMIN_PROTECTED, ex.getCode());
        }
    }

    @Nested
    @DisplayName("deleteRoleByIds 删除角色")
    class DeleteRoleTests {

        @Test
        @DisplayName("空列表返回 false")
        void emptyList_returnsFalse() {
            boolean result = sysRoleService.deleteRoleByIds(Collections.emptyList());
            assertFalse(result);
        }

        @Test
        @DisplayName("删除超级管理员角色时抛出 ROLE_ADMIN_PROTECTED 异常")
        void deleteAdmin_throwsException() {
            SysRole adminRole = new SysRole();
            adminRole.setRoleId(1L);
            adminRole.setRoleKey(AppConstants.ADMIN_ROLE_KEY);
            adminRole.setDelFlag("0");

            when(sysRoleMapper.selectBatchIds(List.of(1L))).thenReturn(List.of(adminRole));

            BizException ex = assertThrows(BizException.class, () -> sysRoleService.deleteRoleByIds(List.of(1L)));
            assertEquals(ErrorCode.ROLE_ADMIN_PROTECTED, ex.getCode());
        }

        @Test
        @DisplayName("删除已被用户使用的角色时抛出 ROLE_USED 异常")
        void deleteUsedRole_throwsException() {
            SysRole role = new SysRole();
            role.setRoleId(2L);
            role.setRoleKey("normal_role");
            role.setRoleName("普通角色");
            role.setDelFlag("0");

            when(sysRoleMapper.selectBatchIds(List.of(2L))).thenReturn(List.of(role));
            when(userRoleMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(5L);

            BizException ex = assertThrows(BizException.class, () -> sysRoleService.deleteRoleByIds(List.of(2L)));
            assertEquals(ErrorCode.ROLE_USED, ex.getCode());
        }
    }

    @Nested
    @DisplayName("assignPermission 分配数据权限")
    class AssignPermissionTests {

        @Test
        @DisplayName("角色不存在时抛出 ROLE_NOT_FOUND 异常")
        void roleNotFound_throwsException() {
            RolePermissionAssignDTO dto = new RolePermissionAssignDTO();
            dto.setRoleId(999L);
            dto.setDataScope("1");

            when(sysRoleMapper.selectById(999L)).thenReturn(null);

            BizException ex = assertThrows(BizException.class, () -> sysRoleService.assignPermission(dto));
            assertEquals(ErrorCode.ROLE_NOT_FOUND, ex.getCode());
        }

        @Test
        @DisplayName("分配自定义数据权限并清除用户权限缓存")
        void assignCustomScope_succeedsAndClearsCache() {
            RolePermissionAssignDTO dto = new RolePermissionAssignDTO();
            dto.setRoleId(2L);
            dto.setDataScope(AppConstants.DataScope.CUSTOM);
            dto.setDeptIds(Arrays.asList(100L, 101L));

            SysRole role = new SysRole();
            role.setRoleId(2L);
            role.setRoleKey("normal_role");
            role.setDelFlag("0");

            when(sysRoleMapper.selectById(2L)).thenReturn(role);
            when(sysRoleMapper.updateById(any(SysRole.class))).thenReturn(1);

            boolean result = sysRoleService.assignPermission(dto);

            assertTrue(result);
            verify(roleDeptMapper).deleteByRoleId(2L);
            verify(roleDeptMapper).batchSave(eq(2L), eq(Arrays.asList(100L, 101L)));
            verify(userPermissionCacheService).clearAllUserPermissionCache();
        }
    }

    @Nested
    @DisplayName("changeRoleStatus 修改角色状态")
    class ChangeRoleStatusTests {

        @Test
        @DisplayName("停用超级管理员时抛出 ROLE_ADMIN_PROTECTED 异常")
        void disableAdmin_throwsException() {
            SysRole adminRole = new SysRole();
            adminRole.setRoleId(1L);
            adminRole.setRoleKey(AppConstants.ADMIN_ROLE_KEY);
            adminRole.setDelFlag("0");

            when(sysRoleMapper.selectById(1L)).thenReturn(adminRole);

            BizException ex = assertThrows(BizException.class,
                    () -> sysRoleService.changeRoleStatus(1L, "1"));
            assertEquals(ErrorCode.ROLE_ADMIN_PROTECTED, ex.getCode());
        }
    }
}
