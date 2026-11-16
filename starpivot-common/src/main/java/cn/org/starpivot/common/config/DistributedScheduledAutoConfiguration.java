package cn.org.starpivot.common.config;

import cn.org.starpivot.common.schedule.DistributedScheduledLockAspect;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务分布式锁自动配置。
 */
@AutoConfiguration(after = StarPivotSchedulingAutoConfiguration.class)
@ConditionalOnClass({Scheduled.class, org.aspectj.lang.annotation.Aspect.class, RedissonClient.class})
@ConditionalOnBean(RedissonClient.class)
@EnableAspectJAutoProxy
public class DistributedScheduledAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DistributedScheduledLockAspect distributedScheduledLockAspect(
            RedissonClient redissonClient) {
        return new DistributedScheduledLockAspect(redissonClient);
    }
}
