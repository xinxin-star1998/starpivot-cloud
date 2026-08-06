package cn.org.starpivot.auth.service;

import cn.org.starpivot.api.system.SysConfigClient;
import cn.org.starpivot.api.system.SysUserClient;
import cn.org.starpivot.api.system.dto.RegisterUserResponse;
import cn.org.starpivot.auth.domain.ForgotPasswordRequest;
import cn.org.starpivot.auth.domain.RegisterRequest;
import cn.org.starpivot.auth.domain.RegisterResponse;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.JwtProperties;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private SysUserClient sysUserClient;
    @Mock
    private SysConfigClient sysConfigClient;
    @Mock
    private LoginLogRecordService loginLogRecordService;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private AuthService authService;

    @Nested
    class GetUserInfo {

        @Test
        void throwsWhenTokenBlank() {
            BizException ex = assertThrows(BizException.class, () -> authService.getUserInfo(" "));
            assertEquals(401, ex.getCode());
            assertEquals("未授权，请先登录", ex.getMessage());
        }

        @Test
        void throwsWhenTokenNull() {
            BizException ex = assertThrows(BizException.class, () -> authService.getUserInfo(null));
            assertEquals(ErrorCode.UNAUTHORIZED, ex.getCode());
        }

        @Test
        void throwsWhenTokenBlacklisted() {
            String token = "blacklisted-token";
            when(tokenBlacklistService.isBlacklisted(token)).thenReturn(true);

            BizException ex = assertThrows(BizException.class, () -> authService.getUserInfo(token));
            assertEquals(401, ex.getCode());
            assertEquals("令牌已失效，请重新登录", ex.getMessage());
        }
    }

    @Nested
    class IsRegisterEnabled {

        @Test
        void returnsTrueWhenConfigEnabled() {
            when(sysConfigClient.isRegisterEnabled()).thenReturn(Result.success(true));
            assertTrue(authService.isRegisterEnabled());
        }

        @Test
        void returnsFalseWhenConfigDisabled() {
            when(sysConfigClient.isRegisterEnabled()).thenReturn(Result.success(false));
            assertFalse(authService.isRegisterEnabled());
        }

        @Test
        void returnsFalseWhenResultNull() {
            when(sysConfigClient.isRegisterEnabled()).thenReturn(null);
            assertFalse(authService.isRegisterEnabled());
        }

        @Test
        void returnsFalseWhenDataNull() {
            Result<Boolean> result = new Result<>(200, "ok", null);
            when(sysConfigClient.isRegisterEnabled()).thenReturn(result);
            assertFalse(authService.isRegisterEnabled());
        }
    }

    @Nested
    class Register {

        @Test
        void throwsWhenRegisterNotEnabled() {
            when(sysConfigClient.isRegisterEnabled()).thenReturn(Result.success(false));

            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setPassword("pass123");

            BizException ex = assertThrows(BizException.class, () -> authService.register(request));
            assertEquals(403, ex.getCode());
        }

        @Test
        void throwsWhenRemoteRegisterFails() {
            when(sysConfigClient.isRegisterEnabled()).thenReturn(Result.success(true));
            when(sysUserClient.registerUser(any())).thenReturn(Result.error("用户名已存在"));

            RegisterRequest request = new RegisterRequest();
            request.setUsername("newuser");
            request.setPassword("pass123");

            BizException ex = assertThrows(BizException.class, () -> authService.register(request));
            assertEquals(500, ex.getCode());
        }

        @Test
        void successReturnsUserIdAndUsername() {
            when(sysConfigClient.isRegisterEnabled()).thenReturn(Result.success(true));
            RegisterUserResponse resp = new RegisterUserResponse();
            resp.setUserId(42L);
            resp.setUsername("newuser");
            resp.setNickName("新用户");
            when(sysUserClient.registerUser(any())).thenReturn(Result.success(resp));

            RegisterRequest request = new RegisterRequest();
            request.setUsername(" newuser ");
            request.setPassword("pass123");

            RegisterResponse response = authService.register(request);
            assertEquals(42L, response.getUserId());
            assertEquals("newuser", response.getUsername());
            assertEquals("新用户", response.getNickName());
        }
    }

    @Nested
    class Logout {

        @Test
        void skipsWhenTokenNull() {
            authService.logout(null);
            verifyNoInteractions(tokenBlacklistService);
        }

        @Test
        void skipsWhenTokenBlank() {
            authService.logout("  ");
            verifyNoInteractions(tokenBlacklistService);
        }

        @Test
        void addsTokenToBlacklist() {
            when(jwtProperties.getExpire()).thenReturn(7200000L);
            // parseToken will throw since it's not a real JWT, which is caught internally
            authService.logout("some-token");
            verify(tokenBlacklistService).add("some-token", 7200000L);
        }
    }

    @Nested
    class ForgotPassword {

        @Test
        void throwsWhenFeatureNotEnabled() {
            when(sysConfigClient.isForgetPasswordEnabled()).thenReturn(Result.success(false));

            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setUsername("user");
            request.setPassword("pass123");
            request.setCaptchaToken("tok");
            request.setCaptcha("1234");

            BizException ex = assertThrows(BizException.class, () -> authService.forgotPassword(request));
            assertEquals(403, ex.getCode());
        }

        @Test
        void throwsWhenRemoteResetFails() {
            when(sysConfigClient.isForgetPasswordEnabled()).thenReturn(Result.success(true));
            doNothing().when(captchaService).check(any(), any(), any());
            when(sysUserClient.resetPasswordByForgot(any())).thenReturn(Result.error("用户不存在"));

            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setUsername("user");
            request.setPassword("pass123");
            request.setCaptchaToken("tok");
            request.setCaptcha("1234");

            BizException ex = assertThrows(BizException.class, () -> authService.forgotPassword(request));
            assertEquals(500, ex.getCode());
        }
    }
}
