package cn.org.starpivot.ai.rag.loader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class MarkdownParser implements DocumentParser {

    private static final Pattern HEADING_PATTERN = Pattern.compile("^#{1,3}\\s+(.+)");

    @Override
    public String supportedType() {
        return "MD";
    }

    @Override
    public ParseResult parse(InputStream inputStream, String fileName) {
        try {
            String markdown = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            List<ParseResult.PageContent> pages = new ArrayList<>();
            String[] lines = markdown.split("\n");
            StringBuilder currentSection = new StringBuilder();
            String currentTitle = null;
            int sectionCount = 0;
            boolean inCodeBlock = false;

            for (String line : lines) {
                if (line.startsWith("```")) {
                    inCodeBlock = !inCodeBlock;
                    currentSection.append(line).append("\n");
                    continue;
                }
                if (inCodeBlock) {
                    currentSection.append(line).append("\n");
                    continue;
                }
                Matcher matcher = HEADING_PATTERN.matcher(line);
                if (matcher.matches() && (line.startsWith("# ") || line.startsWith("## "))) {
                    if (currentSection.length() > 100) {
                        pages.add(ParseResult.PageContent.builder()
                                .pageNum(++sectionCount)
                                .text(stripMarkdownSyntax(currentSection.toString()))
                                .sectionTitle(currentTitle)
                                .build());
                        currentSection = new StringBuilder();
                    }
                    currentTitle = matcher.group(1).strip();
                }
                currentSection.append(line).append("\n");
            }
            if (currentSection.length() > 0) {
                pages.add(ParseResult.PageContent.builder()
                        .pageNum(++sectionCount)
                        .text(stripMarkdownSyntax(currentSection.toString()))
                        .sectionTitle(currentTitle)
                        .build());
            }
            if (pages.isEmpty()) {
                return ParseResult.failure("Markdown 解析后无有效文本内容");
            }
            return ParseResult.builder()
                    .success(true)
                    .pages(pages)
                    .totalPages(pages.size())
                    .build();
        } catch (Exception ex) {
            return ParseResult.failure("Markdown 解析失败：" + ex.getMessage());
        }
    }

    private String stripMarkdownSyntax(String text) {
        return text.replaceAll("!\\[[^\\]]*]\\([^)]*\\)", "")
                .replaceAll("\\[([^\\]]+)]\\([^)]*\\)", "$1")
                .replaceAll("(?m)^#{1,6}\\s+", "")
                .replaceAll("(?m)^[-*+]\\s+", "")
                .replaceAll("(?m)^\\d+\\.\\s+", "")
                .replaceAll("[*_`~]", "")
                .strip();
    }
}
