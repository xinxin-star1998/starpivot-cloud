package cn.org.starpivot.system.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Data
@Component
@ConfigurationProperties(prefix = "starpivot.account")
public class SysAccountProperties {

    /**
     * 新建用户未指定密码时的默认密码。
     * 勿在代码中写死；通过 {@code STAR_PIVOT_DEFAULT_PASSWORD} / Nacos 注入。
     */
    private String defaultPassword;

    public String requireDefaultPassword() {
        if (!StringUtils.hasText(defaultPassword)) {
            throw new IllegalStateException(
                    "未配置 starpivot.account.default-password，请设置环境变量 STAR_PIVOT_DEFAULT_PASSWORD");
        }
        return defaultPassword;
    }
}
