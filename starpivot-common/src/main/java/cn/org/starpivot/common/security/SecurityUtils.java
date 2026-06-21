package cn.org.starpivot.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 安全工具类
 * 
 * 提供密码加密和验证功能，使用 Spring 容器中的 PasswordEncoder Bean
 * 确保整个系统使用统一的密码编码策略
 */
@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private static PasswordEncoder passwordEncoder;

    /**
     * 加密密码
     * @param password 原始密码
     * @return 加密后的密码
     */
    public static String encryptPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    public boolean matchesPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}