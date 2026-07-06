package cn.org.starpivot.ai.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Component
public class AiMetrics {

    private final MeterRegistry registry;

    public AiMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordChat(String requestType, String model, boolean success, long latencyMs) {
        registry.timer("ai.chat.latency",
                        "type", safeTag(requestType),
                        "model", safeTag(model),
                        "success", String.valueOf(success))
                .record(latencyMs, TimeUnit.MILLISECONDS);
        registry.counter("ai.chat.requests",
                        "type", safeTag(requestType),
                        "success", String.valueOf(success))
                .increment();
    }

    public void recordRag(long durationMs, int hitCount) {
        registry.timer("ai.rag.duration").record(durationMs, TimeUnit.MILLISECONDS);
        registry.summary("ai.rag.hits").record(hitCount);
    }

    public void recordRagStage(String stage, long durationMs) {
        registry.timer("ai.rag.stage.duration", "stage", safeTag(stage))
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    public void recordRateLimitRejected() {
        registry.counter("ai.chat.rate_limit.rejected").increment();
    }

    public void recordQueryRoute(String intent, boolean useRag, boolean autoRouted) {
        registry.counter("ai.chat.route",
                        "intent", safeTag(intent),
                        "use_rag", String.valueOf(useRag),
                        "auto", String.valueOf(autoRouted))
                .increment();
    }

    private String safeTag(String value) {
        return StringUtils.hasText(value) ? value.trim() : "unknown";
    }
}
