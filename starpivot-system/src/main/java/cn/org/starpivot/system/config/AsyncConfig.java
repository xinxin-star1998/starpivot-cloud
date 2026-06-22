package cn.org.starpivot.system.config;

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
public class AsyncConfig {

    /**
     * 创建通用异步任务线程池。
     * <p>
     * 核心线程 5、最大 20、队列容量 1000；拒绝策略为调用线程执行；
     * 关闭时等待任务完成，最长等待 60 秒。
     * </p>
     *
     * @return 已初始化的 {@link Executor} 实例，Bean 名称为 {@code taskExecutor}
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("async-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
