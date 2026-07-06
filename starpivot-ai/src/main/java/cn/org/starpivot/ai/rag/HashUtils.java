package cn.org.starpivot.ai.rag;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public final class HashUtils {

    private HashUtils() {}

    public static String md5(String text) {
        if (text == null) {
            return "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            return String.valueOf(text.hashCode());
        }
    }
}
