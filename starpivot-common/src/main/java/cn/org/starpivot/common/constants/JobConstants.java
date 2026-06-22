package cn.org.starpivot.common.constants;

/**
 * 定时任务（Quartz）相关常量。
 * <p>
 * 定义任务执行状态码及调用目标白名单/黑名单，供 job 模块校验 {@code invokeTarget} 安全性。
 * </p>
 */
public final class JobConstants {

    private JobConstants() {
    }

    /** 任务执行成功状态码 */
    public static final String SUCCESS = "0";
    /** 任务执行失败状态码 */
    public static final String FAIL = "1";

    /** 允许反射调用的 Bean 包名前缀白名单 */
    public static final String[] JOB_WHITELIST_STR = {"cn.org.starpivot.job.task"};
    /** 禁止出现在调用目标中的危险类名片段（防 RCE） */
    public static final String[] JOB_ERROR_STR = {
            "java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "cn.org.starpivot.common.config", "cn.org.starpivot.generator"
    };
}
