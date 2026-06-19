package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalDownloadRequest extends ExternalSessionRequest {

    @NotEmpty(message = "表名列表不能为空")
    private List<String> tableNames;

    /** 生成范围，默认全选 */
    private ExternalGenScope genScope;
}

