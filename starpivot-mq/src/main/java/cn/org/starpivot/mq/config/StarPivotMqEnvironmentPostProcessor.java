package cn.org.starpivot.mq.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * MQ 未启用时排除 {@code RabbitAutoConfiguration}，避免强依赖 RabbitMQ  Broker。
 */
public class StarPivotMqEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String EXCLUDE_KEY = "spring.autoconfigure.exclude";
    private static final String RABBIT_AUTO_CONFIG =
            "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isMqEnabled(environment)) {
            return;
        }
        Set<String> excludes = new LinkedHashSet<>();
        String existing = environment.getProperty(EXCLUDE_KEY, "");
        if (!existing.isBlank()) {
            for (String item : existing.split(",")) {
                if (!item.isBlank()) {
                    excludes.add(item.trim());
                }
            }
        }
        excludes.add(RABBIT_AUTO_CONFIG);
        environment.getPropertySources().addFirst(new MapPropertySource(
                "starpivotMqAutoConfigurationExclude",
                java.util.Map.of(EXCLUDE_KEY, String.join(",", excludes))));
    }

    private boolean isMqEnabled(ConfigurableEnvironment environment) {
        return environment.getProperty("starpivot.mq.enabled", Boolean.class, false);
    }
}
