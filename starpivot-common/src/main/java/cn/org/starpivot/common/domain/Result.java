package cn.org.starpivot.common.domain;

import cn.org.starpivot.common.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * 通用响应结果包装类
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功标志
     */
    private int code;

    /**
     * 提示信息
     */
    private String message;

    /**
     * 数据对象
     */
    private T data;

    public Result() {
    }

    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(ErrorCode.SUCCESS, "操作成功", null);
    }

    /**
     * 成功
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS, "操作成功", data);
    }

    /**
     * 成功（仅消息，无数据）。
     * <p>
     * 与 {@link #success(Object)} 通过参数类型区分：
     * 编译器根据目标返回类型推断 {@code T}，消息写入 {@code message} 字段，{@code data} 为 {@code null}。
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ErrorCode.SUCCESS, message, null);
    }

    /**
     * 成功带消息和数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ErrorCode.SUCCESS, message, data);
    }

    /**
     * 失败
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 失败带消息
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ErrorCode.SYSTEM_ERROR, message, null);
    }

    /**
     * 参数错误
     */
    public static <T> Result<T> badRequest(String message) {
        return new Result<>(ErrorCode.PARAM_ERROR, message, null);
    }

    /**
     * 未找到
     */
    public static <T> Result<T> notFound(String message) {
        return new Result<>(ErrorCode.NOT_FOUND, message, null);
    }

    /**
     * 禁止访问
     */
    public static <T> Result<T> forbidden(String message) {
        return new Result<>(ErrorCode.FORBIDDEN, message, null);
    }

    /**
     * 未授权
     */
    public static <T> Result<T> unauthorized(String message) {
        return new Result<>(ErrorCode.UNAUTHORIZED, message, null);
    }

    public boolean isSuccess() {
        return code == ErrorCode.SUCCESS;
    }

    public boolean isFail() {
        return code != ErrorCode.SUCCESS;
    }

    // Getter and setter methods are already provided by @Data annotation
}