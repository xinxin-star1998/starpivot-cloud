package cn.org.starpivot.approval.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 审批安全配置：跨库读取 RBAC 时的系统库 schema 名。
 */
@Data
@Component
@ConfigurationProperties(prefix = "starpivot.approval")
public class ApprovalSecurityProperties {

    /**
     * 与 starpivot-system 的 datasource 库名一致。
     */
    private String systemDbSchema = "star_pivot";

    public String validatedSystemDbSchema() {
        if (!StringUtils.hasText(systemDbSchema) || !systemDbSchema.matches("[a-zA-Z0-9_]+")) {
            throw new IllegalStateException("Invalid starpivot.approval.system-db-schema: " + systemDbSchema);
        }
        return systemDbSchema;
    }
}
