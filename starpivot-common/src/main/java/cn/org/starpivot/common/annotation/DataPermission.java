package cn.org.starpivot.common.annotation;

import java.lang.annotation.*;

/**
 * 数据权限注解，标注在 Mapper 方法上以启用数据范围过滤。
 * <p>
 * 当拦截器检测到 Mapper 方法标注此注解时，会根据当前登录用户角色的
 * {@code data_scope} 字段动态拼接 WHERE 条件，实现数据权限隔离。
 * </p>
 * <p>支持的 data_scope 值：</p>
 * <ul>
 *   <li>{@code 1} — 全部数据权限（超级管理员），不追加过滤条件</li>
 *   <li>{@code 2} — 自定义数据权限，按 {@code sys_role_dept} 配置的部门过滤</li>
 *   <li>{@code 3} — 本部门数据权限</li>
 *   <li>{@code 4} — 本部门及子部门数据权限</li>
 *   <li>{@code 5} — 仅本人数据权限</li>
 * </ul>
 *
 * <pre>{@code
 * // 示例：在 Mapper 方法上标注
 * @DataPermission(deptAlias = "d.dept_id", userAlias = "u.user_id")
 * IPage<SysUser> selectPageList(Page<SysUser> page, @Param("param") Map<String, Object> param);
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataPermission {

    /**
     * 部门字段别名，用于拼接 SQL 条件。
     * <p>
     * 例如 {@code "dept_id"} 或 {@code "d.dept_id"}，需与 SQL 中的表别名匹配。
     * </p>
     *
     * @return 部门字段名
     */
    String deptAlias() default "dept_id";

    /**
     * 用户字段别名，用于 {@code data_scope=5}（仅本人）时拼接条件。
     * <p>
     * 例如 {@code "create_by"} 或 {@code "u.user_id"}。
     * </p>
     *
     * @return 用户字段名
     */
    String userAlias() default "create_by";
}
