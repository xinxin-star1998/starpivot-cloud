package cn.org.starpivot.mall.pay.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderItem;
import cn.org.starpivot.mall.oms.entity.OmsOrderOperateHistory;
import cn.org.starpivot.mall.oms.entity.OmsPaymentInfo;
import cn.org.starpivot.mall.oms.mapper.OmsOrderItemMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderOperateHistoryMapper;
import cn.org.starpivot.mall.oms.mapper.OmsPaymentInfoMapper;
import cn.org.starpivot.mall.pay.service.PortalOrderPayService;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.service.PortalCouponService;
import cn.org.starpivot.mall.portal.service.PortalStockLockService;
import cn.org.starpivot.mall.wms.domain.dto.WmsStockDeductionLine;
import cn.org.starpivot.mall.wms.service.WmsWareOrderTaskService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PortalOrderPayServiceImpl implements PortalOrderPayService {

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderItemMapper omsOrderItemMapper;
    private final OmsPaymentInfoMapper omsPaymentInfoMapper;
    private final OmsOrderOperateHistoryMapper omsOrderOperateHistoryMapper;
    private final PortalStockLockService portalStockLockService;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final WmsWareOrderTaskService wmsWareOrderTaskService;
    private final PortalCouponService portalCouponService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmPaid(OmsOrder order, String alipayTradeNo, String paymentStatus, String callbackContent) {
        if (order == null || order.getId() == null) {
            throw new BizException("订单不存在");
        }
        OmsOrder fresh = omsOrderMapper.selectById(order.getId());
        if (fresh == null) {
            throw new BizException("订单不存在");
        }
        if (Integer.valueOf(PortalConstants.ORDER_STATUS_WAIT_DELIVER).equals(fresh.getStatus())
                || Integer.valueOf(PortalConstants.ORDER_STATUS_DELIVERED).equals(fresh.getStatus())
                || Integer.valueOf(PortalConstants.ORDER_STATUS_COMPLETED).equals(fresh.getStatus())) {
            return false;
        }
        if (hasSuccessfulPayment(fresh.getId())) {
            return false;
        }
        if (Integer.valueOf(PortalConstants.ORDER_STATUS_UNPAID).equals(fresh.getStatus())) {
            return fulfillPaidOrder(fresh, alipayTradeNo, paymentStatus, callbackContent, "支付成功");
        }
        if (Integer.valueOf(PortalConstants.ORDER_STATUS_CLOSED).equals(fresh.getStatus())) {
            return fulfillPaidOrder(fresh, alipayTradeNo, paymentStatus, callbackContent, "超时关单后支付恢复");
        }
        return false;
    }

    private boolean fulfillPaidOrder(
            OmsOrder order,
            String alipayTradeNo,
            String paymentStatus,
            String callbackContent,
            String historyNote) {
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(PortalConstants.ORDER_STATUS_WAIT_DELIVER);
        order.setPaymentTime(now);
        order.setModifyTime(now);
        omsOrderMapper.updateById(order);

        OmsPaymentInfo payment = new OmsPaymentInfo();
        payment.setOrderId(order.getId());
        payment.setOrderSn(order.getOrderSn());
        payment.setTotalAmount(order.getPayAmount());
        payment.setSubject("商城订单-" + order.getOrderSn());
        payment.setAlipayTradeNo(alipayTradeNo);
        payment.setPaymentStatus(StringUtils.hasText(paymentStatus) ? paymentStatus : PortalConstants.PAYMENT_STATUS_SUCCESS);
        payment.setCreateTime(now);
        payment.setConfirmTime(now);
        if (StringUtils.hasText(callbackContent)) {
            payment.setCallbackContent(callbackContent);
            payment.setCallbackTime(now);
        }
        omsPaymentInfoMapper.insert(payment);

        List<WmsStockDeductionLine> deductions = portalStockLockService.confirmForOrder(order.getOrderSn());
        wmsWareOrderTaskService.createFinishedRecordForPaidOrder(order.getId(), deductions);
        incrementSkuSaleCount(order.getId());
        portalCouponService.confirmUsed(order.getId());
        saveOperateHistory(order.getId(), PortalConstants.ORDER_STATUS_WAIT_DELIVER, order.getMemberUsername(), historyNote);
        return true;
    }

    private boolean hasSuccessfulPayment(Long orderId) {
        return omsPaymentInfoMapper.selectCount(
                Wrappers.<OmsPaymentInfo>lambdaQuery().eq(OmsPaymentInfo::getOrderId, orderId)) > 0;
    }

    private void incrementSkuSaleCount(Long orderId) {
        List<OmsOrderItem> items = omsOrderItemMapper.selectList(
                Wrappers.<OmsOrderItem>lambdaQuery().eq(OmsOrderItem::getOrderId, orderId));
        Map<Long, Integer> qtyBySku = new LinkedHashMap<>();
        for (OmsOrderItem item : items) {
            if (item.getSkuId() == null) {
                continue;
            }
            int qty = item.getSkuQuantity() == null || item.getSkuQuantity() <= 0 ? 0 : item.getSkuQuantity();
            if (qty <= 0) {
                continue;
            }
            qtyBySku.merge(item.getSkuId(), qty, Integer::sum);
        }
        qtyBySku.forEach((skuId, qty) -> pmsSkuInfoMapper.incrementSaleCount(skuId, qty));
    }

    private void saveOperateHistory(Long orderId, Integer status, String operator, String note) {
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(orderId);
        history.setOperateMan(operator);
        history.setCreateTime(LocalDateTime.now());
        history.setOrderStatus(status);
        history.setNote(note);
        omsOrderOperateHistoryMapper.insert(history);
    }
}
