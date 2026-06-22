package cn.org.starpivot.api.event;

/**
 * RabbitMQ Queue 名称常量。
 */
public final class MqQueueNames {

    public static final String SYSTEM_LOGIN_LOG = "starpivot.system.login-log";
    public static final String SYSTEM_OPER_LOG = "starpivot.system.oper-log";
    public static final String SYSTEM_JOB_HANDLER = "starpivot.system.job-handler";

    private MqQueueNames() {
    }
}
