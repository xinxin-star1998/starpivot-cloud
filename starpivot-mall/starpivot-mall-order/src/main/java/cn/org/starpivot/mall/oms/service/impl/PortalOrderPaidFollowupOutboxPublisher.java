package cn.org.starpivot.mall.oms.service.impl;



import cn.org.starpivot.api.event.MqExchangeNames;

import cn.org.starpivot.api.event.MqRoutingKeys;

import cn.org.starpivot.api.mall.order.dto.OrderPaidFollowupMessage;

import cn.org.starpivot.mall.oms.service.OmsLocalMessageOutboxService;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import org.springframework.stereotype.Service;

import org.springframework.transaction.support.TransactionSynchronization;

import org.springframework.transaction.support.TransactionSynchronizationManager;



import java.util.ArrayList;

import java.util.List;

import java.util.UUID;



@Slf4j

@Service

@RequiredArgsConstructor

@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")

public class PortalOrderPaidFollowupOutboxPublisher {



    private final OmsLocalMessageOutboxService localMessageOutboxService;

    private final ObjectMapper objectMapper;



    /**

     * 在支付成功事务内登记优惠券确认与会员奖励两条 Outbox，提交后立即尝试投递。

     */

    public void enqueuePaidFollowup(Long orderId, String orderSn) {

        if (orderId == null) {

            throw new IllegalArgumentException("orderId 无效");

        }

        OrderPaidFollowupMessage message = new OrderPaidFollowupMessage();

        message.setOrderId(orderId);

        message.setOrderSn(orderSn);



        List<String> messageIds = new ArrayList<>(2);

        messageIds.add(enqueue(message, MqRoutingKeys.MALL_ORDER_PAID_COUPON));

        messageIds.add(enqueue(message, MqRoutingKeys.MALL_ORDER_PAID_REWARD));



        if (TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {

                @Override

                public void afterCommit() {

                    flushAfterCommit(messageIds, orderId);

                }

            });

        } else {

            flushAfterCommit(messageIds, orderId);

        }

    }



    private String enqueue(OrderPaidFollowupMessage message, String routingKey) {

        String messageId = UUID.randomUUID().toString().replace("-", "");

        String content;

        try {

            content = objectMapper.writeValueAsString(message);

        } catch (JsonProcessingException ex) {

            throw new IllegalStateException("订单支付跟进消息序列化失败", ex);

        }

        localMessageOutboxService.enqueue(

                messageId,

                MqExchangeNames.TOPIC,

                routingKey,

                OrderPaidFollowupMessage.class.getSimpleName(),

                content);

        return messageId;

    }



    private void flushAfterCommit(List<String> messageIds, Long orderId) {

        for (String messageId : messageIds) {

            try {

                localMessageOutboxService.flushMessage(messageId);

            } catch (Exception ex) {

                log.warn("订单支付跟进消息提交后投递失败 messageId={}, orderId={}: {}",

                        messageId, orderId, ex.getMessage());

            }

        }

    }

}

