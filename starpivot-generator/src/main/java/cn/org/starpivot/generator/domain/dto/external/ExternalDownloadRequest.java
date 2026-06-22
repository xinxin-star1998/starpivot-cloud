package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 外部库代码生成 ZIP 下载请求 DTO，继承 {@link ExternalSessionRequest}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalDownloadRequest extends ExternalSessionRequest {

    /** 待生成代码的表名列表 */
    @NotEmpty(message = "表名列表不能为空")
    private List<String> tableNames;

    /** 生成范围，默认全选 */
    private ExternalGenScope genScope;
}
