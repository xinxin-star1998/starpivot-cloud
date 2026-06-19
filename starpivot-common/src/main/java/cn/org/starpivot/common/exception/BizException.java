package cn.org.starpivot.common.exception;

/**
 * 业务异常
 */
public class BizException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误提示
     */
    private String message;

    /**
     * 空构造方法，避免反序列化问题
     */
    public BizException() {
    }

    public BizException(String message) {
        this.message = message;
    }

    public BizException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public BizException(String message, Throwable cause) {
        super(cause);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public BizException setMessage(String message) {
        this.message = message;
        return this;
    }
}