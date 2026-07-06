package cn.org.starpivot.ai.config;

import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Nacos 配置热刷新时同步重建 AI 运行时快照。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiRuntimeConfigRefreshListener {

    private final AiRuntimeConfigService aiRuntimeConfigService;

    @EventListener
    public void onEnvironmentChange(EnvironmentChangeEvent event) {
        boolean aiRelated = event.getKeys().stream()
                .anyMatch(key -> key.startsWith("starpivot.ai") || key.startsWith("spring.ai"));
        if (aiRelated) {
            aiRuntimeConfigService.refresh();
            log.info("AI runtime config refreshed after Nacos change, keys={}", event.getKeys());
        }
    }
}
