package cn.org.starpivot.generator.domain.dto.external;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalColumnsRequest extends ExternalSessionRequest {

    @NotBlank(message = "表名不能为空")
    private String tableName;
}

