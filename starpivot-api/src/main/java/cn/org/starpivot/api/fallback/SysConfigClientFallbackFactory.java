package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.system.SysConfigClient;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

public class SysConfigClientFallbackFactory implements FallbackFactory<SysConfigClient> {

    private static final String ACTION = "系统配置";

    @Override
    public SysConfigClient create(Throwable cause) {
        return new SysConfigClient() {
            @Override
            public Result<Boolean> isRegisterEnabled() {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Boolean> isForgetPasswordEnabled() {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
