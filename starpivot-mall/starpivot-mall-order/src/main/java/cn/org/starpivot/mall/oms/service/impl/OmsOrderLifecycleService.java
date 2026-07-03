package cn.org.starpivot.mall.oms.service.impl;

import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderOperateHistory;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderOperateHistoryMapper;
import cn.org.starpivot.mall.oms.service.OmsOrderSettingService;
import cn.org.starpivot.mall.portal.PortalConstants;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmsOrderLifecycleService {

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderOperateHistoryMapper omsOrderOperateHistoryMapper;
    private final OmsOrderSettingService omsOrderSettingService;

    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long orderId, String operator) {
        OmsOrder order = requireOrder(orderId);
        if (!Integer.valueOf(PortalConstants.ORDER_STATUS_DELIVERED).equals(order.getStatus())) {
            throw new cn.org.starpivot.common.exception.BizException("仅已发货订单可确认收货");
        }
        LocalDateTime now = LocalDateTime.now();
        order.setStatus(PortalConstants.ORDER_STATUS_COMPLETED);
        order.setReceiveTime(now);
        order.setConfirmStatus(1);
        order.setModifyTime(now);
        omsOrderMapper.updateById(order);
        saveHistory(orderId, PortalConstants.ORDER_STATUS_COMPLETED, operator, "确认收货");
    }

    @Transactional(rollbackFor = Exception.class)
    public int autoConfirmDeliveredOrders() {
        Integer days = omsOrderSettingService.getSetting().getConfirmOvertime();
        if (days == null || days <= 0) {
            return 0;
        }
        LocalDateTime deadline = LocalDateTime.now().minusDays(days);
        List<OmsOrder> orders = omsOrderMapper.selectList(
                Wrappers.<OmsOrder>lambdaQuery()
                        .eq(OmsOrder::getStatus, PortalConstants.ORDER_STATUS_DELIVERED)
                        .eq(OmsOrder::getDeleteStatus, 0)
                        .isNotNull(OmsOrder::getDeliveryTime)
                        .le(OmsOrder::getDeliveryTime, deadline));
        int count = 0;
        for (OmsOrder order : orders) {
            try {
                confirmReceive(order.getId(), "system");
                count++;
            } catch (Exception ex) {
                log.warn("Auto confirm receive failed for orderId={}", order.getId(), ex);
            }
        }
        return count;
    }

    @Transactional(rollbackFor = Exception.class)
    public int autoFinishCompletedOrders() {
        Integer days = omsOrderSettingService.getSetting().getFinishOvertime();
        if (days == null || days <= 0) {
            return 0;
        }
        LocalDateTime deadline = LocalDateTime.now().minusDays(days);
        List<OmsOrder> orders = omsOrderMapper.selectList(
                Wrappers.<OmsOrder>lambdaQuery()
                        .eq(OmsOrder::getStatus, PortalConstants.ORDER_STATUS_COMPLETED)
                        .eq(OmsOrder::getDeleteStatus, 0)
                        .isNotNull(OmsOrder::getReceiveTime)
                        .le(OmsOrder::getReceiveTime, deadline)
                        .isNull(OmsOrder::getCommentTime));
        int count = 0;
        for (OmsOrder order : orders) {
            order.setCommentTime(LocalDateTime.now());
            order.setModifyTime(LocalDateTime.now());
            omsOrderMapper.updateById(order);
            saveHistory(order.getId(), PortalConstants.ORDER_STATUS_COMPLETED, "system", "超时自动完成评价期");
            count++;
        }
        return count;
    }

    private OmsOrder requireOrder(Long orderId) {
        OmsOrder order = omsOrderMapper.selectById(orderId);
        if (order == null || Integer.valueOf(1).equals(order.getDeleteStatus())) {
            throw new cn.org.starpivot.common.exception.BizException("订单不存在");
        }
        return order;
    }

    private void saveHistory(Long orderId, Integer status, String operator, String note) {
        OmsOrderOperateHistory history = new OmsOrderOperateHistory();
        history.setOrderId(orderId);
        history.setOperateMan(operator);
        history.setCreateTime(LocalDateTime.now());
        history.setOrderStatus(status);
        history.setNote(note);
        omsOrderOperateHistoryMapper.insert(history);
    }
}
