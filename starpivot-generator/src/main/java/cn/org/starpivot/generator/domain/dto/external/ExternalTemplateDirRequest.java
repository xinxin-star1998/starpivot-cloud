package cn.org.starpivot.generator.domain.dto.external;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalTemplateDirRequest extends ExternalSessionRequest {

    /** 会话级模板目录，空字符串表示清除覆盖、回退全局配置 */
    private String templateDir;
}

