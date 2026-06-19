package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ExternalWriteResultVO {

    private String outputRoot;

    @Builder.Default
    private List<String> writtenFiles = new ArrayList<>();

    private int fileCount;

    /** 本次写盘备份 ID（覆盖文件时生成，可用于回退） */
    private String backupId;

    /** 备份的文件数量 */
    private int backedUpCount;
}

