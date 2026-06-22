package cn.org.starpivot.common.util;

import com.alibaba.fastjson2.JSON;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 操作日志辅助工具：客户端 IP、User-Agent 解析及请求参数脱敏。
 * <p>
 * 供 {@link cn.org.starpivot.common.annotation.Log} 切面及登录/操作日志落库时使用。
 * </p>
 */
public final class LogUtils {

    private static final List<String> SENSITIVE_FIELDS = Arrays.asList(
            "password", "pwd", "passwd", "pass",
            "oldPassword", "newPassword", "confirmPassword",
            "token", "accessToken", "refreshToken",
            "secret", "secretKey", "apiKey",
            "idCard", "idcard", "cardNo",
            "phone", "phonenumber", "mobile", "tel",
            "email", "mail",
            "bankCard", "bankcard", "cardNumber",
            "cvv", "cvc"
    );

    private LogUtils() {
    }

    /**
     * 从请求头解析客户端真实 IP（优先 {@code X-Forwarded-For}、{@code X-Real-IP}）。
     *
     * @param request HTTP 请求，可为 {@code null}
     * @return 客户端 IP，无法解析时返回空字符串
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            int index = ip.indexOf(',');
            return index != -1 ? ip.substring(0, index).trim() : ip.trim();
        }
        ip = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip)) {
            return ip.trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 从 {@link RequestContextHolder} 获取当前线程绑定的 {@link HttpServletRequest}。
     *
     * @return 当前请求，非 Web 上下文时返回 {@code null}
     */
    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 对 JSON 或纯文本请求参数脱敏，隐藏密码、Token 等敏感字段。
     *
     * @param param 原始参数字符串
     * @return 脱敏后的字符串
     */
    public static String desensitizeParam(String param) {
        if (!StringUtils.hasText(param)) {
            return param;
        }
        try {
            Object obj = JSON.parse(param);
            return desensitizeJson(JSON.toJSONString(obj));
        } catch (Exception e) {
            return desensitizeString(param);
        }
    }

    private static String desensitizeJson(String jsonStr) {
        String result = jsonStr;
        for (String field : SENSITIVE_FIELDS) {
            Pattern pattern = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"([^\"]+)\"",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                matcher.appendReplacement(sb, "\"" + field + "\":\"******\"");
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }
        return result;
    }

    private static String desensitizeString(String str) {
        return str.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 将对象序列化为 JSON 并截断至指定长度，用于日志输出。
     *
     * @param obj 待序列化对象
     * @return JSON 字符串，{@code null} 时返回空字符串
     */
    public static String toJsonString(Object obj) {
        if (obj == null) {
            return "";
        }
        try {
            return truncateString(JSON.toJSONString(obj), 2000);
        } catch (Exception e) {
            return truncateString(String.valueOf(obj), 2000);
        }
    }

    /**
     * 按 UTF-8 字节长度截断字符串，超出时在末尾追加 {@code ...}。
     *
     * @param str       原字符串
     * @param maxLength 最大字节长度
     * @return 截断后的字符串
     */
    public static String truncateString(String str, int maxLength) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        if (str.length() <= maxLength && str.getBytes(StandardCharsets.UTF_8).length <= maxLength) {
            return str;
        }
        int targetLength = Math.min(str.length(), maxLength - 10);
        if (targetLength <= 0) {
            return "...";
        }
        String truncated = str.substring(0, targetLength);
        while (truncated.getBytes(StandardCharsets.UTF_8).length > maxLength - 10 && targetLength > 0) {
            targetLength--;
            truncated = str.substring(0, targetLength);
        }
        return truncated + "...";
    }

    /**
     * 根据 User-Agent 识别浏览器类型（Chrome、Firefox、Edge 等）。
     *
     * @param request HTTP 请求
     * @return 浏览器名称，未知时返回 {@code Unknown}
     */
    public static String getBrowser(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ua = request.getHeader("User-Agent");
        if (!StringUtils.hasText(ua)) {
            return "";
        }
        if (ua.contains("Edg/")) {
            return truncateString("Edge", 50);
        }
        if (ua.contains("Chrome/")) {
            return truncateString("Chrome", 50);
        }
        if (ua.contains("Firefox/")) {
            return truncateString("Firefox", 50);
        }
        if (ua.contains("Safari/") && !ua.contains("Chrome")) {
            return truncateString("Safari", 50);
        }
        return truncateString("Unknown", 50);
    }

    /**
     * 根据 User-Agent 识别操作系统（Windows、macOS、Android 等）。
     *
     * @param request HTTP 请求
     * @return 操作系统名称
     */
    public static String getOs(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String ua = request.getHeader("User-Agent");
        if (!StringUtils.hasText(ua)) {
            return "";
        }
        if (ua.contains("Windows NT 10.0")) {
            return "Windows 10";
        }
        if (ua.contains("Windows NT 6.1")) {
            return "Windows 7";
        }
        if (ua.contains("Mac OS X")) {
            return "macOS";
        }
        if (ua.contains("Android")) {
            return "Android";
        }
        if (ua.contains("iPhone") || ua.contains("iPad")) {
            return "iOS";
        }
        if (ua.contains("Linux")) {
            return "Linux";
        }
        return truncateString("Unknown", 50);
    }

    /**
     * 根据 IP 判断登录地点；内网地址返回 {@code 内网IP}，公网 IP 暂留空（可扩展 GeoIP）。
     *
     * @param ip 客户端 IP
     * @return 地点描述
     */
    public static String getLoginLocation(String ip) {
        if (!StringUtils.hasText(ip)) {
            return "";
        }
        if (ip.startsWith("127.") || ip.startsWith("192.168.") || ip.startsWith("10.")
                || ip.startsWith("172.") || "localhost".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) {
            return "内网IP";
        }
        return "";
    }
}
