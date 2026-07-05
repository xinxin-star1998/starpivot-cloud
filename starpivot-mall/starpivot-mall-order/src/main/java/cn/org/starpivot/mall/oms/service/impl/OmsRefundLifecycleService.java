package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.mall.oms.OmsConstants;
import cn.org.starpivot.mall.oms.entity.OmsRefundInfo;
import cn.org.starpivot.mall.oms.mapper.OmsRefundInfoMapper;
import cn.org.starpivot.mall.oms.service.OmsRefundSyncService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmsRefundLifecycleService {

    private static final int BATCH_SIZE = 50;

    private final OmsRefundInfoMapper omsRefundInfoMapper;
    private final OmsRefundSyncService omsRefundSyncService;

    public int syncPendingRefunds() {
        List<OmsRefundInfo> pending = omsRefundInfoMapper.selectList(
                Wrappers.<OmsRefundInfo>lambdaQuery()
                        .in(OmsRefundInfo::getRefundStatus,
                                OmsConstants.REFUND_STATUS_PENDING,
                                OmsConstants.REFUND_STATUS_PROCESSING)
                        .orderByAsc(OmsRefundInfo::getId)
                        .last("LIMIT " + BATCH_SIZE));
        int synced = 0;
        int failed = 0;
        for (OmsRefundInfo refund : pending) {
            try {
                omsRefundSyncService.syncStatus(refund.getId());
                synced++;
            } catch (Exception ex) {
                failed++;
                log.warn("Auto refund sync failed, refundId={}, refundSn={}",
                        refund.getId(), refund.getRefundSn(), ex);
            }
        }
        if (failed > 0) {
            log.warn("Refund auto-sync finished with failures, synced={}, failed={}", synced, failed);
        }
        return synced;
    }
}
