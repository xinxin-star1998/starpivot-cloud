package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.LoginUser;
import cn.org.starpivot.common.security.SecurityUtils;
import cn.org.starpivot.system.assembler.UserVOAssembler;
import cn.org.starpivot.system.config.SysAccountProperties;
import cn.org.starpivot.system.domain.dto.UserDTO;
import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.mapper.SysUserMapper;
import cn.org.starpivot.system.mapper.UserPostMapper;
import cn.org.starpivot.system.mapper.UserRoleMapper;
import cn.org.starpivot.system.service.DataScopeService;
import cn.org.starpivot.system.service.UserPermissionCacheService;
import cn.org.starpivot.system.service.support.SysUserAuthSupport;
import cn.org.starpivot.system.service.support.SysUserExcelSupport;
import cn.org.starpivot.system.service.support.SysUserRelationSupport;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * {@link SysUserServiceImpl} 单元测试。
 * <p>覆盖用户新增、更新、状态变更、密码重置及修改等核心场景。</p>
 */
@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    private static MockedStatic<SecurityContextHolder> securityContextHolderMock;

    @Mock private SysUserMapper sysUserMapper;
    @Mock private UserRoleMapper userRoleMapper;
    @Mock private UserPostMapper userPostMapper;
    @Mock private UserPermissionCacheService userPermissionCacheService;
    @Mock private DataScopeService dataScopeService;
    @Mock private UserVOAssembler userVOAssembler;
    @Mock private TransactionTemplate transactionTemplate;
    @Mock private SecurityUtils securityUtils;
    @Mock private SysAccountProperties sysAccountProperties;
    @Mock private SysUserAuthSupport sysUserAuthSupport;
    @Mock private SysUserRelationSupport sysUserRelationSupport;
    @Mock private SysUserExcelSupport sysUserExcelSupport;

    @Spy
    private SysUserServiceImpl sysUserService;

    @BeforeAll
    static void setupSecurityContext() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(1L);
        loginUser.setUsername("admin");
        var auth = new UsernamePasswordAuthenticationToken(
                loginUser, null, List.of(new SimpleGrantedAuthority("system:user:update")));
        securityContextHolderMock = mockStatic(SecurityContextHolder.class);
        securityContextHolderMock.when(SecurityContextHolder::getContext)
                .thenReturn(new org.springframework.security.core.context.SecurityContextImpl(auth));
    }

    @AfterAll
    static void tearDownSecurityContext() {
        if (securityContextHolderMock != null) {
            securityContextHolderMock.close();
        }
    }

    // ==================== addUser ====================

    @Nested
    @DisplayName("addUser 新增用户")
    class AddUserTests {

        @Test
        @DisplayName("用户名已存在时抛出 USER_USERNAME_EXISTS 异常")
        void duplicateUsername_throwsException() {
            UserDTO dto = new UserDTO();
            dto.setUserName("existUser");
            dto.setNickName("Nick");

            SysUser existing = new SysUser();
            existing.setUserId(10L);
            when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

            BizException ex = assertThrows(BizException.class, () -> sysUserService.addUser(dto));
            assertEquals(ErrorCode.USER_USERNAME_EXISTS, ex.getCode());
        }

        @Test
        @DisplayName("未指定密码时使用默认密码，并分配角色与岗位")
        void noPassword_usesDefault() {
            UserDTO dto = new UserDTO();
            dto.setUserName("newUser");
            dto.setNickName("Nick");
            dto.setRoleIds(Arrays.asList(2L, 3L));
            dto.setPostIds(List.of(1L));

            when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);
            when(securityUtils.encryptPassword("default123")).thenReturn("$2a$10$encrypted");
            when(sysAccountProperties.requireDefaultPassword()).thenReturn("default123");
            when(sysUserMapper.insert(any(SysUser.class))).thenReturn(1);

            boolean result = sysUserService.addUser(dto);

            assertTrue(result);
            verify(securityUtils).encryptPassword("default123");
            verify(sysUserRelationSupport).insertUserRoles(any(), eq(Arrays.asList(2L, 3L)));
            verify(sysUserRelationSupport).insertUserPosts(any(), eq(List.of(1L)));
        }
    }

    // ==================== updateUser ====================

    @Nested
    @DisplayName("updateUser 更新用户")
    class UpdateUserTests {

        @Test
        @DisplayName("用户不存在时抛出 USER_NOT_FOUND 异常")
        void userNotFound_throwsException() {
            UserDTO dto = new UserDTO();
            dto.setUserId(999L);
            dto.setUserName("anyone");

            when(sysUserMapper.selectById(999L)).thenReturn(null);

            BizException ex = assertThrows(BizException.class, () -> sysUserService.updateUser(dto));
            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getCode());
        }

        @Test
        @DisplayName("用户名冲突时抛出 USER_USERNAME_USED 异常")
        void usernameConflict_throwsException() {
            UserDTO dto = new UserDTO();
            dto.setUserId(10L);
            dto.setUserName("takenUser");

            SysUser target = new SysUser();
            target.setUserId(10L);
            target.setUserName("originalUser");
            target.setDelFlag(AppConstants.DelFlag.NORMAL);

            SysUser conflict = new SysUser();
            conflict.setUserId(20L);
            conflict.setUserName("takenUser");

            when(sysUserMapper.selectById(10L)).thenReturn(target);
            when(sysUserMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(conflict);
            when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

            BizException ex = assertThrows(BizException.class, () -> sysUserService.updateUser(dto));
            assertEquals(ErrorCode.USER_USERNAME_USED, ex.getCode());
        }

        @Test
        @DisplayName("非管理员修改本人资料成功")
        void selfProfileUpdate_succeeds() {
            UserDTO dto = new UserDTO();
            dto.setUserId(1L);
            dto.setUserName("admin");
            dto.setNickName("NewNick");

            SysUser currentUser = new SysUser();
            currentUser.setUserId(1L);
            currentUser.setUserName("admin");
            currentUser.setDelFlag(AppConstants.DelFlag.NORMAL);

            when(sysUserMapper.selectById(1L)).thenReturn(currentUser);
            when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

            boolean result = sysUserService.updateUser(dto);
            assertTrue(result);
        }
    }

    // ==================== changeUserStatus ====================

    @Nested
    @DisplayName("changeUserStatus 变更用户状态")
    class ChangeUserStatusTests {

        @Test
        @DisplayName("用户不存在时抛出 USER_NOT_FOUND 异常")
        void userNotFound_throwsException() {
            when(sysUserMapper.selectById(999L)).thenReturn(null);

            BizException ex = assertThrows(BizException.class,
                    () -> sysUserService.changeUserStatus(999L, "1"));
            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getCode());
        }

        @Test
        @DisplayName("正常变更用户状态为停用")
        void normalChange_succeeds() {
            SysUser user = new SysUser();
            user.setUserId(10L);
            user.setStatus("0");
            user.setDelFlag(AppConstants.DelFlag.NORMAL);

            when(sysUserMapper.selectById(10L)).thenReturn(user);
            when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

            boolean result = sysUserService.changeUserStatus(10L, "1");
            assertTrue(result);
        }
    }

    // ==================== resetUserPassword ====================

    @Nested
    @DisplayName("resetUserPassword 重置密码")
    class ResetPasswordTests {

        @Test
        @DisplayName("用户不存在时抛出 USER_NOT_FOUND 异常")
        void userNotFound_throwsException() {
            when(sysUserMapper.selectById(999L)).thenReturn(null);

            BizException ex = assertThrows(BizException.class,
                    () -> sysUserService.resetUserPassword(999L, "newPass123"));
            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getCode());
        }

        @Test
        @DisplayName("正常重置密码并清除权限缓存")
        void normalReset_succeeds() {
            SysUser user = new SysUser();
            user.setUserId(10L);
            user.setDelFlag(AppConstants.DelFlag.NORMAL);

            when(sysUserMapper.selectById(10L)).thenReturn(user);
            when(securityUtils.encryptPassword("newPass123")).thenReturn("$2a$10$enc");
            when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

            boolean result = sysUserService.resetUserPassword(10L, "newPass123");

            assertTrue(result);
            verify(userPermissionCacheService).clearUserPermissionCacheByUserId(10L);
        }
    }

    // ==================== updateUserPassword ====================

    @Nested
    @DisplayName("updateUserPassword 修改密码")
    class UpdatePasswordTests {

        @Test
        @DisplayName("旧密码错误时抛出 USER_PASSWORD_ERROR 异常")
        void wrongOldPassword_throwsException() {
            SysUser user = new SysUser();
            user.setUserId(10L);
            user.setPassword("$2a$10$oldEncrypted");
            user.setDelFlag(AppConstants.DelFlag.NORMAL);

            when(sysUserMapper.selectById(10L)).thenReturn(user);
            when(securityUtils.matchesPassword("wrongOld", "$2a$10$oldEncrypted")).thenReturn(false);

            BizException ex = assertThrows(BizException.class,
                    () -> sysUserService.updateUserPassword(10L, "wrongOld", "newPass123"));
            assertEquals(ErrorCode.USER_PASSWORD_ERROR, ex.getCode());
        }

        @Test
        @DisplayName("新旧密码相同时抛出 PARAM_INVALID 异常")
        void samePassword_throwsException() {
            SysUser user = new SysUser();
            user.setUserId(10L);
            user.setPassword("$2a$10$same");
            user.setDelFlag(AppConstants.DelFlag.NORMAL);

            when(sysUserMapper.selectById(10L)).thenReturn(user);
            // Both old-vs-stored and new-vs-stored checks use the same arguments; stub once returns true for both
            when(securityUtils.matchesPassword("samePass", "$2a$10$same")).thenReturn(true);

            BizException ex = assertThrows(BizException.class,
                    () -> sysUserService.updateUserPassword(10L, "samePass", "samePass"));
            assertEquals(ErrorCode.PARAM_INVALID, ex.getCode());
        }

        @Test
        @DisplayName("正常修改密码并清除权限缓存")
        void normalUpdate_succeeds() {
            SysUser user = new SysUser();
            user.setUserId(10L);
            user.setPassword("$2a$10$old");
            user.setDelFlag(AppConstants.DelFlag.NORMAL);

            when(sysUserMapper.selectById(10L)).thenReturn(user);
            when(securityUtils.matchesPassword("oldPass", "$2a$10$old")).thenReturn(true);
            when(securityUtils.matchesPassword("newPass", "$2a$10$old")).thenReturn(false);
            when(securityUtils.encryptPassword("newPass")).thenReturn("$2a$10$newEnc");
            when(sysUserMapper.updateById(any(SysUser.class))).thenReturn(1);

            boolean result = sysUserService.updateUserPassword(10L, "oldPass", "newPass");

            assertTrue(result);
            verify(userPermissionCacheService).clearUserPermissionCacheByUserId(10L);
        }
    }

    // ==================== deleteUserByIds ====================

    @Nested
    @DisplayName("deleteUserByIds 删除用户")
    class DeleteUserTests {

        @Test
        @DisplayName("空列表返回 false")
        void emptyList_returnsFalse() {
            assertFalse(sysUserService.deleteUserByIds(Collections.emptyList()));
        }

        @Test
        @DisplayName("null 列表返回 false")
        void nullList_returnsFalse() {
            assertFalse(sysUserService.deleteUserByIds(null));
        }
    }
}
