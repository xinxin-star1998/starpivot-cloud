package cn.org.starpivot.mq.core;

import cn.org.starpivot.mq.constant.MqHeaderNames;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Message 与 {@link MessageEnvelope} 互转。
 */
@Component
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class MqMessageConverter {

    private final ObjectMapper objectMapper;

    public <T> MessageEnvelope<T> fromMessage(Message message, Class<T> payloadType) {
        try {
            JavaType javaType = objectMapper.getTypeFactory()
                    .constructParametricType(MessageEnvelope.class, payloadType);
            return objectMapper.readValue(message.getBody(), javaType);
        } catch (Exception ex) {
            throw new IllegalStateException("MQ 消息反序列化失败", ex);
        }
    }

    public String resolveMessageId(Message message, MessageEnvelope<?> envelope) {
        if (envelope != null && envelope.getMessageId() != null) {
            return envelope.getMessageId();
        }
        Object headerId = message.getMessageProperties().getHeader(MqHeaderNames.MESSAGE_ID);
        if (headerId != null) {
            return headerId.toString();
        }
        return message.getMessageProperties().getMessageId();
    }
}
