package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.oms.OmsConstants;
import cn.org.starpivot.mall.oms.domain.vo.RefundVo;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.entity.OmsPaymentInfo;
import cn.org.starpivot.mall.oms.entity.OmsRefundInfo;
import cn.org.starpivot.mall.oms.mapper.OmsOrderReturnApplyMapper;
import cn.org.starpivot.mall.oms.mapper.OmsPaymentInfoMapper;
import cn.org.starpivot.mall.oms.mapper.OmsRefundInfoMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.service.OmsRefundAlertService;
import cn.org.starpivot.mall.oms.service.OmsRefundInfoService;
import cn.org.starpivot.mall.oms.service.OmsRefundProcessService;
import cn.org.starpivot.mall.pay.domain.vo.AlipayRefundResult;
import cn.org.starpivot.mall.pay.domain.vo.WxRefundResult;
import cn.org.starpivot.mall.pay.service.AlipayPayService;
import cn.org.starpivot.mall.pay.service.WxPayService;
import cn.org.starpivot.mall.portal.PortalConstants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmsRefundProcessServiceImpl implements OmsRefundProcessService {

    private final OmsRefundInfoMapper omsRefundInfoMapper;
    private final OmsPaymentInfoMapper omsPaymentInfoMapper;
    private final OmsOrderReturnApplyMapper omsOrderReturnApplyMapper;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsRefundInfoService omsRefundInfoService;
    private final OmsRefundAlertService omsRefundAlertService;
    private final AlipayPayService alipayPayService;
    private final WxPayService wxPayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRefundForReturn(OmsOrder order, OmsOrderReturnApply apply, BigDecimal refundAmount) {
        if (apply == null || apply.getId() == null) {
            return;
        }
        long exists = omsRefundInfoMapper.selectCount(
                Wrappers.<OmsRefundInfo>lambdaQuery().eq(OmsRefundInfo::getOrderReturnId, apply.getId()));
        if (exists > 0) {
            return;
        }

        validateRefundAmount(order, apply, refundAmount);

        String refundSn = "RF" + System.currentTimeMillis();
        RefundOutcome outcome = processChannelRefund(order, refundAmount, refundSn);

        OmsRefundInfo refund = new OmsRefundInfo();
        refund.setOrderReturnId(apply.getId());
        refund.setRefund(refundAmount);
        refund.setRefundSn(refundSn);
        refund.setRefundStatus(outcome.refundStatus());
        refund.setRefundChannel(outcome.refundChannel());
        refund.setRefundContent(outcome.content());
        refund.setAlertAck(0);
        omsRefundInfoMapper.insert(refund);
        notifyRefundFailure(null, outcome.refundStatus(), refund.getId(), refundSn, order);
        log.info("Created refund record for return apply id={}, amount={}, channel={}",
                apply.getId(), refundAmount, outcome.refundChannel());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundVo retryFailedRefund(Long refundId) {
        OmsRefundInfo refund = omsRefundInfoMapper.selectById(refundId);
        if (refund == null) {
            throw new BizException("退款记录不存在");
        }
        Integer status = refund.getRefundStatus();
        if (!Integer.valueOf(OmsConstants.REFUND_STATUS_FAILED).equals(status)
                && !Integer.valueOf(OmsConstants.REFUND_STATUS_PENDING).equals(status)) {
            throw new BizException("仅待退款或退款失败记录可重试原路退款");
        }
        if (!StringUtils.hasText(refund.getRefundSn())) {
            throw new BizException("退款单号无效，无法重试");
        }
        if (refund.getRefund() == null || refund.getRefund().signum() <= 0) {
            throw new BizException("退款金额无效，无法重试");
        }

        OmsOrder order = requireOrderForRefund(refund);
        Integer previousStatus = refund.getRefundStatus();
        RefundOutcome outcome = processChannelRefund(order, refund.getRefund(), refund.getRefundSn());

        OmsRefundInfo patch = new OmsRefundInfo();
        patch.setId(refundId);
        patch.setRefundStatus(outcome.refundStatus());
        patch.setRefundChannel(outcome.refundChannel());
        patch.setRefundContent(outcome.content());
        omsRefundInfoMapper.updateById(patch);
        notifyRefundFailure(previousStatus, outcome.refundStatus(), refundId, refund.getRefundSn(), order);
        log.info("Retried channel refund, refundId={}, status={}", refundId, outcome.refundStatus());
        return omsRefundInfoService.getDetailById(refundId);
    }

    private OmsOrder requireOrderForRefund(OmsRefundInfo refund) {
        if (refund.getOrderReturnId() == null) {
            throw new BizException("退款记录未关联退货申请");
        }
        OmsOrderReturnApply apply = omsOrderReturnApplyMapper.selectById(refund.getOrderReturnId());
        if (apply == null || apply.getOrderId() == null) {
            throw new BizException("关联退货申请或订单不存在");
        }
        OmsOrder order = omsOrderMapper.selectById(apply.getOrderId());
        if (order == null) {
            throw new BizException("关联订单不存在");
        }
        return order;
    }

    private void validateRefundAmount(OmsOrder order, OmsOrderReturnApply apply, BigDecimal refundAmount) {
        if (order == null || refundAmount == null || refundAmount.signum() <= 0) {
            return;
        }
        BigDecimal payAmount = order.getPayAmount();
        if (payAmount == null || payAmount.signum() <= 0) {
            throw new BizException("订单实付金额无效，无法退款");
        }
        BigDecimal normalizedRefund = refundAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal normalizedPay = payAmount.setScale(2, RoundingMode.HALF_UP);
        if (normalizedRefund.compareTo(normalizedPay) > 0) {
            throw new BizException("退款金额不能大于订单实付金额");
        }

        List<OmsOrderReturnApply> siblingApplies = omsOrderReturnApplyMapper.selectList(
                Wrappers.<OmsOrderReturnApply>lambdaQuery()
                        .eq(OmsOrderReturnApply::getOrderId, order.getId())
                        .ne(OmsOrderReturnApply::getId, apply.getId()));
        BigDecimal alreadyRefunded = BigDecimal.ZERO;
        for (OmsOrderReturnApply sibling : siblingApplies) {
            OmsRefundInfo existing = omsRefundInfoMapper.selectOne(
                    Wrappers.<OmsRefundInfo>lambdaQuery()
                            .eq(OmsRefundInfo::getOrderReturnId, sibling.getId())
                            .last("LIMIT 1"));
            if (existing == null
                    || existing.getRefund() == null
                    || !Integer.valueOf(OmsConstants.REFUND_STATUS_SUCCESS).equals(existing.getRefundStatus())) {
                continue;
            }
            alreadyRefunded = alreadyRefunded.add(existing.getRefund());
        }
        if (alreadyRefunded.add(normalizedRefund).compareTo(normalizedPay) > 0) {
            throw new BizException("累计退款金额不能超过订单实付金额");
        }
    }

    private RefundOutcome processChannelRefund(OmsOrder order, BigDecimal refundAmount, String refundSn) {
        int refundChannel = resolveRefundChannel(order);
        if (order == null || refundAmount == null || refundAmount.signum() <= 0) {
            return RefundOutcome.recordOnly(refundChannel, "退款金额为 0，仅记录");
        }

        OmsPaymentInfo payment = omsPaymentInfoMapper.selectOne(
                Wrappers.<OmsPaymentInfo>lambdaQuery()
                        .eq(OmsPaymentInfo::getOrderId, order.getId())
                        .orderByDesc(OmsPaymentInfo::getId)
                        .last("LIMIT 1"));
        String tradeNo = payment != null ? payment.getAlipayTradeNo() : null;
        if (!StringUtils.hasText(tradeNo) || isMockTradeNo(tradeNo)) {
            return RefundOutcome.recordOnly(refundChannel, "Mock/无支付流水，系统记录退款（未调用支付渠道）");
        }

        int payType = resolvePayType(order);
        if (Integer.valueOf(PortalConstants.PAY_TYPE_WECHAT).equals(payType)) {
            return refundViaWechat(order, refundAmount, refundSn, tradeNo);
        }
        if (Integer.valueOf(PortalConstants.PAY_TYPE_ALIPAY).equals(payType)) {
            return refundViaAlipay(order, refundAmount, refundSn, tradeNo);
        }
        return RefundOutcome.recordOnly(refundChannel, "未知支付方式，系统记录退款（未调用支付渠道）");
    }

    private RefundOutcome refundViaAlipay(
            OmsOrder order, BigDecimal refundAmount, String refundSn, String tradeNo) {
        if (!alipayPayService.isAvailable()) {
            throw new BizException("支付宝未配置，无法对支付宝订单原路退款");
        }
        AlipayRefundResult alipayResult = alipayPayService.refund(
                order.getOrderSn(), tradeNo, refundAmount, refundSn);
        String note = "支付宝原路退款成功"
                + "，tradeNo=" + alipayResult.getTradeNo()
                + "，fundChange=" + alipayResult.getFundChange();
        return new RefundOutcome(
                OmsConstants.REFUND_STATUS_SUCCESS,
                PortalConstants.PAY_TYPE_ALIPAY,
                StringUtils.hasText(alipayResult.getRawResponse()) ? alipayResult.getRawResponse() : note);
    }

    private RefundOutcome refundViaWechat(
            OmsOrder order, BigDecimal refundAmount, String refundSn, String tradeNo) {
        if (!wxPayService.isConfigured()) {
            throw new BizException("微信支付未配置，无法对微信订单原路退款");
        }
        BigDecimal orderPayAmount = resolveOrderPayAmount(order);
        WxRefundResult wxResult = wxPayService.refund(
                order.getOrderSn(), tradeNo, refundAmount, orderPayAmount, refundSn);
        String note = "微信原路退款受理"
                + "，refundId=" + wxResult.getRefundId()
                + "，status=" + wxResult.getStatus();
        int refundStatus = "SUCCESS".equalsIgnoreCase(wxResult.getStatus())
                ? OmsConstants.REFUND_STATUS_SUCCESS
                : OmsConstants.REFUND_STATUS_PROCESSING;
        return new RefundOutcome(
                refundStatus,
                PortalConstants.PAY_TYPE_WECHAT,
                StringUtils.hasText(wxResult.getRawResponse()) ? wxResult.getRawResponse() : note);
    }

    private BigDecimal resolveOrderPayAmount(OmsOrder order) {
        OmsPaymentInfo payment = omsPaymentInfoMapper.selectOne(
                Wrappers.<OmsPaymentInfo>lambdaQuery()
                        .eq(OmsPaymentInfo::getOrderId, order.getId())
                        .orderByDesc(OmsPaymentInfo::getId)
                        .last("LIMIT 1"));
        if (payment != null && payment.getTotalAmount() != null && payment.getTotalAmount().signum() > 0) {
            return payment.getTotalAmount();
        }
        return order.getPayAmount();
    }

    private int resolveRefundChannel(OmsOrder order) {
        return order != null && order.getPayType() != null ? order.getPayType() : PortalConstants.PAY_TYPE_ALIPAY;
    }

    private int resolvePayType(OmsOrder order) {
        return order.getPayType() == null ? PortalConstants.PAY_TYPE_ALIPAY : order.getPayType();
    }

    private boolean isMockTradeNo(String tradeNo) {
        String upper = tradeNo.trim().toUpperCase();
        return upper.startsWith("MOCK") || upper.startsWith("WXMOCK");
    }

    private void notifyRefundFailure(
            Integer previousStatus,
            int newStatus,
            Long refundId,
            String refundSn,
            OmsOrder order) {
        String orderSn = order != null ? order.getOrderSn() : null;
        omsRefundAlertService.onStatusChanged(previousStatus, newStatus, refundId, refundSn, orderSn);
    }

    private record RefundOutcome(int refundStatus, int refundChannel, String content) {

        private static RefundOutcome recordOnly(int refundChannel, String content) {
            return new RefundOutcome(OmsConstants.REFUND_STATUS_SUCCESS, refundChannel, content);
        }
    }
}
