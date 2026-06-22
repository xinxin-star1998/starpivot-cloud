package cn.org.starpivot.mq.config;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 监听器容器配置。
 */
@Configuration
public class RabbitListenerConfiguration {

    public static final String LISTENER_CONTAINER_FACTORY = "starPivotRabbitListenerContainerFactory";

    @Bean(name = LISTENER_CONTAINER_FACTORY)
    public SimpleRabbitListenerContainerFactory starPivotRabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory,
            StarPivotMqProperties mqProperties) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setPrefetchCount(mqProperties.getListener().getPrefetch());
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        return factory;
    }
}
