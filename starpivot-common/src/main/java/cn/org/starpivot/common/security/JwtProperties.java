package cn.org.starpivot.common.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * JWT 配置属性，各服务通过 @EnableConfigurationProperties 启用
 */
@Data
@Validated
@ConfigurationProperties(prefix = "starpivot.jwt")
public class JwtProperties {

    /** HS256 密钥，至少 32 字符，请通过环境变量 JWT_SECRET 注入 */
    @NotBlank(message = "JWT secret cannot be blank, set environment variable JWT_SECRET")
    private String secret;

    /** 访问令牌有效期（毫秒），默认 2 小时 */
    @NotNull(message = "Access token expiration cannot be null")
    @Min(value = 60000, message = "Access token expiration must be at least 1 minute (60000 ms)")
    private long expire = 7_200_000L;

    /** 刷新令牌有效期（毫秒），默认 7 天 */
    @NotNull(message = "Refresh token expiration cannot be null")
    @Min(value = 3600000, message = "Refresh token expiration must be at least 1 hour (3600000 ms)")
    private long refreshExpire = 604_800_000L;

    /** Token刷新提前时间（毫秒），默认1小时，表示在token到期前1小时提示需要刷新 */
    private long refreshGracePeriod = 3_600_000L;

    /**
     * 检查密钥强度
     */
    public boolean isSecretStrong() {
        return secret != null && secret.length() >= 32;
    }
}
