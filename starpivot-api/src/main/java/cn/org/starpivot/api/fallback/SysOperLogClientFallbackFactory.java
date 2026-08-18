package cn.org.starpivot.api.fallback;

import cn.org.starpivot.api.system.SysOperLogClient;
import cn.org.starpivot.common.domain.Result;
import org.springframework.cloud.openfeign.FallbackFactory;

public class SysOperLogClientFallbackFactory implements FallbackFactory<SysOperLogClient> {

    private static final String ACTION = "操作日志";

    @Override
    public SysOperLogClient create(Throwable cause) {
        return new SysOperLogClient() {
            @Override
            public Result<Void> cleanAll() {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }

            @Override
            public Result<Void> cleanBeforeDays(int days) {
                return FeignFallbackSupport.unavailable(cause, ACTION);
            }
        };
    }
}
