package cn.org.starpivot.common.annotation;

import java.lang.annotation.*;

/**
 * 标记导出等接口直接返回 ResponseEntity，不再包装为 Result。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoResponseWrapper {
}
