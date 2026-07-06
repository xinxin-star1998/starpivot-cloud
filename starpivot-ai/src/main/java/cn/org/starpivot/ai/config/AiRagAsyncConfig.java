package cn.org.starpivot.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@EnableRetry
public class AiRagAsyncConfig {

    @Bean(name = "indexTaskExecutor")
    public Executor indexTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ai-index-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "usageLogExecutor")
    public Executor usageLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("ai-usage-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "ragRetrievalExecutor")
    public Executor ragRetrievalExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("ai-rag-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "chatStreamExecutor")
    public Executor chatStreamExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("ai-chat-");
        executor.setTaskDecorator(this::wrapWithSecurityContext);
        executor.initialize();
        return executor;
    }

    private Runnable wrapWithSecurityContext(Runnable task) {
        SecurityContext context = SecurityContextHolder.getContext();
        return () -> {
            SecurityContext previous = SecurityContextHolder.getContext();
            try {
                SecurityContextHolder.setContext(context);
                task.run();
            } finally {
                SecurityContextHolder.setContext(previous);
            }
        };
    }
}
