package cn.org.starpivot.common.security.utils;

import cn.org.starpivot.common.security.LoginUser;

/**
 * 安全工具类 - 为其他模块提供便捷的安全操作方法
 */
public class SecurityHelper {

    /**
     * 获取当前登录用户名 - 此处只是占位符，实际获取需在具体上下文中实现
     */
    public static String getCurrentUsername() {
        // 此方法需要在具体安全上下文中实现，不能在common模块中直接实现
        return null;
    }

    /**
     * 获取当前登录用户ID - 此处只是占位符，实际获取需在具体上下文中实现
     */
    public static Long getCurrentUserId() {
        // 此方法需要在具体安全上下文中实现，不能在common模块中直接实现
        return null;
    }

    /**
     * 获取当前登录用户信息 - 此处只是占位符，实际获取需在具体上下文中实现
     */
    public static LoginUser getCurrentLoginUser() {
        // 此方法需要在具体安全上下文中实现，不能在common模块中直接实现
        return null;
    }

    /**
     * 设置当前登录用户 - 此处只是占位符，实际设置需在具体上下文中实现
     */
    public static void setCurrentLoginUser(LoginUser loginUser) {
        // 此方法需要在具体安全上下文中实现，不能在common模块中直接实现
    }
}