package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalPreviewRequest extends ExternalSessionRequest {

    @NotBlank(message = "表名不能为空")
    private String tableName;

    /** 生成范围，默认全选 */
    private ExternalGenScope genScope;
}

