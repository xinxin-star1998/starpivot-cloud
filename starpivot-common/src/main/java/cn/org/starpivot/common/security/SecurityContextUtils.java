package cn.org.starpivot.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 从 Spring Security 上下文读取当前登录用户。
 * <p>
 * 依赖 {@link cn.org.starpivot.common.filter.MicroserviceAuthenticationFilter} 将
 * {@link LoginUser} 写入 {@code Authentication#getPrincipal()}。
 * </p>
 */
public final class SecurityContextUtils {

    private SecurityContextUtils() {
    }

    /**
     * 获取当前认证主体中的 {@link LoginUser}。
     *
     * @return 登录用户，未认证或 principal 非 {@link LoginUser} 时返回 {@code null}
     */
    public static LoginUser getLoginUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser loginUser) {
            return loginUser;
        }
        return null;
    }

    /** @return 当前用户 ID，未登录时返回 {@code null} */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /** @return 当前设备会话 ID，未登录或未写入 JWT 时返回 {@code null} */
    public static String getSessionId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getSessionId() : null;
    }

    /**
     * 获取当前用户名；优先 {@link LoginUser#getUsername()}，否则回退至 {@code Authentication#getName()}。
     *
     * @return 用户名，未认证时返回 {@code null}
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        if (loginUser != null) {
            return loginUser.getUsername();
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return null;
    }

    /**
     * 判断当前用户是否拥有指定 authority（角色或权限标识）。
     *
     * @param authority 权限字符串，空值返回 {@code false}
     * @return 拥有返回 {@code true}
     */
    public static boolean hasAuthority(String authority) {
        if (authority == null || authority.isBlank()) {
            return false;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }
}
