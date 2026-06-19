package cn.org.starpivot.common;

import java.lang.annotation.*;

/**
 * 简化版 PreAuthorize 注解，用于权限控制
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface PreAuthorize {
    /**
     * 需要的权限表达式
     */
    String value() default "";
}