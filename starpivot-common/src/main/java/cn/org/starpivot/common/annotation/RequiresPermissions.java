package cn.org.starpivot.common.annotation;

import java.lang.annotation.*;

/**
 * 权限认证注解 - 用于验证用户是否具有指定权限
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermissions {
    /**
     * 权限标识
     */
    String[] value() default {};

    /**
     * 逻辑关系，当有多个权限时，AND = 与，OR = 或
     */
    Logical logical() default Logical.OR;
}