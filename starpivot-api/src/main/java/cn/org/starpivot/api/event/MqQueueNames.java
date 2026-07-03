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

    private MqQueueNames() {
    }
}
