package cn.org.starpivot.ai.rag.loader;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class DocxParser implements DocumentParser {

    @Override
    public String supportedType() {
        return "DOCX";
    }

    @Override
    public ParseResult parse(InputStream inputStream, String fileName) {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<ParseResult.PageContent> pages = new ArrayList<>();
            StringBuilder currentSection = new StringBuilder();
            String currentTitle = null;
            int sectionCount = 0;

            for (IBodyElement elem : document.getBodyElements()) {
                if (elem instanceof XWPFParagraph paragraph) {
                    String text = paragraph.getText();
                    if (text == null || text.isBlank()) {
                        continue;
                    }
                    String style = paragraph.getStyle();
                    boolean isHeading = style != null
                            && (style.startsWith("Heading") || style.startsWith("heading") || style.contains("标题"));
                    if (isHeading && currentSection.length() > 200) {
                        pages.add(buildSection(++sectionCount, currentSection, currentTitle));
                        currentSection = new StringBuilder();
                        currentTitle = text;
                    } else if (isHeading) {
                        currentTitle = text;
                    }
                    currentSection.append(text).append("\n");
                } else if (elem instanceof XWPFTable table) {
                    appendTable(currentSection, table);
                }
            }
            if (!currentSection.isEmpty()) {
                pages.add(buildSection(++sectionCount, currentSection, currentTitle));
            }
            if (pages.isEmpty()) {
                return ParseResult.failure("DOCX 解析后无有效文本内容");
            }
            return ParseResult.builder()
                    .success(true)
                    .pages(pages)
                    .totalPages(pages.size())
                    .title(pages.get(0).getSectionTitle())
                    .build();
        } catch (Exception ex) {
            log.error("[DOCX解析] 文件={}，解析失败", fileName, ex);
            return ParseResult.failure("DOCX 解析失败：" + ex.getMessage());
        }
    }

    private ParseResult.PageContent buildSection(int sectionCount, StringBuilder currentSection, String currentTitle) {
        return ParseResult.PageContent.builder()
                .pageNum(sectionCount)
                .text(currentSection.toString().strip())
                .sectionTitle(currentTitle)
                .build();
    }

    private void appendTable(StringBuilder currentSection, XWPFTable table) {
        StringBuilder tableText = new StringBuilder();
        for (XWPFTableRow row : table.getRows()) {
            List<String> cellTexts = row.getTableCells().stream()
                    .map(XWPFTableCell::getText)
                    .filter(t -> !t.isBlank())
                    .toList();
            if (!cellTexts.isEmpty()) {
                tableText.append(String.join(" | ", cellTexts)).append("\n");
            }
        }
        if (!tableText.isEmpty()) {
            currentSection.append("\n[表格]\n").append(tableText);
        }
    }
}
