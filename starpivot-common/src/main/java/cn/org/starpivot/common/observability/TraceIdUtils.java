package cn.org.starpivot.common.observability;

import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * TraceId 生成与规范化工具类。
 * <p>
 * 供 {@link TraceIdServletFilter} 及网关/非 Tracing 场景复用：
 * 优先沿用上游传入的 traceId，缺失时生成 32 位无连字符 UUID。
 * <p>
 * 不可实例化。
 *
 * @see TraceIdConstants#TRACE_ID_HEADER
 * @see TraceIdServletFilter
 */
public final class TraceIdUtils {

    /** 禁止实例化 */
    private TraceIdUtils() {
    }

    /**
     * 解析或创建 traceId。
     * <p>
     * 若 {@code incoming} 非空白则 {@code trim()} 后返回；否则生成新的 UUID（去除 {@code -}）。
     *
     * @param incoming 上游请求头或上下文中的 traceId，可为 {@code null}
     * @return 规范化后的 traceId，永不为 {@code null}
     */
    public static String resolveOrCreate(String incoming) {
        if (StringUtils.hasText(incoming)) {
            return incoming.trim();
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}
