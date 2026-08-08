package cn.org.starpivot.api.fallback;

import cn.org.starpivot.common.domain.Result;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * Feign 降级统一响应工具类。
 * <p>
 * 根据异常类型区分降级原因（熔断器开启、超时、连接失败、业务异常），
 * 返回差异化错误码和提示信息，便于调用方进行针对性处理。
 * <p>
 * 所有方法均为静态工具方法，不可实例化。
 */
@Slf4j
public final class FeignFallbackSupport {

    /** 下游服务不可用（通用） */
    public static final int CODE_UNAVAILABLE = 503;
    /** 下游服务熔断器已开启（流量保护中） */
    public static final int CODE_CIRCUIT_BREAKER_OPEN = 5031;
    /** 下游服务调用超时 */
    public static final int CODE_TIMEOUT = 504;
    /** 下游服务连接失败 */
    public static final int CODE_CONNECT_FAILED = 502;

    private FeignFallbackSupport() {
    }

    /**
     * 根据异常类型自动判断降级原因并返回差异化 Result。
     *
     * @param cause  触发降级的异常
     * @param action 服务/操作描述（如"用户服务"、"订单服务"）
     * @return 包含差异化错误码和提示的 Result
     */
    public static <T> Result<T> unavailable(Throwable cause, String action) {
        Throwable root = unwrap(cause);

        if (root instanceof CallNotPermittedException) {
            log.warn("[CircuitBreaker] {}熔断器已开启，请求被拒绝: {}", action, root.getMessage());
            return Result.error(CODE_CIRCUIT_BREAKER_OPEN,
                    action + "正在保护性熔断中，请稍后重试");
        }

        if (root instanceof TimeoutException || root instanceof SocketTimeoutException) {
            log.warn("[Timeout] {}调用超时: {}", action, root.getMessage());
            return Result.error(CODE_TIMEOUT,
                    action + "响应超时，请稍后重试");
        }

        if (root instanceof ConnectException) {
            log.warn("[ConnectFailed] {}连接失败: {}", action, root.getMessage());
            return Result.error(CODE_CONNECT_FAILED,
                    action + "连接失败，服务可能未启动");
        }

        if (root instanceof FeignException fe) {
            int httpStatus = fe.status();
            log.warn("[FeignError] {}HTTP异常 status={}, message={}", action, httpStatus, root.getMessage());
            if (httpStatus < 0) {
                return Result.error(CODE_TIMEOUT, action + "响应超时或连接异常，请稍后重试");
            }
            return Result.error(CODE_UNAVAILABLE,
                    action + "服务异常(HTTP " + httpStatus + ")，请稍后重试");
        }

        log.warn("[Fallback] {}服务降级: {}", action, root.getMessage(), root);
        return Result.error(CODE_UNAVAILABLE, action + "服务暂不可用，请稍后重试");
    }

    /**
     * 返回空数据的成功 Result（用于非关键路径的静默降级）。
     * <p>
     * 适用于：统计数量、推荐列表等"缺失不影响主流程"的场景。
     * <b>禁止</b>用于扣库存、下单、支付确认等关键写路径。
     *
     * @param cause  触发降级的异常
     * @param action 服务/操作描述
     * @return data 为 null 的成功 Result
     */
    public static <T> Result<T> silentFallback(Throwable cause, String action) {
        log.info("[SilentFallback] {}降级为默认值: {}", action, cause == null ? "unknown" : cause.getMessage());
        return Result.success(null);
    }

    /**
     * 返回带默认数据的成功 Result（用于有合理默认值的非关键路径）。
     *
     * @param cause       触发降级的异常
     * @param action      服务/操作描述
     * @param defaultData 降级默认数据
     * @return 携带默认数据的成功 Result
     */
    public static <T> Result<T> silentFallback(Throwable cause, String action, T defaultData) {
        log.info("[SilentFallback] {}降级为默认值: {}", action, cause == null ? "unknown" : cause.getMessage());
        return Result.success(defaultData);
    }

    /**
     * 解包异常链，找到最有意义的根因异常。
     * <p>
     * 优先返回 {@link CallNotPermittedException}、{@link TimeoutException}、
     * {@link SocketTimeoutException}、{@link ConnectException}、{@link FeignException}；
     * 否则返回最深层 cause。
     */
    private static Throwable unwrap(Throwable cause) {
        if (cause == null) {
            return new RuntimeException("unknown");
        }
        Throwable current = cause;
        int depth = 0;
        while (current.getCause() != null && current.getCause() != current && depth < 10) {
            if (current instanceof CallNotPermittedException
                    || current instanceof TimeoutException
                    || current instanceof SocketTimeoutException
                    || current instanceof ConnectException
                    || current instanceof FeignException) {
                return current;
            }
            current = current.getCause();
            depth++;
        }
        return current;
    }
}
