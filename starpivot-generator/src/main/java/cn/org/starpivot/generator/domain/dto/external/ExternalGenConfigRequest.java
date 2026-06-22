package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.external.GenPathProfile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 外部库批量生成配置保存请求 DTO，继承 {@link ExternalSessionRequest}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalGenConfigRequest extends ExternalSessionRequest {

    /** 待配置的表名列表 */
    @NotEmpty(message = "表名列表不能为空")
    private List<String> tableNames;

    /** 模板分类（如单表、树表、主子表） */
    @NotBlank(message = "模板类型不能为空")
    private String tplCategory;

    /** 前端模板类型 */
    @NotBlank(message = "前端类型不能为空")
    private String tplWebType;

    /** 代码作者署名 */
    private String author;

    /** 输出路径配置 */
    @Valid
    private GenPathProfile pathProfile;
}
