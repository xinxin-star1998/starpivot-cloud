package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExternalWriteDirEntryVO {

    private String name;

    private String path;
}

