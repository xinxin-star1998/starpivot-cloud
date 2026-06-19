package cn.org.starpivot.common.security.utils;

import cn.org.starpivot.common.security.LoginUser;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Objects;

/**
 * 安全工具类 - 为其他模块提供便捷的安全操作方法
 */
public class SecurityUtils {

    /**
     * 获取当前用户信息
     *
     * @return LoginUser
     */
    public static LoginUser getLoginUser() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return null;
            }
            return (LoginUser) attributes.getRequest().getAttribute("login_user");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public static String getUsername() {
        LoginUser loginUser = getLoginUser();
        if (Objects.nonNull(loginUser)) {
            return loginUser.getUsername();
        }
        return null;
    }

    /**
     * 获取当前用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        if (Objects.nonNull(loginUser)) {
            return loginUser.getUserId();
        }
        return null;
    }
}