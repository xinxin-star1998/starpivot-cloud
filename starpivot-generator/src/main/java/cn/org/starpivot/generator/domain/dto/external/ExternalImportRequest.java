package cn.org.starpivot.generator.domain.dto.external;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 外部库表导入内置 gen_table 的请求 DTO，继承 {@link ExternalSessionRequest}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalImportRequest extends ExternalSessionRequest {

    /** 待导入的表名列表 */
    @NotEmpty(message = "表名列表不能为空")
    private List<String> tableNames;

    /** 已存在同名 gen_table 时是否覆盖 */
    private boolean overwrite;
}
