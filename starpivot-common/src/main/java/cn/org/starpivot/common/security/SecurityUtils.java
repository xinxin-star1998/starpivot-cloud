package cn.org.starpivot.common.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 安全工具类
 *
 * 提供密码加密和验证功能，使用 Spring 容器中的 PasswordEncoder Bean
 * 确保整个系统使用统一的密码编码策略
 */
@Component
public class SecurityUtils {

    private static volatile PasswordEncoder passwordEncoder;

    public SecurityUtils(PasswordEncoder passwordEncoder) {
        SecurityUtils.passwordEncoder = passwordEncoder;
    }

    /**
     * 加密密码
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        PasswordEncoder encoder = passwordEncoder;
        if (encoder == null) {
            throw new IllegalStateException("SecurityUtils 尚未初始化，请确保在 Spring 容器启动完成后调用");
        }
        return encoder.encode(password);
    }

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        PasswordEncoder encoder = passwordEncoder;
        if (encoder == null) {
            throw new IllegalStateException("SecurityUtils 尚未初始化，请确保在 Spring 容器启动完成后调用");
        }
        return encoder.matches(rawPassword, encodedPassword);
    }
}