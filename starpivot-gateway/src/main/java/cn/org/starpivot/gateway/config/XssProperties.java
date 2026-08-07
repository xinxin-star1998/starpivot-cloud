package cn.org.starpivot.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * XSS 防护配置属性。
 * <p>
 * 通过 Nacos 或 {@code application.yml} 配置 {@code starpivot.xss.*} 控制 XSS 过滤器行为。
 * </p>
 * <pre>{@code
 * starpivot:
 *   xss:
 *     enabled: true
 *     whitelist:
 *       - /api/v1/ai/chat/**
 *       - /api/v1/knowledge/document/**
 * }</pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "starpivot.xss")
public class XssProperties {

    /**
     * 是否启用 XSS 防护过滤器。
     * <p>默认为 {@code true}。</p>
     */
    private boolean enabled = true;

    /**
     * XSS 过滤白名单路径列表（Ant 模式）。
     * <p>
     * 匹配白名单的请求将跳过 XSS 清理，适用于富文本编辑器提交等场景。
     * </p>
     */
    private List<String> whitelist = new ArrayList<>();
}
