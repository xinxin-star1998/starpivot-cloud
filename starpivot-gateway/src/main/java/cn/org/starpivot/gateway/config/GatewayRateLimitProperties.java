package cn.org.starpivot.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关限流配置（基于 Redis 固定窗口计数）。
 */
@Data
@ConfigurationProperties(prefix = "starpivot.gateway.rate-limit")
public class GatewayRateLimitProperties {

    /** 是否启用网关限流 */
    private boolean enabled = true;

    /** 限流规则列表，按顺序匹配，命中第一条即生效 */
    private List<Rule> rules = defaultRules();

    @Data
    public static class Rule {
        /** 规则标识，用于 Redis Key */
        private String id;
        /** Ant 风格路径模式 */
        private String pattern;
        /** 窗口内最大请求数 */
        private int limit = 20;
        /** 窗口时长（秒） */
        private int windowSeconds = 60;
    }

    private static List<Rule> defaultRules() {
        List<Rule> rules = new ArrayList<>();

        rules.add(rule("auth-login", "/api/v1/auth/login", 20, 60));
        rules.add(rule("auth-captcha", "/api/v1/auth/captcha/**", 30, 60));
        rules.add(rule("auth-forgot-password", "/api/v1/auth/forgot-password/**", 10, 60));
        rules.add(rule("portal-sms", "/api/v1/portal/auth/sms/**", 10, 60));
        rules.add(rule("portal-login-password", "/api/v1/portal/auth/login/password", 20, 60));
        rules.add(rule("portal-member-login", "/api/v1/portal/member/login", 20, 60));
        rules.add(rule("portal-member-register", "/api/v1/portal/member/register", 10, 60));

        return rules;
    }

    private static Rule rule(String id, String pattern, int limit, int windowSeconds) {
        Rule rule = new Rule();
        rule.setId(id);
        rule.setPattern(pattern);
        rule.setLimit(limit);
        rule.setWindowSeconds(windowSeconds);
        return rule;
    }
}
