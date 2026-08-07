package cn.org.starpivot.common.util;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * {@link LogUtils} 单元测试。
 * <p>覆盖参数脱敏、字符串截断、IP 解析、浏览器/操作系统识别及登录地点判断。</p>
 */
class LogUtilsTest {

    // ==================== desensitizeParam ====================

    @Nested
    @DisplayName("desensitizeParam JSON 脱敏")
    class DesensitizeJsonTests {

        @Test
        @DisplayName("密码字段被脱敏为 ******")
        void passwordField_desensitized() {
            String json = "{\"username\":\"admin\",\"password\":\"secret123\"}";
            String result = LogUtils.desensitizeParam(json);

            assertFalse(result.contains("secret123"));
            assertTrue(result.contains("******"));
            assertTrue(result.contains("admin"));
        }

        @Test
        @DisplayName("Token 字段被脱敏")
        void tokenField_desensitized() {
            String json = "{\"accessToken\":\"eyJhbGciOiJIUzI1NiJ9\"}";
            String result = LogUtils.desensitizeParam(json);

            assertFalse(result.contains("eyJhbGciOiJIUzI1NiJ9"));
            assertTrue(result.contains("******"));
        }

        @Test
        @DisplayName("手机号字段被脱敏")
        void phoneField_desensitized() {
            String json = "{\"phonenumber\":\"13812345678\",\"name\":\"张三\"}";
            String result = LogUtils.desensitizeParam(json);

            assertFalse(result.contains("13812345678"));
            assertTrue(result.contains("张三"));
        }

        @Test
        @DisplayName("邮箱字段被脱敏")
        void emailField_desensitized() {
            String json = "{\"email\":\"test@example.com\"}";
            String result = LogUtils.desensitizeParam(json);

            assertFalse(result.contains("test@example.com"));
            assertTrue(result.contains("******"));
        }

        @Test
        @DisplayName("身份证号字段被脱敏")
        void idCardField_desensitized() {
            String json = "{\"idCard\":\"110101199001011234\"}";
            String result = LogUtils.desensitizeParam(json);

            assertFalse(result.contains("110101199001011234"));
        }

        @Test
        @DisplayName("银行卡号字段被脱敏")
        void bankCardField_desensitized() {
            String json = "{\"bankCard\":\"6222021234567890123\"}";
            String result = LogUtils.desensitizeParam(json);

            assertFalse(result.contains("6222021234567890123"));
        }

        @Test
        @DisplayName("多个敏感字段同时脱敏")
        void multipleFields_desensitized() {
            String json = "{\"password\":\"pwd123\",\"email\":\"a@b.com\",\"oldPassword\":\"old\",\"newPassword\":\"new\"}";
            String result = LogUtils.desensitizeParam(json);

            assertFalse(result.contains("pwd123"));
            assertFalse(result.contains("a@b.com"));
            assertFalse(result.contains("\"old\""));
            assertFalse(result.contains("\"new\""));
        }

        @Test
        @DisplayName("非敏感字段不被脱敏")
        void nonSensitiveField_unchanged() {
            String json = "{\"username\":\"admin\",\"nickName\":\"管理员\"}";
            String result = LogUtils.desensitizeParam(json);

            assertTrue(result.contains("admin"));
            assertTrue(result.contains("管理员"));
        }
    }

    // ==================== desensitizeParam 纯文本 ====================

    @Nested
    @DisplayName("desensitizeParam 纯文本脱敏")
    class DesensitizeStringTests {

        @Test
        @DisplayName("非 JSON 格式的含手机号文本被脱敏")
        void textWithPhone_desensitized() {
            // 拼接非 JSON 文本，使 JSON.parse 抛异常走 desensitizeString 路径
            String result = LogUtils.desensitizeParam("手机号:13812345678 联系人:张三");
            assertTrue(result.contains("138****5678"), "手机号中间四位应被替换为 ****");
        }

