package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalWriteDiffFileRequest extends ExternalSessionRequest {

    @NotEmpty(message = "表名列表不能为空")
    private List<String> tableNames;

    private String outputRoot;

    private ExternalGenScope genScope;

    @NotBlank(message = "文件路径不能为空")
    private String path;
}

