package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.external.GenPathProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalGenConfigRequest extends ExternalSessionRequest {

    @NotEmpty(message = "表名列表不能为空")
    private List<String> tableNames;

    @NotBlank(message = "模板类型不能为空")
    private String tplCategory;

    @NotBlank(message = "前端类型不能为空")
    private String tplWebType;

    private String author;

    @Valid
    private GenPathProfile pathProfile;
}

