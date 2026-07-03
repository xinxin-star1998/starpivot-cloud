package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.sms.entity.SmsSeckillSkuRelation;

import java.util.List;

/**
 * 秒杀 Redis 库存与限购服务。
 */
public interface PortalSeckillStockService {

    /** 将 DB 秒杀库存预热到 Redis（SETNX，不覆盖已有值） */
    void warmup(Long promotionId, List<SmsSeckillSkuRelation> relations);

    /** 查询 Redis 剩余秒杀库存 */
    int getRemainStock(Long promotionId, Long sessionId, Long skuId);

    /**
     * 预扣秒杀库存。
     *
     * @return true 成功
     */
    boolean reserve(Long promotionId, Long sessionId, Long skuId, Long memberId, int quantity, int seckillLimit);

    /** 归还秒杀库存（取消/超时关单） */
    void release(Long promotionId, Long sessionId, Long skuId, Long memberId, int quantity, int seckillLimit);

    /** 绑定订单与秒杀扣减记录，便于关单回滚 */
    void bindOrder(String orderSn, Long promotionId, Long sessionId, Long skuId, Long memberId, int quantity, int seckillLimit);

    /** 按订单号归还秒杀库存（幂等） */
    void releaseByOrderSn(String orderSn);
}
