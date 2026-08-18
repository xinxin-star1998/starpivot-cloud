package cn.org.starpivot.ai.rag.splitter;

import cn.org.starpivot.ai.rag.loader.ParseResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SectionAwareChunkSplitterTest {

    private final SectionAwareChunkSplitter splitter =
            new SectionAwareChunkSplitter(new SlidingWindowChunkSplitter());

    @Test
    void keepsSectionAsWholeWhenUnderLimit() {
        ParseResult parseResult = ParseResult.builder()
                .success(true)
                .pages(List.of(
                        ParseResult.PageContent.builder()
                                .pageNum(1)
                                .sectionTitle("新建用户")
                                .text("1. 打开用户管理\n2. 点击新增\n3. 填写资料并保存")
                                .build()))
                .totalPages(1)
                .build();

        List<ChunkResult> chunks = splitter.split(
                parseResult,
                ChunkConfig.builder().chunkSize(200).chunkOverlap(40).structureAware(true).build());

        assertEquals(1, chunks.size());
        assertTrue(chunks.get(0).getContent().startsWith("【新建用户】"));
        assertTrue(chunks.get(0).getContent().contains("点击新增"));
        assertEquals("新建用户", chunks.get(0).getSectionTitle());
    }

    @Test
    void fallsBackToSlidingWindowWithoutSections() {
        ParseResult parseResult = ParseResult.builder()
                .success(true)
                .pages(List.of(ParseResult.PageContent.builder()
                        .pageNum(1)
                        .text("a".repeat(500))
                        .build()))
                .totalPages(1)
                .build();

        List<ChunkResult> chunks = splitter.split(
                parseResult,
                ChunkConfig.builder().chunkSize(120).chunkOverlap(20).structureAware(true).build());

        assertTrue(chunks.size() > 1);
    }
}
