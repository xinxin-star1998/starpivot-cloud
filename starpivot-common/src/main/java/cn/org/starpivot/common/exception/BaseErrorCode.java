package cn.org.starpivot.common.exception;

/**
 * 基础错误码定义 — 仅保留 HTTP 语义码。
 * <p>
 * 业务错误码请统一使用 {@link ErrorCode} 中按模块分段的定义。
 * </p>
 */
public interface BaseErrorCode {

    /**
     * 成功
     */
    int SUCCESS = 200;

    /**
     * 参数错误
     */
    int PARAM_ERROR = 400;

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
}