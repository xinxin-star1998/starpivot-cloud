package cn.org.starpivot.common.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * 跨库菜单权限加载失败计数，供 Prometheus 告警（{@code starpivot_permission_load_failures_total}）。
 */
@Slf4j
@Component
public class PermissionLoadFailureRecorder {

    private static final String METRIC_NAME = "starpivot.permission.load.failures";

    private final ObjectProvider<MeterRegistry> meterRegistryProvider;

    public PermissionLoadFailureRecorder(ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.meterRegistryProvider = meterRegistryProvider;
    }

    /**
     * 记录一次权限加载失败并输出 error 日志。
     *
     * @param service 服务标识，如 approval、mall-member
     * @param cause   根因异常
     */
    public void record(String service, Throwable cause) {
        log.error("Failed to load menu permissions from system DB [service={}]", service, cause);
        MeterRegistry registry = meterRegistryProvider.getIfAvailable();
        if (registry == null) {
            return;
        }
        Counter.builder(METRIC_NAME)
                .description("Cross-DB menu permission load failures")
                .tag("service", service)
                .register(registry)
                .increment();
    }
}
