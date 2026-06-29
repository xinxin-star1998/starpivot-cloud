package cn.org.starpivot.common.exception;

/**
 * 业务异常，携带 HTTP 语义错误码与提示信息。
 *
 * @deprecated 请使用 {@link BizException}，本类保留仅为兼容旧代码。
 */
@Deprecated
public class BusinessException extends BizException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String message) {
        super(ErrorCode.SYSTEM_ERROR, message);
    }

    public BusinessException(Integer code, String message) {
        super(code, message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
