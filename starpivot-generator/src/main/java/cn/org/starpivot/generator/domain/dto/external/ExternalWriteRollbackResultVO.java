package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ExternalWriteRollbackResultVO {

    private String backupId;

    private String outputRoot;

    @Builder.Default
    private List<String> restoredFiles = new ArrayList<>();

    private int fileCount;
}

