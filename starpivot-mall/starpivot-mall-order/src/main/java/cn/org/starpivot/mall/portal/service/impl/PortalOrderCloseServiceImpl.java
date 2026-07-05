package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.mall.common.PromotionFeignSupport;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import cn.org.starpivot.mall.oms.entity.OmsOrderOperateHistory;
import cn.org.starpivot.mall.oms.mapper.OmsOrderMapper;
import cn.org.starpivot.mall.oms.mapper.OmsOrderOperateHistoryMapper;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.service.PortalMemberIntegrationService;
import cn.org.starpivot.mall.portal.service.PortalOrderCloseService;
import cn.org.starpivot.mall.portal.service.PortalStockLockService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalOrderCloseServiceImpl implements PortalOrderCloseService {

    private final OmsOrderMapper omsOrderMapper;
    private final OmsOrderOperateHistoryMapper omsOrderOperateHistoryMapper;
    private final PortalStockLockService portalStockLockService;
    private final PromotionFeignSupport promotionFeignSupport;
    private final PortalMemberIntegrationService portalMemberIntegrationService;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeUnpaidOrderAndReleaseStock(String orderSn) {
        if (!StringUtils.hasText(orderSn)) {
            return false;
        }
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(PortalConstants.stockLockConfirmedKey(orderSn)))) {
            return false;
        }
        OmsOrder order = omsOrderMapper.selectOne(
                Wrappers.<OmsOrder>lambdaQuery().eq(OmsOrder::getOrderSn, orderSn).last("LIMIT 1"));
        if (order == null
                || !Integer.valueOf(PortalConstants.ORDER_STATUS_UNPAID).equals(order.getStatus())) {
            return false;
        }
        portalStockLockService.releaseForOrder(orderSn);
        order.setStatus(PortalConstants.ORDER_STATUS_CLOSED);
        order.setModifyTime(LocalDateTime.now());
        omsOrderMapper.updateById(order);
        promotionFeignSupport.releaseCoupon(order.getId());
        promotionFeignSupport.releaseSeckillByOrderSn(orderSn);
        portalMemberIntegrationService.restoreForOrder(order);
        saveOperateHistory(order.getId(), PortalConstants.ORDER_STATUS_CLOSED, "system", "超时未支付自动关闭");
        log.info("Closed unpaid order: {}", orderSn);
        return true;
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
