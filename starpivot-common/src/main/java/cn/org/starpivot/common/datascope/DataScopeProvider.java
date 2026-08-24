package cn.org.starpivot.common.datascope;

import cn.org.starpivot.common.entity.DataScope;

/**
 * 数据范围提供者接口，根据用户 ID 解析其角色的数据权限范围。
 * <p>
 * 实现类应查询 {@code sys_role} 表的 {@code data_scope} 字段及
 * {@code sys_role_dept} 关联表，构建完整的 {@link DataScope} 上下文。
 * </p>
 * <p>
 * 默认实现由 {@code starpivot-system} 模块提供；其他模块可通过
 * {@code @ConditionalOnMissingBean} 覆盖或扩展。
 * </p>
 */
@FunctionalInterface
public interface DataScopeProvider {

    /**
     * 解析指定用户的数据权限范围。
     * <p>
     * 若用户拥有多个角色，应按并集合并（任一角色为全部权限则不过滤；
     * 自定义部门、本部门、本部门及以下与仅本人以 OR 组合）。
     * </p>
     *
     * @param userId 用户 ID，不为 {@code null}
     * @return 数据权限上下文，包含 SQL 过滤片段、可见部门列表等；
     *         若用户为超级管理员或拥有全部数据权限，返回的 {@code sqlFilter} 应为 {@code null} 或空字符串
     */
    DataScope resolve(Long userId);
}
