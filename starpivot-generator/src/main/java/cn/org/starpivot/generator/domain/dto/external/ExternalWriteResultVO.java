package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 外部库代码写盘结果 VO。
 * <p>
 * {@link Data}：生成 getter/setter；
 * {@link Builder}：支持建造者模式构造。
 */
@Data
@Builder
public class ExternalWriteResultVO {

    /** 实际写入的输出根目录 */
    private String outputRoot;

    /** 已写入文件的相对路径列表 */
    @Builder.Default
    private List<String> writtenFiles = new ArrayList<>();

    /** 写入文件总数 */
    private int fileCount;

    /** 本次写盘备份 ID（覆盖文件时生成，可用于回退） */
    private String backupId;

    /** 备份的文件数量 */
    private int backedUpCount;
}
