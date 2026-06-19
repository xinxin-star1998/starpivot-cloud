package cn.org.starpivot.generator.domain.dto.external;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ExternalImportResultVO {

    @Builder.Default
    private List<String> imported = new ArrayList<>();

    @Builder.Default
    private List<String> updated = new ArrayList<>();

    @Builder.Default
    private List<String> skipped = new ArrayList<>();
}

