package cn.org.starpivot.approval.constant;

/**
 * 审批中台常量。
 */
public final class ApprovalConstants {

    public static final String TEMPLATE_DRAFT = "DRAFT";
    public static final String TEMPLATE_PUBLISHED = "PUBLISHED";
    public static final String TEMPLATE_DISABLED = "DISABLED";

    public static final String INSTANCE_RUNNING = "RUNNING";
    public static final String INSTANCE_APPROVED = "APPROVED";
    public static final String INSTANCE_REJECTED = "REJECTED";
    public static final String INSTANCE_WITHDRAWN = "WITHDRAWN";

    public static final String TASK_PENDING = "PENDING";
    public static final String TASK_DONE = "DONE";
    public static final String TASK_CANCELLED = "CANCELLED";

    public static final String ACTION_SUBMIT = "SUBMIT";
    public static final String ACTION_APPROVE = "APPROVE";
    public static final String ACTION_REJECT = "REJECT";
    public static final String ACTION_WITHDRAW = "WITHDRAW";
    public static final String ACTION_SKIP = "SKIP";
    public static final String ACTION_TIMEOUT = "TIMEOUT";

    public static final String TIMEOUT_ACTION_REJECT = "AUTO_REJECT";
    public static final String TIMEOUT_ACTION_APPROVE = "AUTO_APPROVE";

    public static final String APPROVE_MODE_ANY = "ANY";
    public static final String APPROVE_MODE_ALL = "ALL";

    public static final String ASSIGNEE_STARTER = "STARTER";
    public static final String ASSIGNEE_DEPT_LEADER = "DEPT_LEADER";
    public static final String ASSIGNEE_ROLE = "ROLE";
    public static final String ASSIGNEE_POST = "POST";
    public static final String ASSIGNEE_USER = "USER";

    public static final String ROUTE_DEFAULT = "default";

    private ApprovalConstants() {
    }
}
