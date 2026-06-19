package cn.org.starpivot.generator.domain.dto.external;

import cn.org.starpivot.generator.domain.external.ExternalGenScope;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExternalWriteRequest extends ExternalSessionRequest {

    @NotEmpty(message = "表名列表不能为空")
    private List<String> tableNames;

    /** 项目根目录（空则使用配置 default-output-root 或 user.dir） */
    private String outputRoot;

    private ExternalGenScope genScope;

    /** 仅写入有变化的文件（跳过 UNCHANGED） */
    private boolean onlyChanged;

    /** 指定写入的文件路径（空则不过滤） */
    private List<String> selectedPaths;

    /** 覆盖前是否备份原文件（默认 true） */
    private Boolean backupBeforeWrite;
}

