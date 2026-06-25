package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.mall.oms.OmsConstants;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderOperateHistory;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderOperateHistoryMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderReturnApplyMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderStockService;
import cn.org.starpivot.mall.oms.service.OmsRefundProcessService;
import cn.org.starpivot.mall.portal.PortalConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OmsOrderReturnFulfillmentService {

    private final OmsOrderReturnApplyMapper omsOrderReturnApplyMapper;
    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderOperateHistoryMapper omsOrderOperateHistoryMapper;
    private final OmsOrderStockService omsOrderStockService;
    private final OmsRefundProcessService omsRefundProcessService;

    @Transactional(rollbackFor = Exception.class)
    public void completeReturn(Long applyId) {
        OmsOrderReturnApply apply = omsOrderReturnApplyMapper.selectById(applyId);
        if (apply == null) {
            throw new BizException("退货申请不存在");
        }
        if (!Integer.valueOf(OmsConstants.RETURN_STATUS_RETURNING).equals(apply.getStatus())) {
            throw new BizException("仅退货中申请可完成退款");
        }
        OmsOrder order = omsOrderMapper.selectById(apply.getOrderId());
        if (order == null) {
            throw new BizException("关联订单不存在");
        }

        int qty = apply.getSkuCount() == null ? 0 : apply.getSkuCount();
        if (apply.getSkuId() != null && qty > 0) {
            omsOrderStockService.inboundForReturn(order.getId(), apply.getSkuId(), qty);
        }

        BigDecimal refundAmount = apply.getReturnAmount() != null ? apply.getReturnAmount() : order.getPayAmount();
        omsRefundProcessService.createRefundForReturn(order, apply, refundAmount);

        apply.setStatus(OmsConstants.RETURN_STATUS_COMPLETED);
        apply.setReceiveTime(LocalDateTime.now());
        if (!StringUtils.hasText(apply.getReceiveMan())) {
            apply.setReceiveMan(SecurityContextUtils.getUsername());
        }
        omsOrderReturnApplyMapper.updateById(apply);

        order.setStatus(PortalConstants.ORDER_STATUS_CLOSED);
        order.setModifyTime(LocalDateTime.now());
        omsOrderMapper.updateById(order);

        saveOperateHistory(order.getId(), PortalConstants.ORDER_STATUS_CLOSED, "退货完成，订单关闭");
    }

    private void saveOperateHistory(Long orderId, Integer status, String note) {
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(orderId);
        history.setOperateMan(SecurityContextUtils.getUsername());
        history.setCreateTime(LocalDateTime.now());
        history.setOrderStatus(status);
        history.setNote(note);
        omsOrderOperateHistoryMapper.insert(history);
    }
}
