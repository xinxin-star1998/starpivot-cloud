package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ExternalWriteDiffResultVO {

    private String outputRoot;

    @Builder.Default
    private int newCount = 0;

    @Builder.Default
    private int modifiedCount = 0;

    @Builder.Default
    private int unchangedCount = 0;

    @Builder.Default
    private List<ExternalWriteDiffItemVO> files = new ArrayList<>();
}

