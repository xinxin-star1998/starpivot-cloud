package cn.org.starpivot.monitor.constants;

/**
 * 监控模块常量类
 *
 * @author xinxin
 * @since 2026-03-04
 */
public class MonitorConstants {

    /**
     * 监控模块前缀
     */
    public static final String MONITOR_PREFIX = "monitor";

    /**
     * 服务器监控路径
     */
    public static final String SERVER_MONITOR_PATH = "/monitor/server";

    /**
     * 缓存管理路径
     */
    public static final String CACHE_MANAGEMENT_PATH = "/monitor/cache";

    /**
     * Druid监控路径
     */
    public static final String DRUID_MONITOR_PATH = "/monitor/druid";

    /**
     * 健康检查路径
     */
    public static final String HEALTH_CHECK_PATH = "/monitor/health";

    /**
     * 在线用户管理路径
     */
    public static final String ONLINE_USER_PATH = "/monitor/online";

    /**
     * 权限前缀
     */
    public static final String PERMISSION_PREFIX = "monitor:";

    private MonitorConstants() {
        // 私有构造函数，防止实例化
    }
}
