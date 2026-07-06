package cn.org.starpivot.ai.rag.splitter;

import cn.org.starpivot.ai.rag.loader.ParseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagChunkService {

    private final SlidingWindowChunkSplitter slidingWindowChunkSplitter;

    public List<ChunkResult> chunk(ParseResult parseResult, int chunkSize, int chunkOverlap) {
        if (parseResult == null || !parseResult.isSuccess()) {
            return List.of();
        }
        ChunkConfig config = ChunkConfig.builder()
                .chunkSize(chunkSize)
                .chunkOverlap(chunkOverlap)
                .build();
        List<ChunkResult> chunks = slidingWindowChunkSplitter.split(parseResult, config);
        return chunks.stream()
                .filter(c -> c.getContent() != null && c.getContent().length() >= 20)
                .toList();
    }
}
