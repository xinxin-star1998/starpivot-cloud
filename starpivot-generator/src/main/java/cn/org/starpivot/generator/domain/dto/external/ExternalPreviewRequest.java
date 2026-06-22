package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外部库代码生成预览请求 DTO，继承 {@link ExternalSessionRequest}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalPreviewRequest extends ExternalSessionRequest {

    /** 目标表名 */
    @NotBlank(message = "表名不能为空")
    private String tableName;

    /** 生成范围，默认全选 */
    private ExternalGenScope genScope;
}
