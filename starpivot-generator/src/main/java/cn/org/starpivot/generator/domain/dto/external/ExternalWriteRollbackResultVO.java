package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 外部库写盘回退结果 VO。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link Builder}：支持建造者模式构造。
 */
@Data
@Builder
public class ExternalWriteRollbackResultVO {

    /** 使用的备份 ID */
    private String backupId;

    /** 回退目标根目录 */
    private String outputRoot;

    /** 已恢复文件的相对路径列表 */
    @Builder.Default
    private List<String> restoredFiles = new ArrayList<>();

    /** 恢复文件总数 */
    private int fileCount;
}
