package cn.org.starpivot.common.util;

import cn.org.starpivot.common.exception.BusinessException;

/**
 * 断言工具类
 */
public class AssertUtils {

    /**
     * 断言为真
     */
    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言为真并带有错误码
     */
    public static void isTrue(boolean condition, int errorCode, String message) {
        if (!condition) {
            throw new BusinessException(errorCode, message);
        }
    }

    /**
     * 断言为假
     */
    public static void isFalse(boolean condition, String message) {
        if (condition) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言对象不为空
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言对象不为空并带有错误码
     */
    public static void notNull(Object object, int errorCode) {
        if (object == null) {
            throw new BusinessException(errorCode, "参数不能为空");
        }
    }

    /**
     * 断言对象不为空并带有错误码和自定义消息
     */
    public static void notNull(Object object, int errorCode, String message) {
        if (object == null) {
            throw new BusinessException(errorCode, message);
        }
    }

    /**
     * 断言对象为空
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言字符串不为空
     */
    public static void notEmpty(String str, String message) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言字符串为空
     */
    public static void isNull(Object object, int errorCode) {
        if (object != null) {
            throw new cn.org.starpivot.common.exception.BizException(errorCode, "数据已存在");
        }
    }

    public static void isNull(Object object, int errorCode, String message) {
        if (object != null) {
            throw new cn.org.starpivot.common.exception.BizException(errorCode, message);
        }
    }

    public static void notEmpty(java.util.Collection<?> collection, int errorCode) {
        if (collection == null || collection.isEmpty()) {
            throw new cn.org.starpivot.common.exception.BizException(errorCode, "数据不能为空");
        }
    }

    public static void notEmpty(java.util.Collection<?> collection, int errorCode, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new cn.org.starpivot.common.exception.BizException(errorCode, message);
        }
    }
}