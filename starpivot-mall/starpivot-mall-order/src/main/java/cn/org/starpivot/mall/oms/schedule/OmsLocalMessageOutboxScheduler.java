package cn.org.starpivot.mall.oms.schedule;

import cn.org.starpivot.mall.oms.service.OmsLocalMessageOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 本地消息表定时投递（Outbox 模式）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(OmsLocalMessageOutboxService.class)
@ConditionalOnProperty(prefix = "starpivot.mall.local-message", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OmsLocalMessageOutboxScheduler {

    private final OmsLocalMessageOutboxService localMessageOutboxService;

    @Scheduled(fixedDelayString = "${starpivot.mall.local-message.flush-ms:60000}")
    public void flushPendingMessages() {
        int count = localMessageOutboxService.flushPending();
        if (count > 0) {
            log.debug("本地消息表投递完成: count={}", count);
        }
    }
}
