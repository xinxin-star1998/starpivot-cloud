package cn.org.starpivot.mall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 本地文件存储静态访问（OSS 未启用时，供小程序/H5 加载 /local-storage/** 图片）。
 */
@Configuration
@ConditionalOnProperty(prefix = "oss", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LocalStorageResourceConfig implements WebMvcConfigurer {

    @Value("${file.storage.local-path:${user.home}/starpivot/uploads}")
    private String localPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path baseDir = Paths.get(localPath).toAbsolutePath().normalize();
        String location = baseDir.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/local-storage/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
