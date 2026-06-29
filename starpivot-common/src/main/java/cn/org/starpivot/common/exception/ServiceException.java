package cn.org.starpivot.common.exception;

/**
 * 业务异常。
 *
 * @deprecated 请使用 {@link BizException}，本类保留仅为兼容旧代码。
 */
@Deprecated
public class ServiceException extends BizException {

    private static final long serialVersionUID = 1L;

    private String detailMessage;

    public ServiceException() {
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Integer code) {
        super(code, message);
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public ServiceException setDetailMessage(String detailMessage) {
        this.detailMessage = detailMessage;
        return this;
    }

    @Override
    public ServiceException setMessage(String message) {
        super.setMessage(message);
        return this;
    }
}
