package cn.org.starpivot.ai.rag.splitter;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChunkConfig {

    private int chunkSize;

    private int chunkOverlap;

    @Builder.Default
    private boolean structureAware = true;
}
