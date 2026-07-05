package cn.org.starpivot.system.config;

import cn.org.starpivot.api.system.constant.MessageConstants;
import cn.org.starpivot.system.service.MessagePushService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Configuration
public class MessagePushRedisConfiguration {

    @Bean
    RedisMessageListenerContainer messagePushListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessagePushService messagePushService) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(messagePushService, "dispatchFromRedis");
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(adapter, new ChannelTopic(MessageConstants.REDIS_CHANNEL_MESSAGE_PUSH));
        return container;
    }
}
