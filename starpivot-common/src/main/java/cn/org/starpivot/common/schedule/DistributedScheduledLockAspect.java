package cn.org.starpivot.common.schedule;

import cn.org.starpivot.common.annotation.DistributedScheduled;
import cn.org.starpivot.common.cache.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.util.UUID;

/**
 * {@link DistributedScheduled} 切面：基于 Redis SET NX EX 的互斥执行。
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class DistributedScheduledLockAspect {

    private static final String LOCK_VALUE_PREFIX = "inst:";

    private final StringRedisTemplate stringRedisTemplate;
    private final String instanceId = LOCK_VALUE_PREFIX + UUID.randomUUID();

    @Around("@annotation(distributedScheduled)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedScheduled distributedScheduled) throws Throwable {
        String lockKey = CacheConstants.scheduleLockKey(distributedScheduled.key());
        long ttlSeconds = Math.max(1L, distributedScheduled.lockTtlSeconds());
        try {
            Boolean acquired = stringRedisTemplate.opsForValue()
                    .setIfAbsent(lockKey, instanceId, Duration.ofSeconds(ttlSeconds));
            if (!Boolean.TRUE.equals(acquired)) {
                if (distributedScheduled.skipIfLocked()) {
                    log.debug("Skip scheduled task, lock held: key={}", distributedScheduled.key());
                } else {
                    log.warn("Failed to acquire distributed lock, abort task: key={}", distributedScheduled.key());
                }
                return null;
            }
        } catch (Exception ex) {
            log.warn("Distributed lock unavailable, skip scheduled task: key={}", distributedScheduled.key(), ex);
            return null;
        }
        return joinPoint.proceed();
    }
}
