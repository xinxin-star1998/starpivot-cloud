package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.api.event.MqQueueNames;
import cn.org.starpivot.api.mall.order.dto.OrderCloseMessage;
import cn.org.starpivot.mall.oms.service.OmsOrderSettingService;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mq.core.MessageEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 下单后向延迟队列投递关单消息（per-message TTL，到期经 DLX 进入消费队列）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
public class PortalOrderCloseDelayPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final OmsOrderSettingService omsOrderSettingService;
    private final Environment environment;

    public void scheduleClose(String orderSn, Long orderId) {
        if (orderId == null || orderSn == null || orderSn.isBlank()) {
            return;
        }
        long delayMs = resolveUnpaidLockMillis();
        String messageId = UUID.randomUUID().toString().replace("-", "");
        OrderCloseMessage payload = new OrderCloseMessage();
        payload.setOrderId(orderId);
        payload.setOrderSn(orderSn);
        try {
            MessageEnvelope<OrderCloseMessage> envelope = MessageEnvelope.<OrderCloseMessage>builder()
                    .messageId(messageId)
                    .eventType("mall.order.close.delay")
                    .occurredAt(LocalDateTime.now())
                    .producer(resolveProducer())
                    .payload(payload)
                    .build();
            byte[] body = objectMapper.writeValueAsBytes(envelope);
            MessageProperties properties = new MessageProperties();
            properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            properties.setMessageId(messageId);
            properties.setExpiration(String.valueOf(delayMs));
            Message message = new Message(body, properties);
            rabbitTemplate.send("", MqQueueNames.MALL_ORDER_CLOSE_DELAY, message);
            log.debug("Scheduled order close delay orderSn={}, delayMs={}", orderSn, delayMs);
        } catch (Exception ex) {
            throw new IllegalStateException("订单关单延迟消息发送失败: " + orderSn, ex);
        }
    }

    private long resolveUnpaidLockMillis() {
        try {
            Integer minutes = omsOrderSettingService.getSetting().getNormalOrderOvertime();
            if (minutes != null && minutes > 0) {
                return minutes * 60_000L;
            }
        } catch (Exception ex) {
            log.debug("Use default unpaid lock ttl for order close delay", ex);
        }
        return PortalConstants.DEFAULT_UNPAID_LOCK_MINUTES * 60_000L;
    }

    private String resolveProducer() {
        return environment.getProperty("spring.application.name", "starpivot-mall-order");
    }
}
