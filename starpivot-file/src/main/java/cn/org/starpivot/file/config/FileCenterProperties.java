package cn.org.starpivot.file.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 文件中心上传配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "file-center.upload")
public class FileCenterProperties {

    /** 按媒体类型的单文件大小上限（字节） */
    private Map<String, Long> maxSizeByMediaType = defaultMaxSizes();

    private static Map<String, Long> defaultMaxSizes() {
        Map<String, Long> map = new HashMap<>();
        map.put("IMAGE", 10L * 1024 * 1024);
        map.put("VIDEO", 200L * 1024 * 1024);
        map.put("DOCUMENT", 50L * 1024 * 1024);
        map.put("AUDIO", 50L * 1024 * 1024);
        map.put("OTHER", 50L * 1024 * 1024);
        return map;
    }
}
