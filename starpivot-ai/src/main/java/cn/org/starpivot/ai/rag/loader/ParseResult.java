package cn.org.starpivot.ai.rag.loader;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ParseResult {

    private boolean success;

    private String errorMsg;

    private List<PageContent> pages;

    private int totalPages;

    private String title;

    @Data
    @Builder
    public static class PageContent {
        private int pageNum;
        private String text;
        private String sectionTitle;
    }

    public static ParseResult failure(String errorMsg) {
        return ParseResult.builder()
                .success(false)
                .errorMsg(errorMsg)
                .pages(List.of())
                .build();
    }

    public String getFullText() {
        if (pages == null) {
            return "";
        }
        return pages.stream()
                .map(PageContent::getText)
                .filter(t -> t != null && !t.isBlank())
                .reduce("", (a, b) -> a + "\n\n" + b)
                .strip();
    }
}
