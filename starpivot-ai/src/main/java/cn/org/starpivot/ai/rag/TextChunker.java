package cn.org.starpivot.ai.rag;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public final class TextChunker {

    private TextChunker() {}

    public static List<String> chunk(String text, int chunkSize, int overlap) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        int size = Math.max(chunkSize, 200);
        int lap = Math.max(Math.min(overlap, size / 3), 0);
        String normalized = text.replace("\r\n", "\n").trim();
        List<String> paragraphs = splitParagraphs(normalized);
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String paragraph : paragraphs) {
            if (paragraph.length() > size) {
                flushChunk(chunks, current);
                chunks.addAll(splitLongText(paragraph, size, lap));
                continue;
            }
            if (current.length() + paragraph.length() + 1 > size && current.length() > 0) {
                flushChunk(chunks, current);
                applyOverlap(current, lap);
            }
            if (current.length() > 0) {
                current.append('\n');
            }
            current.append(paragraph);
        }
        flushChunk(chunks, current);
        return chunks;
    }

    private static List<String> splitParagraphs(String text) {
        String[] parts = text.split("\n+");
        List<String> paragraphs = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                paragraphs.add(part.trim());
            }
        }
        return paragraphs;
    }

    private static List<String> splitLongText(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end).trim());
            if (end >= text.length()) {
                break;
            }
            start = Math.max(end - overlap, start + 1);
        }
        return chunks;
    }

    private static void flushChunk(List<String> chunks, StringBuilder current) {
        if (current.length() == 0) {
            return;
        }
        chunks.add(current.toString().trim());
        current.setLength(0);
    }

    private static void applyOverlap(StringBuilder current, int overlap) {
        if (overlap <= 0 || current.length() == 0) {
            current.setLength(0);
            return;
        }
        String value = current.toString();
        current.setLength(0);
        current.append(value.substring(Math.max(0, value.length() - overlap)));
    }
}
