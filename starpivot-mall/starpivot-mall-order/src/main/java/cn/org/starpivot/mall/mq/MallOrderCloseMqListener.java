package cn.org.starpivot.mall.mq;

import cn.org.starpivot.api.event.MqQueueNames;
import cn.org.starpivot.api.mall.order.dto.OrderCloseMessage;
import cn.org.starpivot.mall.portal.service.PortalOrderCloseService;
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
 * 消费待付款订单超时关单延迟消息。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
public class MallOrderCloseMqListener extends AbstractMqListener<OrderCloseMessage> {

    private final PortalOrderCloseService portalOrderCloseService;

    public MallOrderCloseMqListener(MqMessageConverter messageConverter,
                                    ObjectProvider<IdempotentChecker> idempotentCheckerProvider,
                                    PortalOrderCloseService portalOrderCloseService) {
        super(messageConverter, idempotentCheckerProvider);
        this.portalOrderCloseService = portalOrderCloseService;
    }

    @RabbitListener(
            queues = MqQueueNames.MALL_ORDER_CLOSE,
            ackMode = "MANUAL",
            containerFactory = RabbitListenerConfiguration.LISTENER_CONTAINER_FACTORY)
    public void onMessage(Message message, Channel channel) throws IOException {
        handleMessage(message, channel, OrderCloseMessage.class, this::dispatch);
    }

    @Override
    protected String resolveIdempotentKey(Message message, MessageEnvelope<OrderCloseMessage> envelope) {
        OrderCloseMessage payload = envelope.getPayload();
        if (payload != null && payload.getOrderId() != null) {
            return "mall:order:close:" + payload.getOrderId();
        }
        return super.resolveIdempotentKey(message, envelope);
    }

    private void dispatch(OrderCloseMessage payload) {
        if (payload == null || payload.getOrderSn() == null) {
            return;
        }
        portalOrderCloseService.closeUnpaidOrderAndReleaseStock(payload.getOrderSn());
    }
}
