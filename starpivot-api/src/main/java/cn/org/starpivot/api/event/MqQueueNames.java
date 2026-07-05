package cn.org.starpivot.api.event;

/**
 * RabbitMQ Queue 名称常量。
 */
public final class MqQueueNames {

    public static final String SYSTEM_LOGIN_LOG = "starpivot.system.login-log";
    public static final String SYSTEM_OPER_LOG = "starpivot.system.oper-log";
    public static final String SYSTEM_JOB_HANDLER = "starpivot.system.job-handler";

    /** 商城仓储 — 采购审批完结 */
    public static final String MALL_APPROVAL_FINISHED_PURCHASE = "starpivot.mall.approval-finished.purchase";
    /** 商城商品 — SPU 审批完结 */
    public static final String MALL_APPROVAL_FINISHED_SPU = "starpivot.mall.approval-finished.spu";
    /** 商城订单 — 退货审批完结 */
    public static final String MALL_APPROVAL_FINISHED_RETURN = "starpivot.mall.approval-finished.return";
    /** 商城营销 — 优惠券审批完结 */
    public static final String MALL_APPROVAL_FINISHED_COUPON = "starpivot.mall.approval-finished.coupon";
    /** 商城仓储 — 订单支付成功扣库存 */
    public static final String MALL_ORDER_PAID_WARE = "starpivot.mall.order-paid.ware";
    /** 商城营销 — 订单支付成功优惠券确认 */
    public static final String MALL_ORDER_PAID_PROMOTION = "starpivot.mall.order-paid.promotion";
    /** 商城会员 — 订单支付成功积分/成长值发放 */
    public static final String MALL_ORDER_PAID_MEMBER = "starpivot.mall.order-paid.member";
    /** 商城订单 — 待付款超时关单（延迟到期后消费） */
    public static final String MALL_ORDER_CLOSE = "starpivot.mall.order-close";
    /** 商城订单 — 待付款关单延迟暂存（per-message TTL + DLX） */
    public static final String MALL_ORDER_CLOSE_DELAY = "starpivot.mall.order-close.delay";

    private MqQueueNames() {
    }
}
