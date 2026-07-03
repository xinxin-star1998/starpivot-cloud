package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.mall.oms.OmsConstants;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.entity.OmsPaymentInfo;
import cn.org.starpivot.mall.oms.entity.OmsRefundInfo;
import cn.org.starpivot.mall.oms.mapper.OmsPaymentInfoMapper;
import cn.org.starpivot.mall.oms.mapper.OmsRefundInfoMapper;
import cn.org.starpivot.mall.oms.service.OmsRefundProcessService;
import cn.org.starpivot.mall.pay.service.AlipayPayService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmsRefundProcessServiceImpl implements OmsRefundProcessService {

    private final OmsRefundInfoMapper omsRefundInfoMapper;
    private final OmsPaymentInfoMapper omsPaymentInfoMapper;
    private final AlipayPayService alipayPayService;

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

        String refundSn = "RF" + System.currentTimeMillis();
        String refundNote = processChannelRefund(order, refundAmount, refundSn);

        OmsRefundInfo refund = new OmsRefundInfo();
        refund.setOrderReturnId(apply.getId());
        refund.setRefund(refundAmount);
        refund.setRefundSn(refundSn);
        refund.setRefundStatus(OmsConstants.REFUND_STATUS_SUCCESS);
        refund.setRefundChannel(order != null && order.getPayType() != null ? order.getPayType() : 1);
        refund.setRefundContent(refundNote);
        omsRefundInfoMapper.insert(refund);
        log.info("Created refund record for return apply id={}, amount={}", apply.getId(), refundAmount);
    }

    private String processChannelRefund(OmsOrder order, BigDecimal refundAmount, String refundSn) {
        if (order == null || refundAmount == null || refundAmount.signum() <= 0) {
            return "退款金额为 0，仅记录";
        }
        OmsPaymentInfo payment = omsPaymentInfoMapper.selectOne(
                Wrappers.<OmsPaymentInfo>lambdaQuery()
                        .eq(OmsPaymentInfo::getOrderId, order.getId())
                        .orderByDesc(OmsPaymentInfo::getId)
                        .last("LIMIT 1"));
        String tradeNo = payment != null ? payment.getAlipayTradeNo() : null;
        if (!StringUtils.hasText(tradeNo) || tradeNo.startsWith("MOCK")) {
            return "Mock/无支付流水，系统记录退款（未调用支付渠道）";
        }
        if (!alipayPayService.isAvailable()) {
            return "支付宝未配置，系统记录退款（未调用支付渠道）";
        }
        String alipayRefundTradeNo = alipayPayService.refund(order.getOrderSn(), tradeNo, refundAmount, refundSn);
        return "支付宝原路退款成功，tradeNo=" + alipayRefundTradeNo;
    }
}
