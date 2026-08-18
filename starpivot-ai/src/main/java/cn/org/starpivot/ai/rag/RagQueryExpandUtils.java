package cn.org.starpivot.ai.rag;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 把「权限以及菜单」这类并列问题拆成多路检索查询。
 */
public final class RagQueryExpandUtils {

    private static final Pattern SPLIT = Pattern.compile("(?:以及|还有|另外|分别是|分别|；|;|，且|并且)");
    private static final int MIN_PART_LEN = 2;
    private static final int MAX_QUERIES = 3;

    private RagQueryExpandUtils() {}

    public static List<String> expand(String query) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }
        String normalized = query.trim();
        if (!SPLIT.matcher(normalized).find()) {
            return List.of(normalized);
        }

        String[] parts = SPLIT.split(normalized);
        List<String> clauses = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.length() >= MIN_PART_LEN) {
                clauses.add(trimmed);
            }
        }
        if (clauses.size() < 2) {
            return List.of(normalized);
        }

        Set<String> queries = new LinkedHashSet<>();
        queries.add(normalized);
        for (String clause : clauses) {
            queries.add(clause);
            if (queries.size() >= MAX_QUERIES) {
                break;
            }
        }
        return new ArrayList<>(queries);
    }
}
