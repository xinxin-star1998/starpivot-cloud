package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 外部库写盘差异单文件预览请求 DTO，继承 {@link ExternalSessionRequest}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalWriteDiffFileRequest extends ExternalSessionRequest {

    /** 待比对的表名列表 */
    @NotEmpty(message = "表名列表不能为空")
    private List<String> tableNames;

    /** 输出根目录（可选，空则使用默认配置） */
    private String outputRoot;

    /** 生成范围 */
    private ExternalGenScope genScope;

    /** 待预览的相对文件路径 */
    @NotBlank(message = "文件路径不能为空")
    private String path;
}
