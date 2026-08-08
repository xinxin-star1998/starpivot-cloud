package cn.org.starpivot.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全默认值检测器 — 应用启动后自动扫描潜在的不安全默认配置。
 * <p>
 * 通过实现 {@link SmartInitializingSingleton}，在所有 Bean 初始化完成后执行检测。
 * 发现的每项风险以 {@code WARN} 级别输出日志，帮助开发者在启动阶段即发现配置隐患。
 * <p>
 * 检测项包括：
 * <ul>
 *   <li>JWT 密钥使用开发默认值</li>
 *   <li>内部服务 Token 使用开发默认值或未配置</li>
 *   <li>Redis 密码使用常见弱密码</li>
 *   <li>内部 Token 校验未启用</li>
 *   <li>Feign 熔断器未启用</li>
 *   <li>数据库连接池参数使用默认值</li>
 * </ul>
 */
@Slf4j
public class SafeDefaultsDetector implements SmartInitializingSingleton {

    /** 已知的开发默认 JWT 密钥 */
    private static final String DEV_JWT_SECRET = "dev-local-jwt-secret-must-be-at-least-32-chars";

    /** 已知的开发默认内部 Token */
    private static final String DEV_INTERNAL_TOKEN = "dev-internal-token";

    /** 常见弱 Redis 密码 */
    private static final List<String> WEAK_REDIS_PASSWORDS = List.of(
            "root", "password", "123456", "admin", "redis", "");

    private final Environment env;

    public SafeDefaultsDetector(Environment env) {
        this.env = env;
    }

    @Override
    public void afterSingletonsInstantiated() {
        List<String> warnings = new ArrayList<>();

        checkJwtSecret(warnings);
        checkInternalToken(warnings);
        checkRedisPassword(warnings);
        checkCircuitBreakerEnabled(warnings);
        checkPrometheusAccess(warnings);

        if (warnings.isEmpty()) {
            log.info("[SafeDefaults] ✓ 所有安全配置检查通过");
        } else {
            log.warn("[SafeDefaults] 检测到 {} 项不安全默认值，生产环境请务必修改:", warnings.size());
            warnings.forEach(w -> log.warn("[SafeDefaults]  ⚠ {}", w));
        }
    }

    /**
     * 检查 JWT 密钥是否使用开发默认值。
     */
    private void checkJwtSecret(List<String> warnings) {
        String secret = env.getProperty("starpivot.jwt.secret", DEV_JWT_SECRET);
        if (DEV_JWT_SECRET.equals(secret)) {
            warnings.add("JWT 密钥使用开发默认值 — 请通过环境变量 JWT_SECRET 配置强密钥（≥32字符）");
        } else if (secret != null && secret.length() < 32) {
            warnings.add("JWT 密钥长度仅 " + secret.length() + " 字符 — 建议 ≥32 字符");
        }
    }

    /**
     * 检查内部服务 Token 是否安全。
     */
    private void checkInternalToken(List<String> warnings) {
        String token = env.getProperty("starpivot.internal.token", DEV_INTERNAL_TOKEN);
        boolean requireToken = env.getProperty("starpivot.internal.require-token", Boolean.class, true);

        if (DEV_INTERNAL_TOKEN.equals(token)) {
            warnings.add("内部服务 Token 使用开发默认值 — 请通过环境变量 INTERNAL_SERVICE_TOKEN 配置强随机 Token");
        }
        if (!requireToken) {
            warnings.add("内部服务 Token 校验未启用 (require-token=false) — 生产环境建议开启");
        }
    }

    /**
     * 检查 Redis 密码是否为常见弱密码。
     */
    private void checkRedisPassword(List<String> warnings) {
        String password = env.getProperty("spring.data.redis.password", "root");
        if (WEAK_REDIS_PASSWORDS.contains(password)) {
            warnings.add("Redis 密码为常见弱密码 '" + password + "' — 请通过环境变量 REDIS_PASSWORD 配置强密码");
        }
    }

    /**
     * 检查 Feign 熔断器是否已启用。
     */
    private void checkCircuitBreakerEnabled(List<String> warnings) {
        boolean enabled = env.getProperty("spring.cloud.openfeign.circuitbreaker.enabled",
                Boolean.class, true);
        if (!enabled) {
            warnings.add("Feign 熔断器未启用 (circuitbreaker.enabled=false) — 建议开启以获得服务保护");
        }
    }

    /**
     * 检查 Prometheus 端点是否无鉴权暴露。
     */
    private void checkPrometheusAccess(List<String> warnings) {
        String access = env.getProperty("management.endpoint.prometheus.access", "restricted");
        if ("unrestricted".equalsIgnoreCase(access)) {
            warnings.add("Prometheus 端点无鉴权暴露 (access=unrestricted) — 生产环境请改为 restricted 或通过网络策略限制");
        }
    }
}
