package cn.org.starpivot.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件中心回收站物理清理配置。
 */
@Data
@ConfigurationProperties(prefix = "file-center.purge")
public class FileCenterPurgeProperties {

    /** 是否启用定时物理清理 */
    private boolean enabled = true;

    /** 回收站保留天数，超期后删除 OSS 与 DB 记录 */
    private int recycleRetentionDays = 90;

    /** 每批处理条数 */
    private int batchSize = 100;

    /** 定时任务 cron 表达式（默认每天 03:00） */
    private String cron = "0 0 3 * * ?";
}
