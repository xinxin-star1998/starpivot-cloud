package cn.org.starpivot.mall.oms;

/**
 * OMS 业务常量。
 */
public final class OmsConstants {

    public static final int ORDER_STATUS_DELIVERED = 2;
    public static final int ORDER_STATUS_COMPLETED = 3;
    public static final int ORDER_STATUS_INVALID = 5;

    /** 退货：0待处理 1退货中 2已完成 3已拒绝 */
    public static final int RETURN_STATUS_PENDING = 0;
    public static final int RETURN_STATUS_RETURNING = 1;
    public static final int RETURN_STATUS_COMPLETED = 2;
    public static final int RETURN_STATUS_REJECTED = 3;

    /** 退款：0待退款 1退款中 2已退款 3失败 */
    public static final int REFUND_STATUS_PENDING = 0;
    public static final int REFUND_STATUS_PROCESSING = 1;
    public static final int REFUND_STATUS_SUCCESS = 2;
    public static final int REFUND_STATUS_FAILED = 3;

    private OmsConstants() {
    }
}
