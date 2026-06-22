package cn.org.starpivot.common.exception;

/**
 * 业务异常，携带 HTTP 语义错误码与提示信息。
 * <p>
 * 由 {@link GlobalExceptionHandler} 捕获并转换为 {@link cn.org.starpivot.common.domain.Result} 响应；
 * 与 {@link BizException}、{@link ErrorCode} 配合使用。
 * </p>
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码，默认 500 */
    private Integer code;

    /** 面向用户的错误提示 */
    private String message;

    /**
     * 使用默认错误码 500 构造。
     *
     * @param message 错误提示
     */
    public BusinessException(String message) {
        this.code = 500;
        this.message = message;
    }

    /**
     * 指定错误码与提示信息。
     *
     * @param code    业务错误码，可参考 {@link ErrorCode}
     * @param message 错误提示
     */
    public BusinessException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 携带根因的构造方法，错误码默认 500。
     *
     * @param message 错误提示
     * @param cause   原始异常
     */
    public BusinessException(String message, Throwable cause) {
        super(cause);
        this.code = 500;
        this.message = message;
    }

    /** @return 业务错误码 */
    public Integer getCode() {
        return code;
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return message;
    }
}
