package cn.org.starpivot.common.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;

/**
 * Nacos / Spring Cloud 配置变更时清空 JWT 密钥缓存，避免 secret 热更新后仍用旧 Key。
 */
@AutoConfiguration
@ConditionalOnClass(EnvironmentChangeEvent.class)
public class JwtSecretRefreshAutoConfiguration {

    @Bean
    public JwtSecretRefreshListener jwtSecretRefreshListener() {
        return new JwtSecretRefreshListener();
    }

    @Slf4j
    public static class JwtSecretRefreshListener {

        @EventListener
        public void onEnvironmentChange(EnvironmentChangeEvent event) {
            boolean jwtRelated = event.getKeys().stream()
                    .anyMatch(key -> key.startsWith("starpivot.jwt"));
            if (jwtRelated) {
                JwtUtils.clearCache();
                log.info("JWT key/parser cache cleared after config change, keys={}", event.getKeys());
            }
        }
    }
}
