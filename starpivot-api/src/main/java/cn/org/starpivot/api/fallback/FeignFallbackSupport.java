package cn.org.starpivot.api.fallback;

import cn.org.starpivot.common.domain.Result;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign 降级统一响应。
 */
@Slf4j
public final class FeignFallbackSupport {

    /** 下游服务不可用 */
    public static final int CODE_UNAVAILABLE = 503;

    private FeignFallbackSupport() {
    }

    public static <T> Result<T> unavailable(Throwable cause, String action) {
        log.warn("Feign fallback: {}", action, cause);
        return Result.error(CODE_UNAVAILABLE, action + "服务暂不可用，请稍后重试");
    }
}
