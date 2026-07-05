package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.oms.OmsConstants;
import cn.org.starpivot.mall.oms.domain.vo.RefundAlertSummaryVo;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.entity.OmsRefundInfo;
import cn.org.starpivot.mall.oms.mapper.OmsOrderReturnApplyMapper;
import cn.org.starpivot.mall.oms.mapper.OmsRefundInfoMapper;
import cn.org.starpivot.mall.oms.service.OmsRefundAlertService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmsRefundAlertServiceImpl implements OmsRefundAlertService {

    private static final int RECENT_LIMIT = 5;
    private static final int ALERT_ACK_READ = 1;
    private static final int ALERT_ACK_UNREAD = 0;

    private final OmsRefundInfoMapper omsRefundInfoMapper;
    private final OmsOrderReturnApplyMapper omsOrderReturnApplyMapper;

    @Override
    @Transactional(readOnly = true)
    public RefundAlertSummaryVo summary() {
        long unread = countUnreadFailures();
        List<OmsRefundInfo> recent = omsRefundInfoMapper.selectList(
                Wrappers.<OmsRefundInfo>lambdaQuery()
                        .eq(OmsRefundInfo::getRefundStatus, OmsConstants.REFUND_STATUS_FAILED)
                        .eq(OmsRefundInfo::getAlertAck, ALERT_ACK_UNREAD)
                        .orderByDesc(OmsRefundInfo::getId)
                        .last("LIMIT " + RECENT_LIMIT));
        Map<Long, String> orderSnByReturnId = loadOrderSnMap(recent);

        RefundAlertSummaryVo vo = new RefundAlertSummaryVo();
        vo.setUnreadCount(unread);
        vo.setRecentItems(recent.stream().map(item -> toAlertItem(item, orderSnByReturnId)).collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void acknowledge(Long refundId) {
        OmsRefundInfo refund = omsRefundInfoMapper.selectById(refundId);
        if (refund == null) {
            throw new BizException("退款记录不存在");
        }
        if (!Integer.valueOf(OmsConstants.REFUND_STATUS_FAILED).equals(refund.getRefundStatus())) {
            return;
        }
        OmsRefundInfo patch = new OmsRefundInfo();
        patch.setId(refundId);
        patch.setAlertAck(ALERT_ACK_READ);
        omsRefundInfoMapper.updateById(patch);
    }

    @Override
    public void onStatusChanged(
            Integer previousStatus,
            Integer newStatus,
            Long refundId,
            String refundSn,
            String orderSn) {
        if (!Integer.valueOf(OmsConstants.REFUND_STATUS_FAILED).equals(newStatus)) {
            return;
        }
        if (Integer.valueOf(OmsConstants.REFUND_STATUS_FAILED).equals(previousStatus)) {
            return;
        }
        OmsRefundInfo patch = new OmsRefundInfo();
        patch.setId(refundId);
        patch.setAlertAck(ALERT_ACK_UNREAD);
        omsRefundInfoMapper.updateById(patch);
        log.error("Refund failure alert: refundId={}, refundSn={}, orderSn={}", refundId, refundSn, orderSn);
    }

    private long countUnreadFailures() {
        return omsRefundInfoMapper.selectCount(
                Wrappers.<OmsRefundInfo>lambdaQuery()
                        .eq(OmsRefundInfo::getRefundStatus, OmsConstants.REFUND_STATUS_FAILED)
                        .eq(OmsRefundInfo::getAlertAck, ALERT_ACK_UNREAD));
    }

    private Map<Long, String> loadOrderSnMap(List<OmsRefundInfo> records) {
        List<Long> returnIds = records.stream()
                .map(OmsRefundInfo::getOrderReturnId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (returnIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return omsOrderReturnApplyMapper.selectList(
                        Wrappers.<OmsOrderReturnApply>lambdaQuery()
                                .in(OmsOrderReturnApply::getId, returnIds)
                                .select(OmsOrderReturnApply::getId, OmsOrderReturnApply::getOrderSn))
                .stream()
                .collect(Collectors.toMap(
                        OmsOrderReturnApply::getId,
                        OmsOrderReturnApply::getOrderSn,
                        (left, right) -> left));
    }

    private RefundAlertSummaryVo.RefundAlertItemVo toAlertItem(
            OmsRefundInfo refund,
            Map<Long, String> orderSnByReturnId) {
        RefundAlertSummaryVo.RefundAlertItemVo item = new RefundAlertSummaryVo.RefundAlertItemVo();
        item.setId(refund.getId());
        item.setRefundSn(refund.getRefundSn());
        item.setRefund(refund.getRefund());
        item.setRefundChannel(refund.getRefundChannel());
        if (refund.getOrderReturnId() != null) {
            item.setOrderSn(orderSnByReturnId.get(refund.getOrderReturnId()));
        }
        return item;
    }
}