        @Test
        @DisplayName("纯数字被 JSON 解析为数值，不触发手机号脱敏")
        void plainNumber_noPhoneMask() {
            // JSON.parse("13812345678") 解析为 Long，走 desensitizeJson 路径，无字段匹配
            String result = LogUtils.desensitizeParam("13812345678");
            assertEquals("13812345678", result);
        }

        @Test
        @DisplayName("空字符串原样返回")
        void emptyString_unchanged() {
            assertEquals("", LogUtils.desensitizeParam(""));
        }

        @Test
        @DisplayName("null 原样返回")
        void nullString_unchanged() {
            assertNull(LogUtils.desensitizeParam(null));
        }
    }

    // ==================== truncateString ====================

    @Nested
    @DisplayName("truncateString 截断字符串")
    class TruncateStringTests {

        @Test
        @DisplayName("短字符串不截断")
        void shortString_noTruncate() {
            assertEquals("hello", LogUtils.truncateString("hello", 100));
        }

        @Test
        @DisplayName("超长字符串被截断并追加 ...")
        void longString_truncated() {
            String longStr = "a".repeat(300);
            String result = LogUtils.truncateString(longStr, 100);

            assertTrue(result.endsWith("..."));
            assertTrue(result.length() <= 100);
        }

        @Test
        @DisplayName("null 返回 null")
        void nullInput_returnsNull() {
            assertNull(LogUtils.truncateString(null, 100));
        }

        @Test
        @DisplayName("空字符串返回空字符串")
        void emptyInput_returnsEmpty() {
            assertEquals("", LogUtils.truncateString("", 100));
        }

        @Test
        @DisplayName("UTF-8 中文多字节字符正确截断")
        void utf8Chinese_correctTruncation() {
            String chinese = "测试数据".repeat(50); // 200 chars, 600 bytes
            String result = LogUtils.truncateString(chinese, 100);

            assertTrue(result.endsWith("..."));
            assertTrue(result.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 100);
        }
    }

    // ==================== toJsonString ====================

    @Nested
    @DisplayName("toJsonString 对象序列化")
    class ToJsonStringTests {

        @Test
        @DisplayName("null 返回空字符串")
        void nullInput_returnsEmpty() {
            assertEquals("", LogUtils.toJsonString(null));
        }

        @Test
        @DisplayName("正常对象序列化为 JSON 字符串")
        void normalObject_serialized() {
            var map = java.util.Map.of("key", "value");
            String result = LogUtils.toJsonString(map);

            assertTrue(result.contains("key"));
            assertTrue(result.contains("value"));
        }
    }

    // ==================== getClientIp ====================

    @Nested
    @DisplayName("getClientIp 解析客户端 IP")
    class GetClientIpTests {

        @Test
        @DisplayName("X-Forwarded-For 优先，取第一个 IP")
        void xForwardedFor_firstIp() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn("1.2.3.4, 5.6.7.8");

            assertEquals("1.2.3.4", LogUtils.getClientIp(request));
        }

        @Test
        @DisplayName("X-Forwarded-For 为 unknown 时回退到 X-Real-IP")
        void xForwardedForUnknown_fallbackToRealIp() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn("unknown");
            when(request.getHeader("X-Real-IP")).thenReturn("10.0.0.1");

