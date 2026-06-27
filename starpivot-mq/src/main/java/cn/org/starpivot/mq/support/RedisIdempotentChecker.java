package cn.org.starpivot.mq.support;

import cn.org.starpivot.mq.config.StarPivotMqProperties;
import cn.org.starpivot.mq.listener.IdempotentChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

/**
 * 基于 Redis SETNX 的幂等校验默认实现。
 */
@Component
@ConditionalOnProperty(prefix = "starpivot.mq", name = "enabled", havingValue = "true")
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnBean(StringRedisTemplate.class)
@RequiredArgsConstructor
public class RedisIdempotentChecker implements IdempotentChecker {

    private static final String KEY_PREFIX = "mq:idempotent:";

    private final StringRedisTemplate stringRedisTemplate;
    private final StarPivotMqProperties mqProperties;

    @Override
    public boolean tryAcquire(String messageId) {
        if (!StringUtils.hasText(messageId)) {
            return true;
        }
        Duration ttl = mqProperties.getIdempotentTtl();
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + messageId, "1", ttl);
        return Boolean.TRUE.equals(acquired);
    }
}
