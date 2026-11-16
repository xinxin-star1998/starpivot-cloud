package cn.org.starpivot.mall.sms.schedule;

import cn.org.starpivot.common.annotation.DistributedScheduled;
import cn.org.starpivot.mall.portal.service.PortalBargainService;
import cn.org.starpivot.mall.portal.service.PortalGroupBuyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 拼团 / 砍价活动定时结算：
 * 关闭超时未支付的拼团记录、结算超时未成团团、失效超时砍价记录。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PromotionActivitySettleScheduler {

    private final PortalGroupBuyService portalGroupBuyService;
    private final PortalBargainService portalBargainService;

    @DistributedScheduled(key = "mall:promotion-activity-settle", lockTtlSeconds = 110)
    @Scheduled(fixedDelayString = "${starpivot.mall.promotion-settle-scan-ms:120000}")
    public void settleExpired() {
        int groupHandled = portalGroupBuyService.settleExpired();
        int bargainHandled = portalBargainService.settleExpired();
        if (groupHandled > 0 || bargainHandled > 0) {
            log.info("拼团/砍价定时结算: group={}, bargain={}", groupHandled, bargainHandled);
        }
    }
}
