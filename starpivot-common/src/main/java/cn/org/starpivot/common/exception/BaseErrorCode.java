package cn.org.starpivot.common.exception;

/**
 * 基础错误码定义
 */
public interface BaseErrorCode {

    /**
     * 成功
     */
    int SUCCESS = 200;

    /**
     * 业务错误
     */
    int BIZ_ERROR = 400;

    /**
     * 未授权
     */
    int UNAUTHORIZED = 401;

    /**
     * 禁止访问
     */
    int FORBIDDEN = 403;

    /**
     * 未找到
     */
    int NOT_FOUND = 404;

    /**
     * 系统错误
     */
    int SYSTEM_ERROR = 500;

    /**
     * 参数错误
     */
    int PARAM_ERROR = 400;

    /**
     * 权限不足
     */
    int PERMISSION_DENIED = 403;
}