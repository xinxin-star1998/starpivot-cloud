package cn.org.starpivot.api.system.constant;

/**
 * 全平台站内消息类型与状态常量。
 */
public final class MessageConstants {

    private MessageConstants() {
    }

    /** 审批待办分配 */
    public static final String MSG_TYPE_APPROVAL_TASK = "APPROVAL_TASK";
    /** 审批完结通知发起人 */
    public static final String MSG_TYPE_APPROVAL_RESULT = "APPROVAL_RESULT";
    /** 系统通知 */
    public static final String MSG_TYPE_SYSTEM = "SYSTEM";
    /** 订单/商城业务 */
    public static final String MSG_TYPE_ORDER = "ORDER";
    /** 退款失败告警 */
    public static final String MSG_TYPE_REFUND_ALERT = "REFUND_ALERT";
    /** 管理员群发 */
    public static final String MSG_TYPE_BROADCAST = "BROADCAST";

    public static final String BIZ_MODULE_APPROVAL = "approval";
    public static final String BIZ_MODULE_MALL = "mall";
    public static final String BIZ_MODULE_SYSTEM = "system";

    /** 群发目标：全部用户 */
    public static final String TARGET_ALL = "ALL";
    /** 群发目标：按角色 */
    public static final String TARGET_ROLE = "ROLE";
    /** 群发目标：指定用户 */
    public static final String TARGET_USER = "USER";

    /** Redis 频道：站内消息实时推送 */
    public static final String REDIS_CHANNEL_MESSAGE_PUSH = "starpivot:message:push";

    public static final String READ_FLAG_UNREAD = "0";
    public static final String READ_FLAG_READ = "1";
}
