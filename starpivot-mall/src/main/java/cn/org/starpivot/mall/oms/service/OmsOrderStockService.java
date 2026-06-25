package cn.org.starpivot.mall.oms.service;

/**
 * 订单与 WMS 库存联动（回库、退货入库）。
 */
public interface OmsOrderStockService {

    /** 按出库工作单明细回滚库存与销量（关单补偿） */
    void restoreStockForOrder(Long orderId);

    /** 退货入库：优先回到原出库仓库 */
    void inboundForReturn(Long orderId, Long skuId, int quantity);

    /** 查找 SKU 在原订单中的出库仓库（来自已完成工作单） */
    Long findOutboundWareId(Long orderId, Long skuId);
}
