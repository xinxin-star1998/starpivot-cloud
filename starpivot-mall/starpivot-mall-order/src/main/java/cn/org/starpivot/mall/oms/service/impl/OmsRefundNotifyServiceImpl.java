package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.mall.oms.OmsConstants;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.entity.OmsRefundInfo;
import cn.org.starpivot.mall.oms.mapper.OmsOrderReturnApplyMapper;
import cn.org.starpivot.mall.oms.mapper.OmsRefundInfoMapper;
import cn.org.starpivot.mall.oms.service.OmsRefundAlertService;
import cn.org.starpivot.mall.oms.service.OmsRefundNotifyService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmsRefundNotifyServiceImpl implements OmsRefundNotifyService {

    private final OmsRefundInfoMapper omsRefundInfoMapper;
    private final OmsOrderReturnApplyMapper omsOrderReturnApplyMapper;
    private final OmsRefundAlertService omsRefundAlertService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleWxRefundNotify(String outRefundNo, String refundStatus, String callbackJson) {
        if (!StringUtils.hasText(outRefundNo)) {
            return;
        }
        OmsRefundInfo refund = omsRefundInfoMapper.selectOne(
                Wrappers.<OmsRefundInfo>lambdaQuery()
                        .eq(OmsRefundInfo::getRefundSn, outRefundNo)
                        .last("LIMIT 1"));
        if (refund == null) {
            log.warn("Wx refund notify: refund record not found, outRefundNo={}", outRefundNo);
            return;
        }

        Integer targetStatus = mapWxRefundStatus(refundStatus);
        if (targetStatus == null) {
            log.warn("Wx refund notify: unknown status={}, outRefundNo={}", refundStatus, outRefundNo);
            return;
        }

        if (Integer.valueOf(OmsConstants.REFUND_STATUS_SUCCESS).equals(refund.getRefundStatus())
                && Integer.valueOf(OmsConstants.REFUND_STATUS_SUCCESS).equals(targetStatus)) {
            updateCallbackOnly(refund.getId(), callbackJson);
            return;
        }

        OmsRefundInfo patch = new OmsRefundInfo();
        patch.setId(refund.getId());
        patch.setRefundStatus(targetStatus);
        if (StringUtils.hasText(callbackJson)) {
            patch.setRefundContent(callbackJson);
        }
        omsRefundInfoMapper.updateById(patch);
        omsRefundAlertService.onStatusChanged(
                refund.getRefundStatus(),
                targetStatus,
                refund.getId(),
                refund.getRefundSn(),
                resolveOrderSn(refund));
        log.info("Wx refund notify updated: outRefundNo={}, status={}", outRefundNo, refundStatus);
    }

    private String resolveOrderSn(OmsRefundInfo refund) {
        if (refund.getOrderReturnId() == null) {
            return null;
        }
        OmsOrderReturnApply apply = omsOrderReturnApplyMapper.selectById(refund.getOrderReturnId());
        return apply != null ? apply.getOrderSn() : null;
    }

    private void updateCallbackOnly(Long refundId, String callbackJson) {
        if (!StringUtils.hasText(callbackJson)) {
            return;
        }
        OmsRefundInfo patch = new OmsRefundInfo();
        patch.setId(refundId);
        patch.setRefundContent(callbackJson);
        omsRefundInfoMapper.updateById(patch);
    }

    private Integer mapWxRefundStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        return switch (status.trim().toUpperCase()) {
            case "SUCCESS" -> OmsConstants.REFUND_STATUS_SUCCESS;
            case "PROCESSING" -> OmsConstants.REFUND_STATUS_PROCESSING;
            case "CLOSED", "ABNORMAL" -> OmsConstants.REFUND_STATUS_FAILED;
            default -> null;
        };
    }
}
