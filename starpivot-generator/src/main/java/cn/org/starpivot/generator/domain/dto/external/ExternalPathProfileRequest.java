package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.external.GenPathProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalPathProfileRequest extends ExternalSessionRequest {

    @NotNull(message = "pathProfile 不能为空")
    @Valid
    private GenPathProfile pathProfile;
}

