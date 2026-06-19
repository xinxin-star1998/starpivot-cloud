package cn.org.starpivot.generator.domain.dto.external;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalWriteRollbackRequest extends ExternalSessionRequest {

    @NotBlank(message = "备份 ID 不能为空")
    private String backupId;

    private String outputRoot;
}

