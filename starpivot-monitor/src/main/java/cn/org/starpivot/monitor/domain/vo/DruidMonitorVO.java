package cn.org.starpivot.monitor.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Druid 监控信息 VO
 *
 * @author xinxin
 * @since 2026-01-25
 */
@Data
public class DruidMonitorVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 是否可用：true 表示当前数据源为 Druid，可展示监控；false 表示非 Druid 或未配置，仅展示提示
     */
    private Boolean available;

    /**
     * 不可用时的提示文案（如：数据源不是 Druid 数据源或数据源未配置）
     */
    private String message;

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 数据库类型
     */
    private String dbType;

    /**
     * 驱动类名
     */
    private String driverClassName;

    /**
     * 连接池信息
     */
    private ConnectionPoolInfo connectionPool;

    /**
     * SQL 统计信息
     */
    private SqlStatInfo sqlStat;

    /**
     * 慢SQL列表（可选，当需要详细慢SQL信息时返回）
     */
    private List<SlowSqlInfo> slowSqlList;

    /**
     * 连接池信息
     */
    @Data
    public static class ConnectionPoolInfo implements Serializable {
        /**
         * 初始连接数
         */
        private Integer initialSize;

        /**
         * 最小空闲连接数
         */
        private Integer minIdle;

        /**
         * 最大活跃连接数
         */
        private Integer maxActive;

        /**
         * 当前连接数
         */
        private Integer activeCount;

        /**
         * 活跃连接数
         */
        private Integer activePeak;

        /**
         * 连接池使用率
         */
        private Double usage;
    }

    /**
     * SQL 统计信息
     */
    @Data
    public static class SqlStatInfo implements Serializable {
        /**
         * SQL 执行总数
         */
        private Long executeCount;

        /**
         * SQL 执行总耗时（毫秒）
         */
        private Long executeMillisTotal;

        /**
         * 平均执行时间（毫秒）
         */
        private Double executeMillisAvg;

        /**
         * 慢 SQL 数量
         */
        private Long slowSqlCount;

        /**
         * 错误 SQL 数量
         */
        private Long errorSqlCount;
    }

    /**
     * 慢SQL详细信息
     */
    @Data
    public static class SlowSqlInfo implements Serializable {
        /**
         * SQL ID（Druid生成的SQL标识）
         */
        private String sqlId;

        /**
         * SQL语句
         */
        private String sqlText;

        /**
         * 执行次数
         */
        private Long executeCount;

        /**
         * 总执行时间（毫秒）
         */
        private Long executeTimeTotal;

        /**
         * 最大执行时间（毫秒）
         */
        private Long executeTimeMax;

        /**
         * 平均执行时间（毫秒）
         */
        private Double executeTimeAvg;

        /**
         * 慢SQL次数
         */
        private Long slowCount;

        /**
         * 错误次数
         */
        private Long errorCount;

        /**
         * 最后执行时间
         */
        private Long lastExecuteTime;
    }
}
