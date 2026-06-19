package cn.org.starpivot.common.constants;

/**
 * 通用常量信息
 */
public class Constants {
    /**
     * UTF-8 字符集
     */
    public static final String UTF8 = "UTF-8";

    /**
     * GBK 字符集
     */
    public static final String GBK = "GBK";

    /**
     * www主域
     */
    public static final String WWW = "www.";

    /**
     * http请求
     */
    public static final String HTTP = "http://";

    /**
     * https请求
     */
    public static final String HTTPS = "https://";

    /**
     * 通用成功标识
     */
    public static final String SUCCESS = "0";

    /**
     * 通用失败标识
     */
    public static final String FAIL = "1";

    /**
     * 登录成功
     */
    public static final String LOGIN_SUCCESS = "Success";

    /**
     * 注销
     */
    public static final String LOGOUT = "Logout";

    /**
     * 注册
     */
    public static final String REGISTER = "Register";

    /**
     * 登录失败
     */
    public static final String LOGIN_FAIL = "Error";

    /**
     * 系统用户
     */
    public static final String SYS_USER = "sys_user";

    /**
     * 系统用户ID
     */
    public static final Long SYS_USER_ID = 1L;

    /**
     * 管理员用户ID
     */
    public static final Long ADMIN_USER_ID = 1L;

    /**
     * 当前记录起始索引
     */
    public static final String PAGE_NUM = "pageNum";

    /**
     * 每页显示记录数
     */
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 排序列
     */
    public static final String ORDER_BY_COLUMN = "orderByColumn";

    /**
     * 排序的方向 "desc" 或 "asc"
     */
    public static final String IS_ASC = "isAsc";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /**
     * 验证码有效期（分钟）
     */
    public static final long CAPTCHA_EXPIRATION = 2;

    /**
     * 令牌
     */
    public static final String TOKEN = "token";

    /**
     * 令牌前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 令牌前缀
     */
    public static final String LOGIN_USER_KEY = "login_user_key";

    /**
     * 用户ID
     */
    public static final String USER_ID = "userId";

    /**
     * 用户名
     */
    public static final String USERNAME = "username";

    /**
     * 用户头像
     */
    public static final String AVATAR = "avatar";

    /**
     * 岗位ID
     */
    public static final String POST_ID = "postId";

    /**
     * 岗位编码
     */
    public static final String POST_CODE = "postCode";

    /**
     * 角色ID
     */
    public static final String ROLE_ID = "roleId";

    /**
     * 角色编码
     */
    public static final String ROLE_CODE = "roleCode";

    /**
     * 菜单ID
     */
    public static final String MENU_ID = "menuId";

    /**
     * 用户信息
     */
    public static final String USER_INFO = "userInfo";

    /**
     * 角色权限
     */
    public static final String PERMISSIONS = "permissions";

    /**
     * 数据权限
     */
    public static final String DATA_SCOPE = "dataScope";

    /**
     * 菜单权限
     */
    public static final String MENU_PERMISSIONS = "MenuPermissions";

    /**
     * 角色权限字符
     */
    public static final String ROLE_PERMISSION = "rolePermission";

    /**
     * 链路追踪ID
     */
    public static final String TRACE_ID = "traceId";

    /**
     * 管理员角色权限标识
     */
    public static final String ADMIN_ROLE_PERMISSION = "admin";

    /**
     * 管理员角色ID
     */
    public static final Long ADMIN_ROLE_ID = 1L;

    /**
     * 业务类型
     */
    public static final String BUSINESS_TYPE = "businessType";

    /**
     * 操作类型
     */
    public static final String OPERATE_TYPE = "operateType";

    /**
     * 方法名称
     */
    public static final String METHOD_NAME = "methodName";

    /**
     * 请求方式
     */
    public static final String REQUEST_METHOD = "requestMethod";

    /**
     * 目标地址
     */
    public static final String TARGET_URL = "targetUrl";

    /**
     * 请求地址
     */
    public static final String REQUEST_URL = "requestUrl";

    /**
     * 请求参数
     */
    public static final String REQUEST_PARAM = "requestParam";

    /**
     * 请求数据
     */
    public static final String REQUEST_DATA = "requestData";

    /**
     * 响应数据
     */
    public static final String RESPONSE_DATA = "responseData";

    /**
     * 响应状态
     */
    public static final String RESPONSE_STATUS = "responseStatus";

    /**
     * 用户代理
     */
    public static final String USER_AGENT = "userAgent";

    /**
     * IP地址
     */
    public static final String IPADDR = "ipaddr";

    /**
     * 设备ID
     */
    public static final String DEVICE_ID = "deviceId";

    /**
     * 设备类型
     */
    public static final String DEVICE_TYPE = "deviceType";

    /**
     * 会话ID
     */
    public static final String SESSION_ID = "sessionId";

    /**
     * 租户ID
     */
    public static final String TENANT_ID = "tenantId";

    /**
     * 应用ID
     */
    public static final String APP_ID = "appId";

    /**
     * 语言
     */
    public static final String LANG = "lang";

    /**
     * 时区
     */
    public static final String TIMEZONE = "timezone";

    /**
     * 数据源
     */
    public static final String DATASOURCE = "datasource";

    /**
     * 缓存前缀
     */
    public static final String CACHE_PREFIX = "cache:";

    /**
     * 缓存操作类型
     */
    public static final String CACHE_OPERATE_TYPE = "cacheOperateType";

    /**
     * 缓存键
     */
    public static final String CACHE_KEY = "cacheKey";

    /**
     * 缓存值
     */
    public static final String CACHE_VALUE = "cacheValue";

    /**
     * 缓存时间
     */
    public static final String CACHE_TIME = "cacheTime";

    /**
     * 锁前缀
     */
    public static final String LOCK_PREFIX = "lock:";
}