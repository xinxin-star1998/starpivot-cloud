package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.system.SysLoginLogClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class SysLoginLogClientFallbackFactory implements FallbackFactory<SysLoginLogClient> {

    private static final String ACTION = "登录日志";

    @Override
    public SysLoginLogClient create(Throwable cause) {
        return loginLogDto -> FeignFallbackSupport.unavailable(cause, ACTION);
    }
}
