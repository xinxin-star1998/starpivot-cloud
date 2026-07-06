package cn.org.starpivot.ai.rag.splitter;

import cn.org.starpivot.ai.rag.loader.ParseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class SlidingWindowChunkSplitter {

    private static final int MAX_BACKTRACK = 100;

    public List<ChunkResult> split(ParseResult parseResult, ChunkConfig config) {
        List<ChunkResult> chunks = new ArrayList<>();
        int chunkIndex = 0;
        for (ParseResult.PageContent page : parseResult.getPages()) {
            String text = page.getText();
            if (text == null || text.isBlank()) {
                continue;
            }
            for (String chunkText : splitText(text, config.getChunkSize(), config.getChunkOverlap())) {
                if (chunkText.isBlank()) {
                    continue;
                }
                chunks.add(ChunkResult.builder()
                        .chunkIndex(chunkIndex++)
                        .content(chunkText)
                        .pageNum(page.getPageNum())
                        .sectionTitle(page.getSectionTitle())
                        .build());
            }
        }
        return chunks;
    }

    private List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            if (end < text.length()) {
                end = findGoodBreakPoint(text, end);
            }
            String chunk = text.substring(start, end).strip();
            if (!chunk.isBlank()) {
                result.add(chunk);
            }
            if (end >= text.length()) {
                break;
            }
            int nextStart = end - overlap;
            if (nextStart <= start) {
                nextStart = end;
            }
            start = nextStart;
        }
        return result;
    }

    private int findGoodBreakPoint(String text, int position) {
        int searchLimit = position - MAX_BACKTRACK;
        String[] breakChars = {"\n\n", "\n", "。", "！", "？", "；", "，", " "};
        for (String breakChar : breakChars) {
            int idx = text.lastIndexOf(breakChar, position);
            if (idx > searchLimit && idx > 0) {
                return idx + breakChar.length();
            }
        }
        return position;
    }
}
