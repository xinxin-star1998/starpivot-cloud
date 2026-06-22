package cn.org.starpivot.mq.support;

import cn.org.starpivot.common.observability.TraceIdConstants;
import org.slf4j.MDC;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.util.StringUtils;

/**
 * 发送前将 MDC 中的 traceId 写入消息 Header。
 */
public class TraceIdMessagePostProcessor implements MessagePostProcessor {

    @Override
    public Message postProcessMessage(Message message) throws AmqpException {
        String traceId = MDC.get(TraceIdConstants.MDC_TRACE_ID_KEY);
        if (StringUtils.hasText(traceId)) {
            message.getMessageProperties().setHeader(TraceIdConstants.TRACE_ID_HEADER, traceId);
        }
        return message;
    }
}
