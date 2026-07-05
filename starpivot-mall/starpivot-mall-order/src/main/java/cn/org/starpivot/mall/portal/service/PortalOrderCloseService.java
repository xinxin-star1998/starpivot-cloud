package cn.org.starpivot.mall.portal.service;

/**
 * 待付款订单超时关单（释放库存锁、回滚优惠券/积分）。
 */
public interface PortalOrderCloseService {

    /**
     * 若订单仍为待付款则释放 Redis 预扣并关单。
     *
     * @return 是否执行了关单
     */
    boolean closeUnpaidOrderAndReleaseStock(String orderSn);
}
