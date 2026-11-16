package cn.org.starpivot.mall.sms.constants;

/**
 * 营销活动状态常量。
 * <p>
 * 覆盖活动上下线状态、拼团记录状态与砍价记录状态。
 * </p>
 */
public final class PromotionActivityStatus {

    /** 活动：下线 */
    public static final int OFFLINE = 0;

    /** 活动：上线 */
    public static final int ONLINE = 1;

    /** 拼团记录：待支付 */
    public static final int GROUP_PENDING_PAY = 0;

    /** 拼团记录：已支付，等待成团 */
    public static final int GROUP_WAIT_GROUP = 1;

    /** 拼团记录：已成团 */
    public static final int GROUP_COMPLETED = 2;

    /** 拼团记录：成团失败 */
    public static final int GROUP_FAILED = 3;

    /** 拼团记录：已取消（超时未支付） */
    public static final int GROUP_CANCELED = 4;

    /** 砍价记录：砍价中 */
    public static final int BARGAIN_ONGOING = 0;

    /** 砍价记录：已砍到底价 */
    public static final int BARGAIN_FLOOR = 1;

    /** 砍价记录：已下单 */
    public static final int BARGAIN_ORDERED = 2;

    /** 砍价记录：已失效 */
    public static final int BARGAIN_EXPIRED = 3;

    private PromotionActivityStatus() {
    }
}
