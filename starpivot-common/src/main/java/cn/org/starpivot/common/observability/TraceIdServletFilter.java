package cn.org.starpivot.common.observability;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Servlet 全链路 TraceId 过滤器。
 * <p>
 * 继承 {@link OncePerRequestFilter}，保证每个请求仅执行一次。解析顺序：
 * <ol>
 *   <li>Micrometer Tracing 当前 Span 的 traceId（若已启用）</li>
 *   <li>请求头 {@link TraceIdConstants#TRACE_ID_HEADER}</li>
 *   <li>由 {@link TraceIdUtils} 生成新 UUID</li>
 * </ol>
 * 解析结果写入 {@link org.slf4j.MDC} 与响应头，请求结束后清理 MDC。
 * <p>
 * 由 {@link cn.org.starpivot.common.config.ObservabilityAutoConfiguration} 注册，
 * 顺序靠后以便 Tracing 先创建 Span。
 */
public class TraceIdServletFilter extends OncePerRequestFilter {

    private final ObjectProvider<Tracer> tracerProvider;

    /**
     * @param tracerProvider Micrometer {@link Tracer} 可选提供者；无依赖时为 empty
     */
    public TraceIdServletFilter(ObjectProvider<Tracer> tracerProvider) {
        this.tracerProvider = tracerProvider;
    }

    /**
     * 解析 traceId、写入 MDC 与响应头，并继续过滤器链。
     *
     * @param request     当前 HTTP 请求
     * @param response    当前 HTTP 响应
     * @param filterChain 后续过滤器链
     * @throws ServletException Servlet 处理异常
     * @throws IOException      I/O 异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String traceId = resolveTraceId(request);
        MDC.put(TraceIdConstants.MDC_TRACE_ID_KEY, traceId);
        response.setHeader(TraceIdConstants.TRACE_ID_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TraceIdConstants.MDC_TRACE_ID_KEY);
        }
    }

    /**
     * 按优先级解析本次请求的 traceId。
     * <p>
     * 优先使用 Micrometer 当前 Span；否则回退到请求头或 {@link TraceIdUtils#resolveOrCreate(String)}。
     *
     * @param request HTTP 请求（读取 {@link TraceIdConstants#TRACE_ID_HEADER}）
     * @return 非空的 traceId 字符串
     */
    private String resolveTraceId(HttpServletRequest request) {
        Tracer tracer = tracerProvider.getIfAvailable();
        if (tracer != null) {
            Span current = tracer.currentSpan();
            if (current != null) {
                return current.context().traceId();
            }
        }
        return TraceIdUtils.resolveOrCreate(request.getHeader(TraceIdConstants.TRACE_ID_HEADER));
    }
}
