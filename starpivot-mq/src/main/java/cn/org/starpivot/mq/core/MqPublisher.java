package cn.org.starpivot.mq.core;

import cn.org.starpivot.api.event.MqExchangeNames;
import cn.org.starpivot.mq.config.StarPivotMqProperties;
import cn.org.starpivot.mq.constant.MqHeaderNames;
import cn.org.starpivot.common.observability.TraceIdConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 统一消息发送入口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MqPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final StarPivotMqProperties mqProperties;
    private final Environment environment;

    /**
     * 向 Topic Exchange 发布消息。
     *
     * @param routingKey 路由键
     * @param eventType  事件类型（与 routingKey 通常一致）
     * @param payload    业务载荷
     * @return 发送结果，含 messageId
     */
    public PublishResult publish(String routingKey, String eventType, Object payload) {
        String messageId = UUID.randomUUID().toString();
        String traceId = MDC.get(TraceIdConstants.MDC_TRACE_ID_KEY);
        String producer = resolveProducer();

        MessageEnvelope<Object> envelope = MessageEnvelope.builder()
                .messageId(messageId)
                .traceId(traceId)
                .eventType(eventType)
                .occurredAt(LocalDateTime.now())
                .producer(producer)
                .payload(payload)
                .build();

        try {
            byte[] body = objectMapper.writeValueAsBytes(envelope);
            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setMessageId(messageId);
            properties.setHeader(MqHeaderNames.MESSAGE_ID, messageId);
            properties.setHeader(MqHeaderNames.EVENT_TYPE, eventType);
            if (StringUtils.hasText(traceId)) {
                properties.setHeader(TraceIdConstants.TRACE_ID_HEADER, traceId);
            }
            Message message = new Message(body, properties);

            rabbitTemplate.send(MqExchangeNames.TOPIC, routingKey, message);
            log.debug("MQ 消息已发送: routingKey={}, messageId={}", routingKey, messageId);
            return new PublishResult(messageId, routingKey, eventType);
        } catch (Exception ex) {
            log.error("MQ 消息发送失败: routingKey={}, messageId={}", routingKey, messageId, ex);
            throw new IllegalStateException("MQ 消息发送失败: " + routingKey, ex);
        }
    }

    private String resolveProducer() {
        if (StringUtils.hasText(mqProperties.getProducer())) {
            return mqProperties.getProducer();
        }
        return environment.getProperty("spring.application.name", "unknown");
    }
}
