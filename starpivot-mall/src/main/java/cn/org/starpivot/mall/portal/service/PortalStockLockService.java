package cn.org.starpivot.mall.portal.service;

import java.util.Collection;
import java.util.Map;

/**
 * Stocklockservice服务接口。
 * <p>
 * 封装Stocklock相关业务逻辑。
 * </p>
 */

public interface PortalStockLockService {

    /**
     * 查询可售库存（WMS 可用量 − Redis 预扣量）。
     */
    Map<Long, Integer> getAvailableStockMap(Collection<Long> skuIds);

    /**
     * 为待付款订单预扣库存；库存不足时抛出 {@link cn.org.starpivot.common.exception.BizException}。
     */
    void lockForOrder(String orderSn, Map<Long, Integer> skuQuantities);

    /**
     * 释放订单预扣（取消待付款、超时关单）。
     */
    void releaseForOrder(String orderSn);

    /**
     * 支付成功后确认锁定，移出超时调度（预扣保留至发货/关单）。
     */
    void confirmForOrder(String orderSn);

    /**
     * 扫描并释放已超时的待付款订单库存锁。
     */
    void releaseExpiredLocks();
}
