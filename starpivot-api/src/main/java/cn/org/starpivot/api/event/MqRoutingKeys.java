package cn.org.starpivot.api.event;

/**
 * RabbitMQ Routing Key 常量。
 */
public final class MqRoutingKeys {

    public static final String AUDIT_LOGIN_LOG_CREATED = "audit.login-log.created";
    public static final String AUDIT_OPER_LOG_CREATED = "audit.oper-log.created";
    public static final String JOB_OPER_LOG_CLEAN = "job.oper-log.clean";

    private MqRoutingKeys() {
    }
}