            assertEquals("10.0.0.1", LogUtils.getClientIp(request));
        }

        @Test
        @DisplayName("无代理头时返回 remoteAddr")
        void noProxyHeaders_remoteAddr() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("X-Forwarded-For")).thenReturn(null);
            when(request.getHeader("X-Real-IP")).thenReturn(null);
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");

            assertEquals("127.0.0.1", LogUtils.getClientIp(request));
        }

        @Test
        @DisplayName("request 为 null 时返回空字符串")
        void nullRequest_returnsEmpty() {
            assertEquals("", LogUtils.getClientIp(null));
        }
    }

    // ==================== getBrowser ====================

    @Nested
    @DisplayName("getBrowser 浏览器识别")
    class GetBrowserTests {

        @Test
        @DisplayName("识别 Chrome 浏览器")
        void chrome_identified() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("User-Agent"))
                    .thenReturn("Mozilla/5.0 Chrome/120.0.0.0");

            assertEquals("Chrome", LogUtils.getBrowser(request));
        }

        @Test
        @DisplayName("识别 Edge 浏览器")
        void edge_identified() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("User-Agent"))
                    .thenReturn("Mozilla/5.0 Edg/120.0.0.0");

            assertEquals("Edge", LogUtils.getBrowser(request));
        }

        @Test
        @DisplayName("识别 Firefox 浏览器")
        void firefox_identified() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("User-Agent"))
                    .thenReturn("Mozilla/5.0 Firefox/120.0");

            assertEquals("Firefox", LogUtils.getBrowser(request));
        }

        @Test
        @DisplayName("null request 返回空字符串")
        void nullRequest_returnsEmpty() {
            assertEquals("", LogUtils.getBrowser(null));
        }

        @Test
        @DisplayName("未知 User-Agent 返回 Unknown")
        void unknownUa_returnsUnknown() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("User-Agent")).thenReturn("SomeBot/1.0");

            assertEquals("Unknown", LogUtils.getBrowser(request));
        }
    }

    // ==================== getOs ====================

    @Nested
    @DisplayName("getOs 操作系统识别")
    class GetOsTests {

        @Test
        @DisplayName("识别 Windows 10")
        void windows10_identified() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("User-Agent"))
                    .thenReturn("Mozilla/5.0 Windows NT 10.0; Win64; x64");

            assertEquals("Windows 10", LogUtils.getOs(request));
        }

        @Test
        @DisplayName("识别 macOS")
        void macos_identified() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("User-Agent"))
                    .thenReturn("Mozilla/5.0 Mac OS X 10_15_7");

            assertEquals("macOS", LogUtils.getOs(request));
        }

        @Test
        @DisplayName("识别 Android")
        void android_identified() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("User-Agent"))
                    .thenReturn("Mozilla/5.0 Linux; Android 13");

            assertEquals("Android", LogUtils.getOs(request));
        }

        @Test
        @DisplayName("识别 iOS")
        void ios_identified() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            when(request.getHeader("User-Agent"))
                    .thenReturn("Mozilla/5.0 iPhone; CPU iPhone OS 17_0");

            assertEquals("iOS", LogUtils.getOs(request));
        }

        @Test
        @DisplayName("null request 返回空字符串")
        void nullRequest_returnsEmpty() {
            assertEquals("", LogUtils.getOs(null));
        }
    }

    // ==================== getLoginLocation ====================

    @Nested
    @DisplayName("getLoginLocation 登录地点判断")
    class GetLoginLocationTests {

        @Test
        @DisplayName("127.0.0.1 返回内网IP")
        void localhost_internalIp() {
            assertEquals("内网IP", LogUtils.getLoginLocation("127.0.0.1"));
        }

        @Test
        @DisplayName("192.168.x.x 返回内网IP")
        void privateNetwork192_internalIp() {
            assertEquals("内网IP", LogUtils.getLoginLocation("192.168.1.100"));
        }

        @Test
        @DisplayName("10.x.x.x 返回内网IP")
        void privateNetwork10_internalIp() {
            assertEquals("内网IP", LogUtils.getLoginLocation("10.0.0.1"));
        }

        @Test
        @DisplayName("172.x.x.x 返回内网IP")
        void privateNetwork172_internalIp() {
            assertEquals("内网IP", LogUtils.getLoginLocation("172.16.0.1"));
        }

        @Test
        @DisplayName("公网 IP 返回空字符串")
        void publicIp_returnsEmpty() {
            assertEquals("", LogUtils.getLoginLocation("8.8.8.8"));
        }

        @Test
        @DisplayName("空 IP 返回空字符串")
        void emptyIp_returnsEmpty() {
            assertEquals("", LogUtils.getLoginLocation(""));
        }

        @Test
        @DisplayName("null IP 返回空字符串")
        void nullIp_returnsEmpty() {
            assertEquals("", LogUtils.getLoginLocation(null));
        }
    }
}
