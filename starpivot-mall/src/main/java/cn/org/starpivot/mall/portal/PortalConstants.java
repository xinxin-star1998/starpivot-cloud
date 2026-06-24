package cn.org.starpivot.mall.portal;

/**
 * C 端商城 portal 常量。
 */
public final class PortalConstants {

    /** JWT / Spring Security 中的会员角色标识 */
    public static final String MEMBER_ROLE = "MEMBER";

    /** Redis 购物车 key 前缀：mall:portal:cart:{memberId} */
    public static final String CART_KEY_PREFIX = "mall:portal:cart:";

    /** 购物车默认过期天数 */
    public static final int CART_TTL_DAYS = 30;

    /** 订单号前缀 */
    public static final String ORDER_SN_PREFIX = "SP";

    /** 订单状态：待付款 */
    public static final int ORDER_STATUS_UNPAID = 0;

    /** 订单状态：待发货 */
    public static final int ORDER_STATUS_WAIT_DELIVER = 1;

    /** 订单状态：已关闭 */
    public static final int ORDER_STATUS_CLOSED = 4;

    /** Mock 支付流水状态 */
    public static final String PAYMENT_STATUS_SUCCESS = "TRADE_SUCCESS";

    /** 商品上架状态 */
    public static final int PUBLISH_STATUS_ON = 1;

    /** 首页广告启用状态 */
    public static final int ADV_STATUS_ON = 1;

    /** Redis：SKU 预扣库存计数前缀 mall:portal:stock:reserved:{skuId} */
    public static final String STOCK_RESERVED_PREFIX = "mall:portal:stock:reserved:";

    /** Redis：订单库存锁定明细 mall:portal:stock:lock:order:{orderSn} */
    public static final String STOCK_LOCK_ORDER_PREFIX = "mall:portal:stock:lock:order:";

    /** Redis：待付款订单锁过期调度 ZSET */
    public static final String STOCK_LOCK_EXPIRY_ZSET = "mall:portal:stock:lock:expiry";

    /** Redis：已支付订单锁标记（不再自动释放） */
    public static final String STOCK_LOCK_CONFIRMED_PREFIX = "mall:portal:stock:lock:confirmed:";

    /** 待付款库存锁默认超时（分钟），无订单设置时兜底 */
    public static final int DEFAULT_UNPAID_LOCK_MINUTES = 30;

    private PortalConstants() {
    }

    public static String stockReservedKey(Long skuId) {
        return STOCK_RESERVED_PREFIX + skuId;
    }

    public static String stockLockOrderKey(String orderSn) {
        return STOCK_LOCK_ORDER_PREFIX + orderSn;
    }

    public static String stockLockConfirmedKey(String orderSn) {
        return STOCK_LOCK_CONFIRMED_PREFIX + orderSn;
    }

    public static String cartKey(Long memberId) {
        return CART_KEY_PREFIX + memberId;
    }
}
