package cn.org.starpivot.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * 统一定时任务线程池，避免多 {@code @Scheduled} 任务共用单线程调度器互相阻塞。
 */
@Configuration
@ConditionalOnClass(name = "org.springframework.scheduling.annotation.EnableScheduling")
public class StarPivotSchedulingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TaskScheduler.class)
    public ThreadPoolTaskScheduler taskScheduler(
            @Value("${starpivot.scheduling.pool-size:4}") int poolSize) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Math.max(1, poolSize));
        scheduler.setThreadNamePrefix("sp-schedule-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }
}
