package cn.org.starpivot.common.exception;

/**
 * 全局业务错误码定义，按模块分段编号。
 * <p>
 * 继承 {@link BaseErrorCode} 中的通用码；各模块 Service 抛出 {@link BusinessException} 或
 * {@link BizException} 时引用对应常量。
 * </p>
 */
public interface ErrorCode extends BaseErrorCode {

    // ========== 通用错误 ==========

    /** 请求成功 */
    int SUCCESS = 200;
    /** 系统内部错误 */
    int SYSTEM_ERROR = 500;
    /** 请求参数错误 */
    int PARAM_ERROR = 400;
    /** 未认证（未登录或 Token 无效） */
    int UNAUTHORIZED = 401;
    /** 无访问权限 */
    int FORBIDDEN = 403;
    /** 资源不存在 */
    int NOT_FOUND = 404;
    /** HTTP 方法不允许 */
    int METHOD_NOT_ALLOWED = 405;
    /** 请求超时 */
    int REQUEST_TIMEOUT = 408;
    /** 参数校验失败 */
    int VALIDATE_ERROR = 422;
    /** 通用业务错误 */
    int BIZ_ERROR = 501;
    /** 参数无效 */
    int PARAM_INVALID = 90005;

    // ========== 字典模块 ==========

    /** 字典不存在 */
    int DICT_NOT_FOUND = 40001;
    /** 字典类型已存在 */
    int DICT_TYPE_EXISTS = 40002;
    /** 字典数据已存在 */
    int DICT_DATA_EXISTS = 40003;
    /** 字典类型不存在 */
    int DICT_TYPE_NOT_FOUND = 40004;

    // ========== 用户模块 ==========

    /** 用户不存在 */
    int USER_NOT_FOUND = 50001;
    /** 用户已存在 */
    int USER_EXISTS = 50002;
    /** 密码错误 */
    int USER_PASSWORD_ERROR = 50003;
    /** 账号已锁定 */
    int USER_ACCOUNT_LOCKED = 50004;
    /** 用户名已存在（注册） */
    int USER_USERNAME_EXISTS = 50005;
    /** 用户名已被占用（修改） */
    int USER_USERNAME_USED = 50006;
    /** 导入文件无数据 */
    int USER_IMPORT_EMPTY = 50007;
    /** 导入行数据为空 */
    int USER_IMPORT_ROW_EMPTY = 50008;
    /** 导入用户名为空 */
    int USER_IMPORT_USERNAME_EMPTY = 50009;
    /** 导入昵称为空 */
    int USER_IMPORT_NICKNAME_EMPTY = 50010;

    // ========== 角色模块 ==========

    /** 角色不存在 */
    int ROLE_NOT_FOUND = 60001;
    /** 角色已存在 */
    int ROLE_EXISTS = 60002;
    /** 角色权限字符已存在 */
    int ROLE_KEY_EXISTS = 60003;
    /** 角色权限字符已被占用 */
    int ROLE_KEY_USED = 60004;
    /** 角色已被用户使用 */
    int ROLE_USED = 60005;
    /** 角色已删除 */
    int ROLE_DELETED = 60006;
    /** 角色已停用 */
    int ROLE_DISABLED = 60007;
    /** 内置管理员角色不可操作 */
    int ROLE_ADMIN_PROTECTED = 60008;

    // ========== 菜单模块 ==========

    /** 菜单不存在 */
    int MENU_NOT_FOUND = 70001;
    /** 存在子菜单，不可删除 */
    int MENU_HAS_CHILDREN = 70002;
    /** 同级菜单名称已存在 */
    int MENU_NAME_EXISTS = 70003;
    /** 菜单已被角色引用 */
    int MENU_USED_BY_ROLE = 70004;
    /** 上级菜单选择错误 */
    int MENU_PARENT_ERROR = 70005;

    // ========== 部门模块 ==========

    /** 部门不存在 */
    int DEPT_NOT_FOUND = 80001;
    /** 存在下级部门，不可删除 */
    int DEPT_HAS_CHILDREN = 80002;
    /** 部门下存在用户 */
    int DEPT_HAS_USERS = 80003;
    /** 同级部门名称已存在 */
    int DEPT_NAME_EXISTS = 80004;
    /** 上级部门选择错误 */
    int DEPT_PARENT_ERROR = 80005;

    // ========== 岗位模块 ==========

    /** 岗位不存在 */
    int POST_NOT_FOUND = 85001;
    /** 岗位编码已存在 */
    int POST_CODE_EXISTS = 85002;
    /** 岗位编码已被占用 */
    int POST_CODE_USED = 85003;
    /** 岗位已被用户使用 */
    int POST_USED = 85004;

    // ========== 通用业务 ==========

    /** 数据不存在 */
    int DATA_NOT_FOUND = 90002;
    /** 操作失败 */
    int OPERATION_FAILED = 90003;
    /** 权限不足 */
    int PERMISSION_DENIED = 90004;
    /** 业务处理异常 */
    int BUSINESS_ERROR = 90005;
    /** 请求过于频繁 */
    int REQUEST_TOO_FREQUENT = 90006;
    /** Redis 操作异常 */
    int REDIS_ERROR = 90007;

    // ========== 通知公告模块 ==========

    /** 通知公告不存在 */
    int NOTICE_NOT_FOUND = 100001;
}
