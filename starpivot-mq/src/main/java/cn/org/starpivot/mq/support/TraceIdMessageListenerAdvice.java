package cn.org.starpivot.mq.support;

import cn.org.starpivot.common.observability.TraceIdConstants;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.util.StringUtils;

/**
 * 消费时从消息 Header 恢复 MDC traceId。
 */
public final class TraceIdMessageListenerAdvice {

    private TraceIdMessageListenerAdvice() {
    }

    public static void applyTraceId(Message message) {
        Object traceId = message.getMessageProperties().getHeader(TraceIdConstants.TRACE_ID_HEADER);
        if (traceId != null && StringUtils.hasText(traceId.toString())) {
            MDC.put(TraceIdConstants.MDC_TRACE_ID_KEY, traceId.toString());
        }
    }

    public static void clearTraceId() {
        MDC.remove(TraceIdConstants.MDC_TRACE_ID_KEY);
    }
}
