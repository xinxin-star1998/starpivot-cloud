package cn.org.starpivot.mq.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * StarPivot MQ Starter 自动配置入口。
 */
@AutoConfiguration(after = RabbitAutoConfiguration.class)
@ConditionalOnClass(name = "org.springframework.amqp.rabbit.core.RabbitTemplate")
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(StarPivotMqProperties.class)
@EnableRabbit
@ComponentScan(basePackages = "cn.org.starpivot.mq")
@Import({
        RabbitConnectionConfiguration.class,
        RabbitTopologyConfiguration.class,
        RabbitListenerConfiguration.class
})
public class StarPivotMqAutoConfiguration {
}
