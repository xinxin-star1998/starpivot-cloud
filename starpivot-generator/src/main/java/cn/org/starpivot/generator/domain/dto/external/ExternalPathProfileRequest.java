package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.external.GenPathProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外部库会话路径配置保存请求 DTO，继承 {@link ExternalSessionRequest}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalPathProfileRequest extends ExternalSessionRequest {

    /** 代码输出路径配置 */
    @NotNull(message = "pathProfile 不能为空")
    @Valid
    private GenPathProfile pathProfile;
}
