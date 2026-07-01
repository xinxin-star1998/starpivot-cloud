package cn.org.starpivot.common.cache;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一缓存命名与键构建（若依风格，便于监控页识别）。
 * <p>
 * 约定：缓存分组名使用下划线命名（如 {@code login_tokens}），
 * 业务键使用 {@code 分组名:子类型:业务ID}，Spring Cache 使用 {@code 分组名::业务键}。
 * </p>
 */
public final class CacheConstants {

    private CacheConstants() {
    }

    // ── 缓存分组名（监控页「缓存列表」展示） ─────────────────────────────

    /** 登录令牌 / 刷新令牌 / 登出黑名单 */
    public static final String LOGIN_TOKENS = "login_tokens";
    /** 系统参数配置 */
    public static final String SYS_CONFIG = "sys_config";
    /** 数据字典（Spring Cache） */
    public static final String SYS_DICT = "sys_dict";
    /** 验证码 */
    public static final String CAPTCHA_CODES = "captcha_codes";
    /** 防重复提交 */
    public static final String REPEAT_SUBMIT = "repeat_submit";
    /** 接口限流 */
    public static final String RATE_LIMIT = "rate_limit";
    /** 密码错误次数 / 账号锁定 */
    public static final String PWD_ERR_CNT = "pwd_err_cnt";
    /** 用户权限 */
    public static final String USER_PERMISSIONS = "user_permissions";
    /** 菜单树（Spring Cache） */
    public static final String MENU_TREE = "menu_tree";
    /** 部门树（Spring Cache） */
    public static final String DEPT_TREE = "dept_tree";
    /** 角色列表（Spring Cache） */
    public static final String ROLE_LIST = "role_list";
    /** 控制台仪表盘 */
    public static final String DASHBOARD = "dashboard";
    /** 外部代码生成会话 */
    public static final String GENERATOR_SESSION = "generator_session";

    // ── login_tokens 子类型 ─────────────────────────────────────────────

    public static final String LOGIN_REFRESH = "refresh";
    public static final String LOGIN_BLACKLIST = "blacklist";

    // ── 默认 TTL ────────────────────────────────────────────────────────

    public static final Duration TTL_LOGIN_TOKEN = Duration.ofDays(7);
    public static final Duration TTL_SYS_CONFIG = Duration.ofHours(2);
    public static final Duration TTL_SYS_DICT = Duration.ofHours(24);
    public static final Duration TTL_CAPTCHA = Duration.ofMinutes(5);
    public static final Duration TTL_PWD_ERR = Duration.ofMinutes(15);
    public static final Duration TTL_USER_PERMISSIONS = Duration.ofMinutes(30);
    public static final Duration TTL_MENU_TREE = Duration.ofHours(1);
    public static final Duration TTL_DEPT_TREE = Duration.ofHours(1);
    public static final Duration TTL_ROLE_LIST = Duration.ofHours(1);
    public static final Duration TTL_DASHBOARD = Duration.ofMinutes(5);

    // ── 键构建 ──────────────────────────────────────────────────────────

    public static String loginRefreshKey(Long userId) {
        return LOGIN_TOKENS + ":" + LOGIN_REFRESH + ":" + userId;
    }

    public static String loginRefreshPattern() {
        return LOGIN_TOKENS + ":" + LOGIN_REFRESH + ":*";
    }

    public static String tokenBlacklistKey(String sanitizedToken) {
        return LOGIN_TOKENS + ":" + LOGIN_BLACKLIST + ":" + sanitizedToken;
    }

    public static String tokenBlacklistPrefix() {
        return LOGIN_TOKENS + ":" + LOGIN_BLACKLIST + ":";
    }

    public static String captchaKey(String scene, String token) {
        return CAPTCHA_CODES + ":" + scene + ":" + token;
    }

    public static String sysConfigKey(String configKey) {
        return SYS_CONFIG + ":" + configKey;
    }

    public static String pwdErrCountKey(String username) {
        return PWD_ERR_CNT + ":count:" + username;
    }

    public static String accountLockKey(String username) {
        return PWD_ERR_CNT + ":lock:" + username;
    }

    public static String userPermissionKey(Long userId) {
        return USER_PERMISSIONS + ":" + userId;
    }

    public static String userPermissionPattern() {
        return USER_PERMISSIONS + ":*";
    }

    public static String gatewayRateLimitKey(String ruleId, String clientKey) {
        return RATE_LIMIT + ":gateway:" + ruleId + ":" + clientKey;
    }

    public static String dashboardKey() {
        return DASHBOARD + ":console";
    }

    public static String generatorSessionKey(String sessionId) {
        return GENERATOR_SESSION + ":" + sessionId;
    }

    // ── 监控页备注 ──────────────────────────────────────────────────────

    private static final Map<String, String> REMARKS = new LinkedHashMap<>();

    static {
        REMARKS.put(LOGIN_TOKENS, "用户信息");
        REMARKS.put(SYS_CONFIG, "配置信息");
        REMARKS.put(SYS_DICT, "数据字典");
        REMARKS.put(CAPTCHA_CODES, "验证码");
        REMARKS.put(REPEAT_SUBMIT, "防重提交");
        REMARKS.put(RATE_LIMIT, "限流处理");
        REMARKS.put(PWD_ERR_CNT, "密码错误次数");
        REMARKS.put(USER_PERMISSIONS, "用户权限");
        REMARKS.put(MENU_TREE, "菜单树");
        REMARKS.put(DEPT_TREE, "部门树");
        REMARKS.put(ROLE_LIST, "角色列表");
        REMARKS.put(DASHBOARD, "控制台统计");
        REMARKS.put(GENERATOR_SESSION, "代码生成会话");
    }

    /** 监控页固定展示的缓存分组（顺序与若依一致，并扩展项目自有分组） */
    public static List<String> predefinedGroups() {
        return List.of(
                LOGIN_TOKENS,
                SYS_CONFIG,
                SYS_DICT,
                CAPTCHA_CODES,
                REPEAT_SUBMIT,
                RATE_LIMIT,
                PWD_ERR_CNT,
                USER_PERMISSIONS,
                MENU_TREE,
                DEPT_TREE,
                ROLE_LIST,
                DASHBOARD,
                GENERATOR_SESSION
        );
    }

    public static String getRemark(String groupName) {
        if (groupName == null) {
            return "";
        }
        String remark = REMARKS.get(groupName);
        if (remark != null) {
            return remark;
        }
        if (groupName.startsWith("cache:")) {
            String logical = groupName.substring("cache:".length());
            return REMARKS.getOrDefault(logical, logical + "（Spring Cache）");
        }
        return groupName;
    }

    /** Spring Cache 逻辑名 -> TTL，供 RedisCacheManager 注册 */
    public static Map<String, Duration> springCacheTtls() {
        Map<String, Duration> map = new LinkedHashMap<>();
        map.put(SYS_DICT, TTL_SYS_DICT);
        map.put(MENU_TREE, TTL_MENU_TREE);
        map.put(DEPT_TREE, TTL_DEPT_TREE);
        map.put(ROLE_LIST, TTL_ROLE_LIST);
        return map;
    }
}
