package cn.org.starpivot.common.observability;

/**
 * 全链路 TraceId 相关常量工具类。
 * <p>
 * 定义 HTTP 头与 SLF4J MDC 键名，供 {@link TraceIdServletFilter}、
 * {@link TraceIdUtils} 及日志配置统一引用，避免魔法字符串分散。
 * <p>
 * 不可实例化。
 *
 * @see TraceIdServletFilter
 * @see TraceIdUtils
 */
public final class TraceIdConstants {

    /** HTTP 请求/响应头名称，用于跨服务传递 traceId */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** SLF4J {@link org.slf4j.MDC} 中存放 traceId 的键名，供日志 pattern 使用 */
    public static final String MDC_TRACE_ID_KEY = "traceId";

    /** 禁止实例化 */
    private TraceIdConstants() {
    }
}
