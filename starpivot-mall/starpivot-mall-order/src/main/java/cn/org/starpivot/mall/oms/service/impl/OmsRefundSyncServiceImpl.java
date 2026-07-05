package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.oms.OmsConstants;
import cn.org.starpivot.mall.oms.domain.vo.RefundVo;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.entity.OmsPaymentInfo;
import cn.org.starpivot.mall.oms.entity.OmsRefundInfo;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderReturnApplyMapper;
import cn.org.starpivot.mall.oms.mapper.OmsPaymentInfoMapper;
import cn.org.starpivot.mall.oms.mapper.OmsRefundInfoMapper;
import cn.org.starpivot.mall.oms.service.OmsRefundAlertService;
import cn.org.starpivot.mall.oms.service.OmsRefundInfoService;
import cn.org.starpivot.mall.oms.service.OmsRefundSyncService;
import cn.org.starpivot.mall.pay.domain.vo.AlipayRefundResult;
import cn.org.starpivot.mall.pay.domain.vo.WxRefundResult;
import cn.org.starpivot.mall.pay.service.AlipayPayService;
import cn.org.starpivot.mall.pay.service.WxPayService;
import cn.org.starpivot.mall.portal.PortalConstants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class OmsRefundSyncServiceImpl implements OmsRefundSyncService {

    private final OmsRefundInfoMapper omsRefundInfoMapper;
    private final OmsOrderReturnApplyMapper omsOrderReturnApplyMapper;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsPaymentInfoMapper omsPaymentInfoMapper;
    private final AlipayPayService alipayPayService;
    private final WxPayService wxPayService;
    private final OmsRefundInfoService omsRefundInfoService;
    private final OmsRefundAlertService omsRefundAlertService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundVo syncStatus(Long refundId) {
        OmsRefundInfo refund = omsRefundInfoMapper.selectById(refundId);
        if (refund == null) {
            throw new BizException("退款记录不存在");
        }
        if (!StringUtils.hasText(refund.getRefundSn())) {
            throw new BizException("退款单号无效，无法同步");
        }
        if (Integer.valueOf(OmsConstants.REFUND_STATUS_SUCCESS).equals(refund.getRefundStatus())) {
            return omsRefundInfoService.getDetailById(refundId);
        }

        RefundContext context = loadContext(refund);
        SyncOutcome outcome = queryChannelStatus(refund, context);
        if (outcome == null) {
            throw new BizException("当前退款记录不支持向支付渠道同步状态");
        }

        Integer previousStatus = refund.getRefundStatus();
        OmsRefundInfo patch = new OmsRefundInfo();
        patch.setId(refundId);
        patch.setRefundStatus(outcome.refundStatus());
        if (StringUtils.hasText(outcome.rawResponse())) {
            patch.setRefundContent(outcome.rawResponse());
        }
        omsRefundInfoMapper.updateById(patch);
        omsRefundAlertService.onStatusChanged(
                previousStatus,
                outcome.refundStatus(),
                refundId,
                refund.getRefundSn(),
                context.orderSn());
        return omsRefundInfoService.getDetailById(refundId);
    }

    private SyncOutcome queryChannelStatus(OmsRefundInfo refund, RefundContext context) {
        Integer channel = refund.getRefundChannel();
        if (Integer.valueOf(PortalConstants.PAY_TYPE_ALIPAY).equals(channel)) {
            return syncAlipay(refund, context);
        }
        if (Integer.valueOf(PortalConstants.PAY_TYPE_WECHAT).equals(channel)) {
            return syncWechat(refund);
        }
        return null;
    }

    private SyncOutcome syncAlipay(OmsRefundInfo refund, RefundContext context) {
        if (!alipayPayService.isAvailable()) {
            throw new BizException("支付宝未配置，无法同步退款状态");
        }
        if (context.tradeNo() == null || isMockTradeNo(context.tradeNo())) {
            throw new BizException("Mock/无支付流水，无法向支付宝查询");
        }
        AlipayRefundResult result = alipayPayService.queryRefund(
                context.orderSn(), context.tradeNo(), refund.getRefundSn());
        return new SyncOutcome(mapAlipayRefundStatus(result.getFundChange()), result.getRawResponse());
    }

    private SyncOutcome syncWechat(OmsRefundInfo refund) {
        if (!wxPayService.isConfigured()) {
            throw new BizException("微信支付未配置，无法同步退款状态");
        }
        WxRefundResult result = wxPayService.queryRefund(refund.getRefundSn());
        return new SyncOutcome(mapWxRefundStatus(result.getStatus()), result.getRawResponse());
    }

    private RefundContext loadContext(OmsRefundInfo refund) {
        if (refund.getOrderReturnId() == null) {
            throw new BizException("退款记录未关联退货申请");
        }
        OmsOrderReturnApply apply = omsOrderReturnApplyMapper.selectById(refund.getOrderReturnId());
        if (apply == null) {
            throw new BizException("关联退货申请不存在");
        }
        OmsOrder order = apply.getOrderId() != null ? omsOrderMapper.selectById(apply.getOrderId()) : null;
        String orderSn = apply.getOrderSn();
        if (!StringUtils.hasText(orderSn) && order != null) {
            orderSn = order.getOrderSn();
        }
        OmsPaymentInfo payment = null;
        if (order != null && order.getId() != null) {
            payment = omsPaymentInfoMapper.selectOne(
                    Wrappers.<OmsPaymentInfo>lambdaQuery()
                            .eq(OmsPaymentInfo::getOrderId, order.getId())
                            .orderByDesc(OmsPaymentInfo::getId)
                            .last("LIMIT 1"));
        }
        String tradeNo = payment != null ? payment.getAlipayTradeNo() : null;
        return new RefundContext(orderSn, tradeNo);
    }

    private Integer mapAlipayRefundStatus(String refundStatus) {
        if (!StringUtils.hasText(refundStatus)) {
            return OmsConstants.REFUND_STATUS_PROCESSING;
        }
        return switch (refundStatus.trim().toUpperCase()) {
            case "REFUND_SUCCESS" -> OmsConstants.REFUND_STATUS_SUCCESS;
            case "REFUND_CLOSED" -> OmsConstants.REFUND_STATUS_FAILED;
            default -> OmsConstants.REFUND_STATUS_PROCESSING;
        };
    }

    private Integer mapWxRefundStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return OmsConstants.REFUND_STATUS_PROCESSING;
        }
        return switch (status.trim().toUpperCase()) {
            case "SUCCESS" -> OmsConstants.REFUND_STATUS_SUCCESS;
            case "PROCESSING" -> OmsConstants.REFUND_STATUS_PROCESSING;
            case "CLOSED", "ABNORMAL" -> OmsConstants.REFUND_STATUS_FAILED;
            default -> OmsConstants.REFUND_STATUS_PROCESSING;
        };
    }

    private boolean isMockTradeNo(String tradeNo) {
        String upper = tradeNo.trim().toUpperCase();
        return upper.startsWith("MOCK") || upper.startsWith("WXMOCK");
    }

    private record RefundContext(String orderSn, String tradeNo) {
    }

    private record SyncOutcome(int refundStatus, String rawResponse) {
    }
}
