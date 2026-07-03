package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.system.SysOperLogClient;
import org.springframework.cloud.openfeign.FallbackFactory;

public class SysOperLogClientFallbackFactory implements FallbackFactory<SysOperLogClient> {

    private static final String ACTION = "操作日志";

    @Override
    public SysOperLogClient create(Throwable cause) {
        return () -> FeignFallbackSupport.unavailable(cause, ACTION);
    }
}
