package cn.org.starpivot.generator.domain.dto.external;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外部库会话下列出表字段的请求 DTO，继承 {@link ExternalSessionRequest}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalColumnsRequest extends ExternalSessionRequest {

    /** 目标表名 */
    @NotBlank(message = "表名不能为空")
    private String tableName;
}
