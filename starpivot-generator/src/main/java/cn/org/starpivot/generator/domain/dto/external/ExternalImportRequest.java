package cn.org.starpivot.generator.domain.dto.external;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalImportRequest extends ExternalSessionRequest {

    @NotEmpty(message = "表名列表不能为空")
    private List<String> tableNames;

    /** 已存在同名 gen_table 时是否覆盖 */
    private boolean overwrite;
}

