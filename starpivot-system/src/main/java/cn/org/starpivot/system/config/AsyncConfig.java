package cn.org.starpivot.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置类。
 * <p>
 * 为 {@code @Async} 注解及异步操作日志等后台任务提供命名线程池 {@code taskExecutor}。
 * </p>
 * <ul>
 *   <li>{@link Configuration} — 声明为 Spring 配置类</li>
 *   <li>{@link EnableAsync} — 启用 Spring 异步方法执行能力</li>
 * </ul>
 */
@Configuration
@EnableAsync
@EnableConfigurationProperties(AsyncConfig.AsyncThreadPoolProperties.class)
public class AsyncConfig {

    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

    /**
     * 创建通用异步任务线程池。
     * <p>
     * 默认核心线程数为 CPU 核心数 + 1，最大线程数为 CPU 核心数的 2 倍；
     * 队列容量 500；拒绝策略为调用线程执行；
     * 关闭时等待任务完成，最长等待 60 秒。
     * </p>
     *
     * @return 已初始化的 {@link Executor} 实例，Bean 名称为 {@code taskExecutor}
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor(AsyncThreadPoolProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getCorePoolSize());
        executor.setMaxPoolSize(properties.getMaxPoolSize());
        executor.setQueueCapacity(properties.getQueueCapacity());
        executor.setKeepAliveSeconds(properties.getKeepAliveSeconds());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds());
        executor.initialize();
        return executor;
    }

    @Data
    @ConfigurationProperties(prefix = "starpivot.async")
    public static class AsyncThreadPoolProperties {

        private int corePoolSize = CPU_CORES + 1;

        private int maxPoolSize = CPU_CORES * 2;

        private int queueCapacity = 500;

        private int keepAliveSeconds = 60;

        private String threadNamePrefix = "async-task-";

        private int awaitTerminationSeconds = 60;
    }
}
