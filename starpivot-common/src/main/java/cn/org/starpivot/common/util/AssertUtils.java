package cn.org.starpivot.common.util;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;

/**
 * 断言工具类
 */
public class AssertUtils {

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new BizException(ErrorCode.PARAM_INVALID, message);
        }
    }

    public static void isTrue(boolean condition, int errorCode, String message) {
        if (!condition) {
            throw new BizException(errorCode, message);
        }
    }

    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new BizException(ErrorCode.PARAM_INVALID, message);
        }
    }

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, message);
        }
    }

    public static void notNull(Object object, int errorCode) {
        if (object == null) {
            throw new BizException(errorCode, "参数不能为空");
        }
    }

    public static void notNull(Object object, int errorCode, String message) {
        if (object == null) {
            throw new BizException(errorCode, message);
        }
    }

    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new BizException(ErrorCode.PARAM_INVALID, message);
        }
    }

    public static void notEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, message);
        }
    }

    public static void isNull(Object object, int errorCode) {
        if (object != null) {
            throw new BizException(errorCode, "数据已存在");
        }
    }

    public static void isNull(Object object, int errorCode, String message) {
        if (object != null) {
            throw new BizException(errorCode, message);
        }
    }

    public static void notEmpty(java.util.Collection<?> collection, int errorCode) {
        if (collection == null || collection.isEmpty()) {
            throw new BizException(errorCode, "数据不能为空");
        }
    }

    public static void notEmpty(java.util.Collection<?> collection, int errorCode, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new BizException(errorCode, message);
        }
    }
}
