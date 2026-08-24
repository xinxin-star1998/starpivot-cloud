package cn.org.starpivot.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解，标注在 Mapper 方法上以启用数据范围过滤。
 * <p>
 * 当拦截器检测到 Mapper 方法标注此注解时，会根据当前登录用户角色的
 * 数据范围动态拼接 WHERE 条件。多角色按并集过滤；全部数据权限不加条件。
 * 数据范围不授予菜单或按钮权限。
 * </p>
 * <p>支持的 data_scope 值：</p>
 * <ul>
 *   <li>{@code 1} — 全部数据权限，不追加过滤条件</li>
 *   <li>{@code 2} — 自定义数据权限，按 {@code sys_role_dept} 配置的部门过滤</li>
 *   <li>{@code 3} — 本部门数据权限</li>
 *   <li>{@code 4} — 本部门及子部门数据权限</li>
 *   <li>{@code 5} — 仅本人数据权限</li>
 * </ul>
 *
 * <pre>{@code
 * @DataPermission(deptAlias = "u.dept_id", userAlias = "u.user_id")
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
     * 例如 {@code "dept_id"} 或 {@code "u.dept_id"}，需与 SQL 中的表别名匹配。
     * </p>
     *
     * @return 部门字段名
     */
    String deptAlias() default "dept_id";

    /**
     * 用户字段别名，用于仅本人或「部门 OR 本人」时拼接条件。
     * <p>
     * 例如 {@code "u.user_id"}。须与主键或属主用户字段一致，不要使用 {@code create_by} 用户名。
     * </p>
     *
     * @return 用户字段名
     */
    String userAlias() default "create_by";
}
