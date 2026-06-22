package cn.org.starpivot.generator.domain.dto.external;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 外部库写盘回退请求 DTO，继承 {@link ExternalSessionRequest}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalWriteRollbackRequest extends ExternalSessionRequest {

    /** 写盘时生成的备份 ID */
    @NotBlank(message = "备份 ID 不能为空")
    private String backupId;

    /** 输出根目录（可选，须与写盘时一致） */
    private String outputRoot;
}
