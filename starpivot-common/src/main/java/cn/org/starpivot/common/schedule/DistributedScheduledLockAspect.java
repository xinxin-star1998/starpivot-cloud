package cn.org.starpivot.common.schedule;

import cn.org.starpivot.common.annotation.DistributedScheduled;
import cn.org.starpivot.common.cache.CacheConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * {@link DistributedScheduled} 切面：基于 Redisson RLock 的互斥执行。
 * <p>
 * 固定租期模式：锁到期自动释放，任务结束不主动解锁，
 * 与注解约定的「lockTtlSeconds 略小于调度间隔」语义一致，防止同周期重复触发。
 * </p>
 */
@Slf4j
@Aspect
@RequiredArgsConstructor
public class DistributedScheduledLockAspect {

    private final RedissonClient redissonClient;

    @Around("@annotation(distributedScheduled)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedScheduled distributedScheduled) throws Throwable {
        String lockKey = CacheConstants.scheduleLockKey(distributedScheduled.key());
        long ttlSeconds = Math.max(1L, distributedScheduled.lockTtlSeconds());
        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired;
        try {
            acquired = lock.tryLock(0, ttlSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception ex) {
            log.warn("Distributed lock unavailable, skip scheduled task: key={}", distributedScheduled.key(), ex);
            return null;
        }
        if (!acquired) {
            if (distributedScheduled.skipIfLocked()) {
                log.debug("Skip scheduled task, lock held: key={}", distributedScheduled.key());
            } else {
                log.warn("Failed to acquire distributed lock, abort task: key={}", distributedScheduled.key());
            }
            return null;
        }
        // 不主动 unlock：锁按 lockTtlSeconds 到期自动释放，避免调度周期内其他实例重复触发
        return joinPoint.proceed();
    }
}
