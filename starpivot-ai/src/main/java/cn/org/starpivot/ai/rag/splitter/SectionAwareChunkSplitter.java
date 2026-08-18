package cn.org.starpivot.ai.rag.splitter;

import cn.org.starpivot.ai.rag.loader.ParseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 优先按解析器给出的章节（MD/DOCX 标题页）保留整段；超长章节再滑动切分。
 */
@Component
@RequiredArgsConstructor
public class SectionAwareChunkSplitter {

    private final SlidingWindowChunkSplitter slidingWindowChunkSplitter;

    public List<ChunkResult> split(ParseResult parseResult, ChunkConfig config) {
        if (parseResult == null || parseResult.getPages() == null || parseResult.getPages().isEmpty()) {
            return List.of();
        }
        boolean hasSections = parseResult.getPages().stream()
                .anyMatch(page -> StringUtils.hasText(page.getSectionTitle()));
        if (!hasSections || !config.isStructureAware()) {
            return slidingWindowChunkSplitter.split(parseResult, config);
        }

        List<ChunkResult> chunks = new ArrayList<>();
        int chunkIndex = 0;
        int softLimit = Math.max(config.getChunkSize(), 200);
        int hardLimit = Math.max(softLimit * 2, softLimit + 200);

        for (ParseResult.PageContent page : parseResult.getPages()) {
            String text = page.getText();
            if (text == null || text.isBlank()) {
                continue;
            }
            String sectionTitle = StringUtils.hasText(page.getSectionTitle())
                    ? page.getSectionTitle().trim()
                    : null;
            String body = text.strip();
            if (body.length() <= hardLimit) {
                chunks.add(ChunkResult.builder()
                        .chunkIndex(chunkIndex++)
                        .content(withSectionPrefix(sectionTitle, body))
                        .pageNum(page.getPageNum())
                        .sectionTitle(sectionTitle)
                        .build());
                continue;
            }

            ParseResult singlePage = ParseResult.builder()
                    .success(true)
                    .pages(List.of(ParseResult.PageContent.builder()
                            .pageNum(page.getPageNum())
                            .text(body)
                            .sectionTitle(sectionTitle)
                            .build()))
                    .totalPages(1)
                    .build();
            for (ChunkResult part : slidingWindowChunkSplitter.split(singlePage, config)) {
                chunks.add(ChunkResult.builder()
                        .chunkIndex(chunkIndex++)
                        .content(withSectionPrefix(sectionTitle, part.getContent()))
                        .pageNum(part.getPageNum())
                        .sectionTitle(sectionTitle)
                        .build());
            }
        }
        return chunks;
    }

    static String withSectionPrefix(String sectionTitle, String body) {
        if (!StringUtils.hasText(body)) {
            return "";
        }
        String text = body.strip();
        if (!StringUtils.hasText(sectionTitle)) {
            return text;
        }
        String title = sectionTitle.trim();
        if (text.startsWith(title) || text.startsWith("【" + title)) {
            return text;
        }
        return "【" + title + "】\n" + text;
    }
}
