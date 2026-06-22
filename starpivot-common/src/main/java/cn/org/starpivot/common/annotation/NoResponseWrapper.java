package cn.org.starpivot.common.annotation;

import java.lang.annotation.*;

/**
 * 标记 Controller 方法跳过统一响应包装，直接返回原始类型（如 {@link org.springframework.http.ResponseEntity}）。
 * <p>
 * 典型用于 Excel 导出、文件下载等需自定义 HTTP 头与体的接口；未被标记的方法由全局
 * {@code ResponseBodyAdvice} 包装为 {@link cn.org.starpivot.common.domain.Result}。
 * </p>
 *
 * @see cn.org.starpivot.common.excel.ExcelToolkit
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoResponseWrapper {
}
