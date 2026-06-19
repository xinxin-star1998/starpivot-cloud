package cn.org.starpivot.common.constants;

/**
 * 定时任务相关常量
 */
public final class JobConstants {

    private JobConstants() {
    }

    public static final String SUCCESS = "0";
    public static final String FAIL = "1";

    public static final String[] JOB_WHITELIST_STR = {"cn.org.starpivot.job.task"};
    public static final String[] JOB_ERROR_STR = {
            "java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "cn.org.starpivot.common.config", "cn.org.starpivot.generator"
    };
}
