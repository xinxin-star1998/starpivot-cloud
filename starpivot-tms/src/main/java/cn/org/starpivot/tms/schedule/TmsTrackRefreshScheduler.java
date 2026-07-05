package cn.org.starpivot.tms.schedule;

import cn.org.starpivot.tms.config.TmsProperties;
import cn.org.starpivot.tms.integration.Kuaidi100Client;
import cn.org.starpivot.tms.service.TmsShipmentService;
import cn.org.starpivot.common.annotation.DistributedScheduled;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时刷新在途运单轨迹（需开启快递100）。
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "starpivot.tms.kuaidi100", name = "enabled", havingValue = "true")
public class TmsTrackRefreshScheduler {

    private final TmsShipmentService shipmentService;
    private final TmsProperties tmsProperties;
    private final Kuaidi100Client kuaidi100Client;

    @DistributedScheduled(key = "tms:track-refresh", lockTtlSeconds = 280)
    @Scheduled(fixedDelayString = "${starpivot.tms.track-refresh-scan-ms:300000}")
    public void refreshPendingTracks() {
        if (!kuaidi100Client.isEnabled()) {
            return;
        }
        long scanMs = tmsProperties.getTrackRefreshScanMs();
        if (scanMs <= 0) {
            return;
        }
        int batchSize = Math.max(1, tmsProperties.getTrackRefreshBatchSize());
        shipmentService.refreshPendingTracks(batchSize);
    }
}
