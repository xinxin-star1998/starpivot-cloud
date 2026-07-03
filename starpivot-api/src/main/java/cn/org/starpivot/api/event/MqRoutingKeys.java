package cn.org.starpivot.api.event;

/**
 * RabbitMQ Routing Key 常量。
 */
public final class MqRoutingKeys {

    public static final String AUDIT_LOGIN_LOG_CREATED = "audit.login-log.created";
    public static final String AUDIT_OPER_LOG_CREATED = "audit.oper-log.created";
    public static final String JOB_OPER_LOG_CLEAN = "job.oper-log.clean";

    /** 审批实例完结（通过/驳回/撤回）— 非商城或未指定 bizType 时使用 */
    public static final String APPROVAL_INSTANCE_FINISHED = "approval.instance.finished";

    /** 商城审批完结 routing key 前缀：{@code approval.instance.finished.mall.{bizType}} */
    public static final String APPROVAL_INSTANCE_FINISHED_MALL_PREFIX =
            APPROVAL_INSTANCE_FINISHED + ".mall.";

    /**
     * 商城审批完结 routing key，按 bizType 路由到对应微服务队列。
     *
     * @param bizType 业务类型（purchase / spu / return / coupon）
     */
    public static String mallApprovalFinished(String bizType) {
        if (bizType == null || bizType.isBlank()) {
            return APPROVAL_INSTANCE_FINISHED;
        }
        return APPROVAL_INSTANCE_FINISHED_MALL_PREFIX + bizType;
    }

    private MqRoutingKeys() {
    }
}
