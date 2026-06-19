package cn.org.starpivot.common.util;

import java.util.Collection;
import java.util.Map;

/**
 * 通用工具类
 */
public class CommonUtils {

    /**
     * 判断对象是否为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof CharSequence) {
            return ((CharSequence) obj).length() == 0;
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }
        if (obj.getClass().isArray()) {
            return java.lang.reflect.Array.getLength(obj) == 0;
        }
        return false;
    }

    /**
     * 判断对象是否非空
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 检查对象是否为数字
     */
    public static boolean isNumeric(Object obj) {
        if (obj == null) {
            return false;
        }
        try {
            Double.parseDouble(obj.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 转换为字符串
     */
    public static String toStr(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    /**
     * 转换为整数
     */
    public static Integer toInt(Object obj) {
        return obj == null ? 0 : Integer.valueOf(obj.toString());
    }

    /**
     * 转换为长整数
     */
    public static Long toLong(Object obj) {
        return obj == null ? 0L : Long.valueOf(obj.toString());
    }

    /**
     * 转换为双精度浮点数
     */
    public static Double toDouble(Object obj) {
        return obj == null ? 0.0 : Double.valueOf(obj.toString());
    }
}