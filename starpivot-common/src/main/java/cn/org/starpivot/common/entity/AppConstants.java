package cn.org.starpivot.common.entity;

import cn.org.starpivot.common.enums.BusinessType;

/**
 * 通用常量类
 */
public class AppConstants {

    /**
     * 登录相关常量
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    public static final String USER_ID_KEY = "user_id";

    public static final String USERNAME_KEY = "username";

    public static final String ROLES_KEY = "roles";

    /**
     * 缓存相关常量
     */
    public static final String CACHE_KEY_USER = "user:";

    public static final String CACHE_KEY_ROLE = "role:";

    public static final String CACHE_KEY_MENU = "menu:";

    public static final String CACHE_KEY_DEPT = "dept:";

    public static final String CACHE_KEY_DICT = "dict:";

    /**
     * 系统相关常量
     */
    public static final String SYSTEM_ADMIN = "admin";

    public static final String ADMIN_ROLE_KEY = "admin";

    public static final Long SUPER_ADMIN_ID = 1L;

    public static final Long ADMIN_USER_ID = 1L;

    public static final Long DEFAULT_REGISTER_ROLE_ID = 5L;

    public static final String DEFAULT_PASSWORD = "123456";

    public static class DataScope {
        public static final String ALL = "1";
        public static final String CUSTOM = "2";
        public static final String DEPT = "3";
        public static final String DEPT_AND_CHILD = "4";
        public static final String SELF = "5";
    }

    public static class MenuType {
        public static final String CATALOG = "M";
        public static final String MENU = "C";
        public static final String BUTTON = "F";
    }

    public static class Visible {
        public static final String SHOW = "0";
        public static final String HIDE = "1";
    }

    /**
     * 响应状态码
     */
    public static final int SUCCESS_CODE = 200;

    public static final int ERROR_CODE = 500;

    public static final int UNAUTHORIZED_CODE = 401;

    public static final int FORBIDDEN_CODE = 403;

    public static final int NOT_FOUND_CODE = 404;

    public static final int BAD_REQUEST_CODE = 400;

    /**
     * 用户状态
     */
    public static final String USER_STATUS_NORMAL = "0";

    public static final String USER_STATUS_DISABLE = "1";

    /**
     * 性别常量
     */
    public static final String SEX_MALE = "0";       // 男

    public static final String SEX_FEMALE = "1";     // 女

    public static final String SEX_UNKNOWN = "2";    // 未知

    /**
     * 删除标志
     */
    public static final String DEL_FLAG_NORMAL = "0";    // 正常存在

    public static final String DEL_FLAG_DELETED = "2";   // 已删除

    /**
     * 登录/登出成功消息
     */
    public static final String LOGIN_SUCCESS = "登录成功";

    public static final String LOGOUT_SUCCESS = "退出成功";

    /**
     * 状态常量
     */
    public static class Status {
        public static final String NORMAL = "0";
        public static final String DISABLE = "1";
    }

    /**
     * 通用成功标识
     */
    public static final String SUCCESS = "success";

    /**
     * 删除标志常量
     */
    public static class DelFlag {
        /**
         * 未删除
         */
        public static final String NORMAL = "0";

        /**
         * 已删除
         */
        public static final String DELETED = "2";

        /** 与 StarPivot 命名兼容 */
        public static final String DELETE = DELETED;
    }
}