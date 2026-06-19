package cn.org.starpivot.generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 外部库代码生成配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "gen.external")
public class GenExternalProperties {

    /** 是否启用外部库代码生成 */
    private boolean enabled = true;

    /** 会话 TTL（分钟） */
    private int sessionTtlMinutes = 30;

    /** JDBC 连接超时（毫秒） */
    private int connectTimeoutMs = 5000;

    /** JDBC 查询超时（秒） */
    private int queryTimeoutSeconds = 30;

    /** 单次 ZIP 下载最大表数量 */
    private int maxTablesPerDownload = 20;

    /** 允许连接的主机白名单（空则不限） */
    private java.util.List<String> allowedHosts = new java.util.ArrayList<>();

    /** 是否允许服务端写盘生成 */
    private boolean writeToDiskEnabled = true;

    /** 写盘默认项目根目录（空则 user.dir） */
    private String defaultOutputRoot = "";

    /**
     * 写盘允许的路径前缀（空则不限）。
     * 示例: E:/star-pivot/project0422/StarPivot
     */
    private java.util.List<String> allowedWritePaths = new java.util.ArrayList<>();

    /**
     * 自定义 Velocity 模板目录（空则使用 classpath vm/）。
     * 目录结构需与 resources/vm 一致。
     */
    private String templateDir = "";

    /** 写盘覆盖前是否自动备份 */
    private boolean backupBeforeWrite = true;
}

