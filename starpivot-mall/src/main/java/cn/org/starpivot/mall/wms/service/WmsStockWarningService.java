package cn.org.starpivot.mall.wms.service;

/**
 * 库存预警：可售库存低于阈值时自动生成采购需求。
 */
public interface WmsStockWarningService {

    /**
     * 检查 SKU 总可售库存，不足时创建采购需求（幂等：已有新建状态需求则跳过）。
     *
     * @param skuId  SKU ID
     * @param wareId 优先入库仓库（可为空）
     */
    void checkAndCreatePurchaseDemand(Long skuId, Long wareId);
}
