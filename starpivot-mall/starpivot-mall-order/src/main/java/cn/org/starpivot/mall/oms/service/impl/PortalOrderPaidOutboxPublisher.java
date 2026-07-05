package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.api.event.MqExchangeNames;
import cn.org.starpivot.api.event.MqRoutingKeys;
import cn.org.starpivot.api.mall.order.dto.OrderPaidMessage;
import cn.org.starpivot.mall.oms.entity.MqMessage;
import cn.org.starpivot.mall.oms.service.OmsLocalMessageOutboxService;
import cn.org.starpivot.mall.oms.support.MqMessageStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
public class PortalOrderPaidOutboxPublisher {

    private final OmsLocalMessageOutboxService localMessageOutboxService;
    private final ObjectMapper objectMapper;

    /**
     * 在支付成功事务内登记 Outbox，提交后立即尝试投递。
     */
    public void enqueueOrderPaid(OrderPaidMessage message) {
        if (message == null || message.getOrderId() == null) {
            throw new IllegalArgumentException("OrderPaidMessage 无效");
        }
        String messageId = UUID.randomUUID().toString().replace("-", "");
        String content;
        try {
            content = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("订单支付消息序列化失败", ex);
        }
        localMessageOutboxService.enqueue(
                messageId,
                MqExchangeNames.TOPIC,
                MqRoutingKeys.MALL_ORDER_PAID,
                OrderPaidMessage.class.getSimpleName(),
                content);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        localMessageOutboxService.flushMessage(messageId);
                    } catch (Exception ex) {
                        log.warn("订单支付消息提交后投递失败 messageId={}, orderId={}: {}",
                                messageId, message.getOrderId(), ex.getMessage());
                    }
                }
            });
        } else {
            localMessageOutboxService.flushMessage(messageId);
        }
    }
}
