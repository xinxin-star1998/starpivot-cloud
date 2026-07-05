package cn.org.starpivot.mall.oms.schedule;

import cn.org.starpivot.common.annotation.DistributedScheduled;
import cn.org.starpivot.mall.oms.service.impl.OmsRefundLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 退款状态定时同步：扫描退款中记录并向支付渠道查询最新状态。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OmsRefundLifecycleScheduler {

    private final OmsRefundLifecycleService omsRefundLifecycleService;

    @DistributedScheduled(key = "mall:refund-sync", lockTtlSeconds = 290)
    @Scheduled(fixedDelayString = "${starpivot.mall.refund-sync-scan-ms:300000}")
    public void syncPendingRefunds() {
        int synced = omsRefundLifecycleService.syncPendingRefunds();
        if (synced > 0) {
            log.info("Refund auto-sync scan: synced={}", synced);
        }
    }
}
