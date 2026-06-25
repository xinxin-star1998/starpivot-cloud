package cn.org.starpivot.mall.oms.schedule;

import cn.org.starpivot.mall.oms.service.impl.OmsOrderLifecycleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单生命周期定时任务：自动确认收货、评价期结束标记。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OmsOrderLifecycleScheduler {

    private final OmsOrderLifecycleService omsOrderLifecycleService;

    @Scheduled(fixedDelayString = "${starpivot.mall.order-lifecycle-scan-ms:300000}")
    public void runLifecycleTasks() {
        int confirmed = omsOrderLifecycleService.autoConfirmDeliveredOrders();
        int finished = omsOrderLifecycleService.autoFinishCompletedOrders();
        if (confirmed > 0 || finished > 0) {
            log.info("Order lifecycle scan: autoConfirm={}, autoFinishComment={}", confirmed, finished);
        }
    }
}
