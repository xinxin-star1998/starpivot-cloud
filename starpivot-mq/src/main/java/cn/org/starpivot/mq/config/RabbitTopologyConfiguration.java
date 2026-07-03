package cn.org.starpivot.mq.config;

import cn.org.starpivot.api.event.MqExchangeNames;
import cn.org.starpivot.api.event.MqQueueNames;
import cn.org.starpivot.api.event.MqRoutingKeys;
import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ Exchange / Queue / Binding 声明。
 */
@Configuration
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
public class RabbitTopologyConfiguration {

    @Bean
    public TopicExchange starPivotTopicExchange() {
        return new TopicExchange(MqExchangeNames.TOPIC, true, false);
    }

    @Bean
    public DirectExchange starPivotDirectExchange() {
        return new DirectExchange(MqExchangeNames.DIRECT, true, false);
    }

    @Bean
    public TopicExchange starPivotDlxExchange() {
        return new TopicExchange(MqExchangeNames.DLX, true, false);
    }

    @Bean
    public Queue systemLoginLogQueue() {
        return QueueBuilder.durable(MqQueueNames.SYSTEM_LOGIN_LOG)
                .deadLetterExchange(MqExchangeNames.DLX)
                .deadLetterRoutingKey("dlx." + MqRoutingKeys.AUDIT_LOGIN_LOG_CREATED)
                .build();
    }

    @Bean
    public Queue systemOperLogQueue() {
        return QueueBuilder.durable(MqQueueNames.SYSTEM_OPER_LOG)
                .deadLetterExchange(MqExchangeNames.DLX)
                .deadLetterRoutingKey("dlx." + MqRoutingKeys.AUDIT_OPER_LOG_CREATED)
                .build();
    }

    @Bean
    public Queue systemJobHandlerQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", MqExchangeNames.DLX);
        args.put("x-dead-letter-routing-key", "dlx." + MqRoutingKeys.JOB_OPER_LOG_CLEAN);
        return new Queue(MqQueueNames.SYSTEM_JOB_HANDLER, true, false, false, args);
    }

    @Bean
    public Binding systemLoginLogBinding(Queue systemLoginLogQueue, TopicExchange starPivotTopicExchange) {
        return BindingBuilder.bind(systemLoginLogQueue)
                .to(starPivotTopicExchange)
                .with(MqRoutingKeys.AUDIT_LOGIN_LOG_CREATED);
    }

    @Bean
    public Binding systemOperLogBinding(Queue systemOperLogQueue, TopicExchange starPivotTopicExchange) {
        return BindingBuilder.bind(systemOperLogQueue)
                .to(starPivotTopicExchange)
                .with(MqRoutingKeys.AUDIT_OPER_LOG_CREATED);
    }

    @Bean
    public Binding systemJobHandlerBinding(Queue systemJobHandlerQueue, DirectExchange starPivotDirectExchange) {
        return BindingBuilder.bind(systemJobHandlerQueue)
                .to(starPivotDirectExchange)
                .with(MqRoutingKeys.JOB_OPER_LOG_CLEAN);
    }

    @Bean
    public Queue mallApprovalFinishedPurchaseQueue() {
        return mallApprovalQueue(MqQueueNames.MALL_APPROVAL_FINISHED_PURCHASE, "purchase");
    }

    @Bean
    public Queue mallApprovalFinishedSpuQueue() {
        return mallApprovalQueue(MqQueueNames.MALL_APPROVAL_FINISHED_SPU, "spu");
    }

    @Bean
    public Queue mallApprovalFinishedReturnQueue() {
        return mallApprovalQueue(MqQueueNames.MALL_APPROVAL_FINISHED_RETURN, "return");
    }

    @Bean
    public Queue mallApprovalFinishedCouponQueue() {
        return mallApprovalQueue(MqQueueNames.MALL_APPROVAL_FINISHED_COUPON, "coupon");
    }

    @Bean
    public Binding mallApprovalFinishedPurchaseBinding(Queue mallApprovalFinishedPurchaseQueue,
                                                       TopicExchange starPivotTopicExchange) {
        return mallApprovalBinding(mallApprovalFinishedPurchaseQueue, starPivotTopicExchange, "purchase");
    }

    @Bean
    public Binding mallApprovalFinishedSpuBinding(Queue mallApprovalFinishedSpuQueue,
                                                  TopicExchange starPivotTopicExchange) {
        return mallApprovalBinding(mallApprovalFinishedSpuQueue, starPivotTopicExchange, "spu");
    }

    @Bean
    public Binding mallApprovalFinishedReturnBinding(Queue mallApprovalFinishedReturnQueue,
                                                     TopicExchange starPivotTopicExchange) {
        return mallApprovalBinding(mallApprovalFinishedReturnQueue, starPivotTopicExchange, "return");
    }

    @Bean
    public Binding mallApprovalFinishedCouponBinding(Queue mallApprovalFinishedCouponQueue,
                                                     TopicExchange starPivotTopicExchange) {
        return mallApprovalBinding(mallApprovalFinishedCouponQueue, starPivotTopicExchange, "coupon");
    }

    private Queue mallApprovalQueue(String queueName, String bizType) {
        String routingKey = MqRoutingKeys.mallApprovalFinished(bizType);
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(MqExchangeNames.DLX)
                .deadLetterRoutingKey("dlx." + routingKey)
                .build();
    }

    private Binding mallApprovalBinding(Queue queue, TopicExchange exchange, String bizType) {
        return BindingBuilder.bind(queue)
                .to(exchange)
                .with(MqRoutingKeys.mallApprovalFinished(bizType));
    }
}
