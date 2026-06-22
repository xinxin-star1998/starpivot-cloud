package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 外部库代码写盘请求 DTO，继承 {@link ExternalSessionRequest}。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link EqualsAndHashCode}：生成 equals/hashCode，含父类字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalWriteRequest extends ExternalSessionRequest {

    /** 待写盘的表名列表 */
    @NotEmpty(message = "表名列表不能为空")
    private List<String> tableNames;

    /** 项目根目录（空则使用配置 default-output-root 或 user.dir） */
    private String outputRoot;

    /** 生成范围 */
    private ExternalGenScope genScope;

    /** 仅写入有变化的文件（跳过 UNCHANGED） */
    private boolean onlyChanged;

    /** 指定写入的文件路径（空则不过滤） */
    private List<String> selectedPaths;

    /** 覆盖前是否备份原文件（默认 {@code true}） */
    private Boolean backupBeforeWrite;
}
