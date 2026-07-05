package cn.org.starpivot.mall.mq;

import cn.org.starpivot.api.event.MqQueueNames;
import cn.org.starpivot.api.mall.order.dto.OrderPaidFollowupMessage;
import cn.org.starpivot.mall.portal.service.PortalCouponService;
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
 * 消费订单支付成功事件，异步确认优惠券核销。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
public class MallPromotionOrderPaidMqListener extends AbstractMqListener<OrderPaidFollowupMessage> {

    private final PortalCouponService portalCouponService;

    public MallPromotionOrderPaidMqListener(MqMessageConverter messageConverter,
                                            ObjectProvider<IdempotentChecker> idempotentCheckerProvider,
                                            PortalCouponService portalCouponService) {
        super(messageConverter, idempotentCheckerProvider);
        this.portalCouponService = portalCouponService;
    }

    @RabbitListener(
            queues = MqQueueNames.MALL_ORDER_PAID_PROMOTION,
            ackMode = "MANUAL",
            containerFactory = RabbitListenerConfiguration.LISTENER_CONTAINER_FACTORY)
    public void onMessage(Message message, Channel channel) throws IOException {
        handleMessage(message, channel, OrderPaidFollowupMessage.class, this::confirmCoupon);
    }

    @Override
    protected String resolveIdempotentKey(Message message, MessageEnvelope<OrderPaidFollowupMessage> envelope) {
        OrderPaidFollowupMessage payload = envelope.getPayload();
        if (payload != null && payload.getOrderId() != null) {
            return "mall:order:paid:coupon:" + payload.getOrderId();
        }
        return super.resolveIdempotentKey(message, envelope);
    }

    private void confirmCoupon(OrderPaidFollowupMessage message) {
        if (message == null || message.getOrderId() == null) {
            return;
        }
        portalCouponService.confirmUsed(message.getOrderId());
    }
}
