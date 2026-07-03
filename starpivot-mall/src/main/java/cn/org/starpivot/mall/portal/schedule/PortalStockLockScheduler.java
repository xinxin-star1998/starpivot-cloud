package cn.org.starpivot.mall.portal.schedule;

import cn.org.starpivot.common.annotation.DistributedScheduled;
import cn.org.starpivot.mall.portal.service.PortalStockLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 待付款订单库存锁超时释放。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PortalStockLockScheduler {

    private final PortalStockLockService portalStockLockService;

    @DistributedScheduled(key = "mall:stock-lock-release", lockTtlSeconds = 55)
    @Scheduled(fixedDelayString = "${starpivot.mall.stock-lock-scan-ms:60000}")
    public void releaseExpiredLocks() {
        portalStockLockService.releaseExpiredLocks();
    }
}
