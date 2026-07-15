package cn.org.starpivot.approval.schedule;

import cn.org.starpivot.approval.mq.ApprovalLocalMessageOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 审批本地消息表定时投递（Outbox）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnBean(ApprovalLocalMessageOutboxService.class)
@ConditionalOnProperty(
        prefix = "starpivot.approval.local-message",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class ApprovalLocalMessageOutboxScheduler {

    private final ApprovalLocalMessageOutboxService localMessageOutboxService;

    @Scheduled(fixedDelayString = "${starpivot.approval.local-message.flush-ms:60000}")
    public void flushPendingMessages() {
        int count = localMessageOutboxService.flushPending();
        if (count > 0) {
            log.debug("审批本地消息表投递完成: count={}", count);
        }
    }
}
