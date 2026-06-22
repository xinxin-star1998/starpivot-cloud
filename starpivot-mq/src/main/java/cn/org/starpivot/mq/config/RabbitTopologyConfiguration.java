package cn.org.starpivot.mq.config;

import cn.org.starpivot.api.event.MqExchangeNames;
import cn.org.starpivot.api.event.MqQueueNames;
import cn.org.starpivot.api.event.MqRoutingKeys;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ Exchange / Queue / Binding 声明。
 */
@Configuration
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
}
