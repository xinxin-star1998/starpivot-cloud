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

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

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
