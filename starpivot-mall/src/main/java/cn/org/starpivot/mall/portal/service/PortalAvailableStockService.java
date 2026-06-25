package cn.org.starpivot.mall.portal.service;

import java.util.Collection;
import java.util.Map;

/**
 * 可售库存查询服务（WMS 可用量 − Redis 预扣量）。
 * <p>
 * 独立于 {@link PortalStockLockService}，供购物车、商品展示等只读场景使用，避免与锁库存服务形成循环依赖。
 * </p>
 */
public interface PortalAvailableStockService {

    /**
     * 查询 WMS 侧可用库存（未扣除 Redis 预扣）。
     */
    Map<Long, Integer> getWmsAvailableMap(Collection<Long> skuIds);

    /**
     * 查询可售库存（WMS 可用量 − Redis 预扣量）。
     */
    Map<Long, Integer> getAvailableStockMap(Collection<Long> skuIds);
}
