package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ExternalWriteDirListVO {

    private String current;

    private String parent;

    @Builder.Default
    private List<ExternalWriteDirEntryVO> directories = new ArrayList<>();
}

