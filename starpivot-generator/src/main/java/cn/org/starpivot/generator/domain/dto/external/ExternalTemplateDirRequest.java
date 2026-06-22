package cn.org.starpivot.generator.domain.dto.external;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外部库会话级 Velocity 模板目录配置请求 DTO，继承 {@link ExternalSessionRequest}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalTemplateDirRequest extends ExternalSessionRequest {

    /** 会话级模板目录，空字符串表示清除覆盖、回退全局配置 */
    private String templateDir;
}
