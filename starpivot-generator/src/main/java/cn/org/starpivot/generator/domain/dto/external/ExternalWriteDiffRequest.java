package cn.org.starpivot.generator.domain.dto.external;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外部库写盘差异批量预览请求 DTO，继承 {@link ExternalWriteRequest}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalWriteDiffRequest extends ExternalWriteRequest {

    /** 是否在列表响应中包含文件内容（{@code false} 时仅返回 path/status，详情走 write-diff/file） */
    private Boolean includeContent = true;
}
