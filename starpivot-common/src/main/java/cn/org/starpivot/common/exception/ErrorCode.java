package cn.org.starpivot.common.exception;

/**
 * 错误码定义
 */
public interface ErrorCode extends BaseErrorCode {

    // ========== 通用错误 ==========
    int SUCCESS = 200;
    int SYSTEM_ERROR = 500;
    int PARAM_ERROR = 400;
    int UNAUTHORIZED = 401;
    int FORBIDDEN = 403;
    int NOT_FOUND = 404;
    int METHOD_NOT_ALLOWED = 405;
    int REQUEST_TIMEOUT = 408;
    int VALIDATE_ERROR = 422;
    int BIZ_ERROR = 501;
    int PARAM_INVALID = 90005;

    // ========== 字典模块 ==========
    int DICT_NOT_FOUND = 40001;
    int DICT_TYPE_EXISTS = 40002;
    int DICT_DATA_EXISTS = 40003;
    int DICT_TYPE_NOT_FOUND = 40004;

    // ========== 用户模块 ==========
    int USER_NOT_FOUND = 50001;
    int USER_EXISTS = 50002;
    int USER_PASSWORD_ERROR = 50003;
    int USER_ACCOUNT_LOCKED = 50004;
    int USER_USERNAME_EXISTS = 50005;
    int USER_USERNAME_USED = 50006;
    int USER_IMPORT_EMPTY = 50007;
    int USER_IMPORT_ROW_EMPTY = 50008;
    int USER_IMPORT_USERNAME_EMPTY = 50009;
    int USER_IMPORT_NICKNAME_EMPTY = 50010;

    // ========== 角色模块 ==========
    int ROLE_NOT_FOUND = 60001;
    int ROLE_EXISTS = 60002;
    int ROLE_KEY_EXISTS = 60003;
    int ROLE_KEY_USED = 60004;
    int ROLE_USED = 60005;
    int ROLE_DELETED = 60006;
    int ROLE_DISABLED = 60007;
    int ROLE_ADMIN_PROTECTED = 60008;

    // ========== 菜单模块 ==========
    int MENU_NOT_FOUND = 70001;
    int MENU_HAS_CHILDREN = 70002;
    int MENU_NAME_EXISTS = 70003;
    int MENU_USED_BY_ROLE = 70004;
    int MENU_PARENT_ERROR = 70005;

    // ========== 部门模块 ==========
    int DEPT_NOT_FOUND = 80001;
    int DEPT_HAS_CHILDREN = 80002;
    int DEPT_HAS_USERS = 80003;
    int DEPT_NAME_EXISTS = 80004;
    int DEPT_PARENT_ERROR = 80005;

    // ========== 岗位模块 ==========
    int POST_NOT_FOUND = 85001;
    int POST_CODE_EXISTS = 85002;
    int POST_CODE_USED = 85003;
    int POST_USED = 85004;

    // ========== 通用错误 ==========
    int DATA_NOT_FOUND = 90002;
    int OPERATION_FAILED = 90003;
    int PERMISSION_DENIED = 90004;
    int BUSINESS_ERROR = 90005;
    int REQUEST_TOO_FREQUENT = 90006;
    int REDIS_ERROR = 90007;
}