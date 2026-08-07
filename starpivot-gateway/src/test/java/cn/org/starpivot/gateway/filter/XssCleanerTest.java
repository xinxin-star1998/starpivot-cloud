package cn.org.starpivot.gateway.filter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link XssCleaner} 单元测试。
 * <p>验证各类 XSS payload 的清理效果。</p>
 */
class XssCleanerTest {

    @Nested
    @DisplayName("Script 标签清理")
    class ScriptTagTests {

        @Test
        @DisplayName("移除 <script> 标签及其内容")
        void removesScriptTag() {
            String input = "Hello <script>alert('XSS')</script> World";
            String result = XssCleaner.clean(input);
            assertFalse(result.contains("<script"));
            assertFalse(result.contains("alert"));
            assertTrue(result.contains("Hello"));
            assertTrue(result.contains("World"));
        }

        @Test
        @DisplayName("移除带属性的 <script> 标签")
        void removesScriptTagWithAttributes() {
            String input = "<script type=\"text/javascript\">document.cookie</script>";
            String result = XssCleaner.clean(input);
            assertFalse(result.contains("<script"));
            assertFalse(result.contains("document.cookie"));
        }

        @Test
        @DisplayName("移除跨行 <script> 标签")
        void removesMultiLineScriptTag() {
            String input = "Text <script>\nvar x = 1;\nalert(x);\n</script> More text";
            String result = XssCleaner.clean(input);
            assertFalse(result.contains("<script"));
            assertFalse(result.contains("alert"));
            assertTrue(result.contains("Text"));
            assertTrue(result.contains("More text"));
        }

        @Test
        @DisplayName("忽略大小写移除 <SCRIPT> 标签")
        void removesScriptTagCaseInsensitive() {
            String input = "<SCRIPT>alert('XSS')</SCRIPT>";
            String result = XssCleaner.clean(input);
            assertFalse(result.toLowerCase().contains("<script"));
        }
    }

