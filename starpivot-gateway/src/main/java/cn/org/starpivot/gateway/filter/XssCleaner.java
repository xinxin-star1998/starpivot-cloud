package cn.org.starpivot.gateway.filter;

import java.util.regex.Pattern;

/**
 * XSS 清理工具类。
 * <p>
 * 基于正则表达式清理 HTML 标签、危险属性和事件处理器，防止跨站脚本攻击。
 * 轻量实现，不引入额外依赖（如 OWASP Java HTML Sanitizer）。
 * </p>
 * <p>清理策略：</p>
 * <ul>
 *   <li>移除 {@code <script>} 标签及其内容</li>
 *   <li>移除 {@code <iframe>}, {@code <object>}, {@code <embed>} 等危险标签</li>
 *   <li>移除 {@code javascript:}, {@code vbscript:}, {@code data:} 协议</li>
 *   <li>移除 {@code on*} 事件处理器属性（如 {@code onclick}, {@code onerror}）</li>
 *   <li>移除 {@code expression()} CSS 表达式</li>
 *   <li>转义 {@code <}, {@code >}, {@code "}, {@code '}, {@code &} 等 HTML 特殊字符</li>
 * </ul>
 */
public final class XssCleaner {

    private XssCleaner() {
    }

    // ==================== 危险标签模式 ====================

    /** 匹配 <script>...</script> 标签及其内容（含换行） */
    private static final Pattern SCRIPT_TAG_PATTERN =
            Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** 匹配 <iframe>, <object>, <embed>, <applet>, <form> 等危险标签 */
    private static final Pattern DANGEROUS_TAG_PATTERN =
            Pattern.compile("<(iframe|object|embed|applet|form|base|link|meta|style)[^>]*>.*?</\\1>",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /** 匹配自闭合的危险标签 */
    private static final Pattern SELF_CLOSING_DANGEROUS_TAG_PATTERN =
            Pattern.compile("<(iframe|object|embed|applet|form|base|link|meta|style)[^>]*/?>",
                    Pattern.CASE_INSENSITIVE);

    // ==================== 危险协议模式 ====================

    /** 匹配 javascript:, vbscript:, data: 协议 */
    private static final Pattern DANGEROUS_PROTOCOL_PATTERN =
            Pattern.compile("(javascript|vbscript|data)\\s*:", Pattern.CASE_INSENSITIVE);

    // ==================== 事件处理器模式 ====================

    /** 匹配 on* 事件属性（如 onclick="...", onerror='...'） */
    private static final Pattern EVENT_HANDLER_PATTERN =
            Pattern.compile("\\bon\\w+\\s*=\\s*[\"'][^\"']*[\"']", Pattern.CASE_INSENSITIVE);

    /** 匹配无引号的 on* 事件属性 */
    private static final Pattern EVENT_HANDLER_NO_QUOTE_PATTERN =
            Pattern.compile("\\bon\\w+\\s*=\\s*[^\\s>]+", Pattern.CASE_INSENSITIVE);

    // ==================== CSS 危险模式 ====================

    /** 匹配 expression() CSS 表达式 */
    private static final Pattern CSS_EXPRESSION_PATTERN =
            Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE);

    /** 匹配 url() 中的危险协议 */
    private static final Pattern CSS_URL_PATTERN =
            Pattern.compile("url\\s*\\(\\s*[\"']?\\s*(javascript|vbscript|data)\\s*:", Pattern.CASE_INSENSITIVE);

    // ==================== HTML 特殊字符 ====================

    /** 匹配 < 和 > */
    private static final Pattern ANGLE_BRACKET_PATTERN =
            Pattern.compile("[<>]");

    /**
     * 清理输入字符串中的 XSS 危险内容。
     *
     * @param input 原始输入
     * @return 清理后的安全字符串，{@code null} 输入返回 {@code null}
     */
    public static String clean(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;

        // 1. 移除 <script> 标签及内容
        result = SCRIPT_TAG_PATTERN.matcher(result).replaceAll("");

        // 2. 移除危险标签及内容
        result = DANGEROUS_TAG_PATTERN.matcher(result).replaceAll("");

        // 3. 移除自闭合危险标签
        result = SELF_CLOSING_DANGEROUS_TAG_PATTERN.matcher(result).replaceAll("");

        // 4. 移除危险协议
        result = DANGEROUS_PROTOCOL_PATTERN.matcher(result).replaceAll("");

        // 5. 移除事件处理器属性
        result = EVENT_HANDLER_PATTERN.matcher(result).replaceAll("");
        result = EVENT_HANDLER_NO_QUOTE_PATTERN.matcher(result).replaceAll("");

        // 6. 移除 CSS 危险表达式
        result = CSS_EXPRESSION_PATTERN.matcher(result).replaceAll("");
        result = CSS_URL_PATTERN.matcher(result).replaceAll("");

        // 7. 转义 HTML 特殊字符
        result = escapeHtml(result);

        return result;
    }

    /**
     * 转义 HTML 特殊字符。
     *
     * @param input 原始字符串
     * @return 转义后的字符串
     */
    private static String escapeHtml(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                case '&':
                    // 避免重复转义已存在的实体
                    if (i + 1 < input.length() && input.charAt(i + 1) != '#') {
                        sb.append("&amp;");
                    } else {
                        sb.append(c);
                    }
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 检查输入是否包含潜在 XSS 内容（用于日志记录）。
     *
     * @param input 待检查的字符串
     * @return 包含危险内容返回 {@code true}
     */
    public static boolean containsXss(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return SCRIPT_TAG_PATTERN.matcher(input).find()
                || DANGEROUS_TAG_PATTERN.matcher(input).find()
                || DANGEROUS_PROTOCOL_PATTERN.matcher(input).find()
                || EVENT_HANDLER_PATTERN.matcher(input).find();
    }
}
