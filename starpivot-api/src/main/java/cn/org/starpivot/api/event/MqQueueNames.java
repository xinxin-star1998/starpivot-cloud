package cn.org.starpivot.api.event;

/**
 * RabbitMQ Queue 名称常量。
 */
public final class MqQueueNames {

    public static final String SYSTEM_LOGIN_LOG = "starpivot.system.login-log";
    public static final String SYSTEM_OPER_LOG = "starpivot.system.oper-log";
    public static final String SYSTEM_JOB_HANDLER = "starpivot.system.job-handler";

    /** 商城服务 — 审批完结消费队列 */
    public static final String MALL_APPROVAL_FINISHED = "starpivot.mall.approval-finished";

    private MqQueueNames() {
    }
}