    @Nested
    @DisplayName("危险标签清理")
    class DangerousTagTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "<iframe src=\"evil.com\"></iframe>",
                "<object data=\"evil.swf\"></object>",
                "<embed src=\"evil.swf\">",
                "<applet code=\"Evil.class\"></applet>",
                "<form action=\"evil.com\"><input></form>"
        })
        @DisplayName("移除危险 HTML 标签")
        void removesDangerousTags(String input) {
            String result = XssCleaner.clean(input);
            assertFalse(result.toLowerCase().contains("<iframe"));
            assertFalse(result.toLowerCase().contains("<object"));
            assertFalse(result.toLowerCase().contains("<embed"));
            assertFalse(result.toLowerCase().contains("<applet"));
            assertFalse(result.toLowerCase().contains("<form"));
        }
    }

    @Nested
    @DisplayName("危险协议清理")
    class DangerousProtocolTests {

        @Test
        @DisplayName("移除 javascript: 协议")
        void removesJavascriptProtocol() {
            String input = "<a href=\"javascript:alert('XSS')\">Click</a>";
            String result = XssCleaner.clean(input);
            assertFalse(result.toLowerCase().contains("javascript:"));
        }

        @Test
        @DisplayName("移除 vbscript: 协议")
        void removesVbscriptProtocol() {
            String input = "<a href=\"vbscript:MsgBox('XSS')\">Click</a>";
            String result = XssCleaner.clean(input);
            assertFalse(result.toLowerCase().contains("vbscript:"));
        }

        @Test
        @DisplayName("移除 data: 协议")
        void removesDataProtocol() {
            String input = "<img src=\"data:text/html,<script>alert('XSS')</script>\">";
            String result = XssCleaner.clean(input);
            assertFalse(result.toLowerCase().contains("data:text/html"));
        }

        @Test
        @DisplayName("移除带空格的 javascript: 协议")
        void removesJavascriptProtocolWithSpaces() {
            String input = "<a href=\"java script:alert('XSS')\">Click</a>";
            String result = XssCleaner.clean(input);
            assertFalse(result.toLowerCase().contains("javascript:"));
        }
    }

    @Nested
    @DisplayName("事件处理器清理")
    class EventHandlerTests {

        @Test
        @DisplayName("移除 onclick 事件")
        void removesOnClick() {
            String input = "<div onclick=\"alert('XSS')\">Content</div>";
            String result = XssCleaner.clean(input);
            assertFalse(result.toLowerCase().contains("onclick"));
        }

        @Test
        @DisplayName("移除 onerror 事件")
        void removesOnError() {
            String input = "<img src=\"x\" onerror=\"alert('XSS')\">";
            String result = XssCleaner.clean(input);
            assertFalse(result.toLowerCase().contains("onerror"));
        }

        @Test
        @DisplayName("移除 onload 事件")
        void removesOnLoad() {
            String input = "<body onload=\"alert('XSS')\">";
            String result = XssCleaner.clean(input);
            assertFalse(result.toLowerCase().contains("onload"));
        }

        @Test
        @DisplayName("移除单引号包裹的事件处理器")
        void removesEventHandlerWithSingleQuotes() {
            String input = "<div onmouseover='alert(1)'>Content</div>";
            String result = XssCleaner.clean(input);
            assertFalse(result.toLowerCase().contains("onmouseover"));
        }
    }

    @Nested
    @DisplayName("HTML 特殊字符转义")
    class HtmlEscapeTests {

        @Test
        @DisplayName("转义 < 和 >")
        void escapesAngleBrackets() {
            String input = "<div>test</div>";
            String result = XssCleaner.clean(input);
            assertTrue(result.contains("&lt;"));
            assertTrue(result.contains("&gt;"));
            assertFalse(result.contains("<div>"));
        }

        @Test
        @DisplayName("转义双引号")
        void escapesDoubleQuotes() {
            String input = "value=\"test\"";
            String result = XssCleaner.clean(input);
            assertTrue(result.contains("&quot;"));
        }

        @Test
        @DisplayName("转义单引号")
        void escapesSingleQuotes() {
            String input = "value='test'";
            String result = XssCleaner.clean(input);
            assertTrue(result.contains("&#39;"));
        }
    }

    @Nested
    @DisplayName("边界场景")
    class EdgeCaseTests {

        @Test
        @DisplayName("null 输入返回 null")
        void nullInput_returnsNull() {
            assertNull(XssCleaner.clean(null));
        }

        @Test
        @DisplayName("空字符串返回空字符串")
        void emptyInput_returnsEmpty() {
            assertEquals("", XssCleaner.clean(""));
        }

        @Test
        @DisplayName("安全文本保持不变")
        void safeText_unchanged() {
            String input = "Hello World 123";
            String result = XssCleaner.clean(input);
            assertEquals(input, result);
        }

        @Test
        @DisplayName("中文文本保持不变")
        void chineseText_unchanged() {
            String input = "你好世界";
            String result = XssCleaner.clean(input);
            assertEquals(input, result);
        }

        @Test
        @DisplayName("混合内容：清理危险部分保留安全部分")
        void mixedContent_cleansDangerousKeepsSafe() {
            String input = "Safe text <script>alert('XSS')</script> more safe text";
            String result = XssCleaner.clean(input);
            assertFalse(result.contains("<script"));
            assertTrue(result.contains("Safe text"));
            assertTrue(result.contains("more safe text"));
        }
    }

    @Nested
    @DisplayName("containsXss 检测")
    class ContainsXssTests {

        @Test
        @DisplayName("null 返回 false")
        void null_returnsFalse() {
            assertFalse(XssCleaner.containsXss(null));
        }

        @Test
        @DisplayName("空字符串返回 false")
        void empty_returnsFalse() {
            assertFalse(XssCleaner.containsXss(""));
        }

        @Test
        @DisplayName("安全文本返回 false")
        void safeText_returnsFalse() {
            assertFalse(XssCleaner.containsXss("Hello World"));
        }

        @Test
        @DisplayName("包含 script 标签返回 true")
        void scriptTag_returnsTrue() {
            assertTrue(XssCleaner.containsXss("<script>alert('XSS')</script>"));
        }

        @Test
        @DisplayName("包含 javascript: 协议返回 true")
        void javascriptProtocol_returnsTrue() {
            assertTrue(XssCleaner.containsXss("javascript:alert(1)"));
        }

        @Test
        @DisplayName("包含事件处理器返回 true")
        void eventHandler_returnsTrue() {
            assertTrue(XssCleaner.containsXss("<div onclick=\"alert(1)\">"));
        }
    }
}
