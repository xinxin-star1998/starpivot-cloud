package cn.org.starpivot.generator.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * 读取代码生成相关配置
 *
 * @author StarPivot
 */
@Data
@Component
@ConfigurationProperties(prefix = "gen")
@PropertySource(value = { "classpath:generator.yml" })
public class GenConfig {

    /** 作者 */
    private String author;

    /** 生成包路径 */
    private String packageName;

    /** 自动去除表前缀 */
    private boolean autoRemovePre;

    /** 表前缀 */
    private String tablePrefix;
}

