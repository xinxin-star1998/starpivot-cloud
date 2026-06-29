package cn.org.starpivot.auth.service;

import cn.org.starpivot.api.system.SysConfigClient;
import cn.org.starpivot.api.system.SysUserClient;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.JwtProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    void getUserInfo_throwsWhenTokenBlank() {
        BizException ex = assertThrows(BizException.class, () -> authService.getUserInfo(" "));
        assertEquals(401, ex.getCode());
        assertEquals("未授权，请先登录", ex.getMessage());
    }

    @Test
    void getUserInfo_throwsWhenTokenNull() {
        BizException ex = assertThrows(BizException.class, () -> authService.getUserInfo(null));
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getCode());
    }
}
