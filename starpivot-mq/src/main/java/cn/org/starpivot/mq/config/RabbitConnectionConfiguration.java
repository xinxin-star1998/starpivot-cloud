package cn.org.starpivot.mq.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 连接与 {@link RabbitTemplate} 配置。
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
public class RabbitConnectionConfiguration {

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         RabbitProperties rabbitProperties) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMandatory(true);
        template.setReturnsCallback(returned -> log.warn(
                "MQ 消息未路由: exchange={}, routingKey={}, replyCode={}",
                returned.getExchange(), returned.getRoutingKey(), returned.getReplyCode()));
        if (rabbitProperties.getPublisherConfirmType() != null) {
            template.setConfirmCallback((CorrelationData correlationData, boolean ack, String cause) -> {
                if (!ack) {
                    log.error("MQ Publisher Confirm 失败: id={}, cause={}",
                            correlationData != null ? correlationData.getId() : null, cause);
                }
            });
        }
        return template;
    }
}
