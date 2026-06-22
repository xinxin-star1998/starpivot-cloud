package cn.org.starpivot.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 微服务间内部调用鉴权配置。
 * 生产环境请通过环境变量 INTERNAL_SERVICE_TOKEN 注入，未配置时内部接口不校验（仅便于本地开发）。
 */
@Data
@ConfigurationProperties(prefix = "starpivot.internal")
public class InternalServiceProperties {

    /** 服务间共享密钥，Feign 调用 /internal/** 时携带 */
    private String token = "";
}
