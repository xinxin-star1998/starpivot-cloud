package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.common.domain.PageReqBo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalTableQueryDTO extends PageReqBo {

    @NotBlank(message = "sessionId 不能为空")
    private String sessionId;

    private String tableName;

    private String tableComment;
}

