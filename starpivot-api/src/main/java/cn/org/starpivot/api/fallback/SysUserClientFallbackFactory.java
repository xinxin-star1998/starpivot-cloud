package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.system.SysUserClient;
import cn.org.starpivot.api.system.dto.*;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.List;

public class SysUserClientFallbackFactory implements FallbackFactory<SysUserClient> {

    private static final String ACTION = "用户服务";

    @Override
    public SysUserClient create(Throwable cause) {
        return new SysUserClient() {
            @Override
            public Result<SysUserAuthDto> getByUsername(String username) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<SysUserAuthDto> verifyPassword(VerifyPasswordRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<List<SysMenuDto>> getUserMenus(Long userId) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<RegisterUserResponse> registerUser(RegisterUserRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Boolean> resetPasswordByForgot(ForgotPasswordResetRequest request) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
