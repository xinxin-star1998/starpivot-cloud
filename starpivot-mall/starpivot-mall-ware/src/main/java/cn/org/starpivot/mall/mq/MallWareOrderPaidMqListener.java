package cn.org.starpivot.mall.mq;

import cn.org.starpivot.api.event.MqQueueNames;
import cn.org.starpivot.api.mall.order.dto.OrderPaidMessage;
import cn.org.starpivot.mall.wms.service.WmsOrderPaidFulfillmentService;
import cn.org.starpivot.mq.config.RabbitListenerConfiguration;
import cn.org.starpivot.mq.core.MessageEnvelope;
import cn.org.starpivot.mq.core.MqMessageConverter;
import cn.org.starpivot.mq.listener.AbstractMqListener;
import cn.org.starpivot.mq.listener.IdempotentChecker;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 消费订单支付成功事件，异步扣减 WMS 库存。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
public class MallWareOrderPaidMqListener extends AbstractMqListener<OrderPaidMessage> {

    private final WmsOrderPaidFulfillmentService orderPaidFulfillmentService;

    public MallWareOrderPaidMqListener(MqMessageConverter messageConverter,
                                       ObjectProvider<IdempotentChecker> idempotentCheckerProvider,
                                       WmsOrderPaidFulfillmentService orderPaidFulfillmentService) {
        super(messageConverter, idempotentCheckerProvider);
        this.orderPaidFulfillmentService = orderPaidFulfillmentService;
    }

    @RabbitListener(
            queues = MqQueueNames.MALL_ORDER_PAID_WARE,
            ackMode = "MANUAL",
            containerFactory = RabbitListenerConfiguration.LISTENER_CONTAINER_FACTORY)
    public void onMessage(Message message, Channel channel) throws IOException {
        handleMessage(message, channel, OrderPaidMessage.class, orderPaidFulfillmentService::fulfill);
    }

    @Override
    protected String resolveIdempotentKey(Message message, MessageEnvelope<OrderPaidMessage> envelope) {
        OrderPaidMessage payload = envelope.getPayload();
        if (payload != null && payload.getOrderId() != null) {
            return "mall:order:paid:" + payload.getOrderId();
        }
        return super.resolveIdempotentKey(message, envelope);
    }
}
