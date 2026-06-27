package cn.org.starpivot.mq.listener;

import cn.org.starpivot.mq.core.MessageEnvelope;
import cn.org.starpivot.mq.core.MqMessageConverter;
import cn.org.starpivot.mq.support.TraceIdMessageListenerAdvice;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.ObjectProvider;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * MQ 消费模板：TraceId 恢复、幂等、手动 ACK。
 */
@Slf4j
@RequiredArgsConstructor
public abstract class AbstractMqListener<T> {

    private final MqMessageConverter messageConverter;
    private final ObjectProvider<IdempotentChecker> idempotentCheckerProvider;

    protected void handleMessage(Message message, Channel channel, Class<T> payloadType,
                               Consumer<T> businessHandler) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        TraceIdMessageListenerAdvice.applyTraceId(message);
        try {
            MessageEnvelope<T> envelope = messageConverter.fromMessage(message, payloadType);
            String messageId = resolveIdempotentKey(message, envelope);

            IdempotentChecker idempotentChecker = idempotentCheckerProvider.getIfAvailable();
            if (idempotentChecker != null && !idempotentChecker.tryAcquire(messageId)) {
                log.info("跳过重复 MQ 消息: messageId={}", messageId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            businessHandler.accept(envelope.getPayload());
            channel.basicAck(deliveryTag, false);
        } catch (Exception ex) {
            log.error("MQ 消息消费失败", ex);
            channel.basicNack(deliveryTag, false, false);
        } finally {
            TraceIdMessageListenerAdvice.clearTraceId();
        }
    }

    /**
     * 解析幂等键，子类可覆盖以使用业务语义键（如 approval:finished:{instanceId}:{result}）。
     */
    protected String resolveIdempotentKey(Message message, MessageEnvelope<T> envelope) {
        return messageConverter.resolveMessageId(message, envelope);
    }
}
