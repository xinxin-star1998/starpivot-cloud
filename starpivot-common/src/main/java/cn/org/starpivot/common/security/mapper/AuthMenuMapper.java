package cn.org.starpivot.common.security.mapper;

import cn.org.starpivot.common.security.MenuPermissionAuthorityResolver;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单权限查询（generator / monitor 等服务与 system 共用 sys_menu 表）。
 * <p>
 * 供 {@link MenuPermissionAuthorityResolver} 加载用户菜单 perms 作为 Spring Security authorities。
 * </p>
 */
@Mapper
public interface AuthMenuMapper {

    /**
     * 查询指定用户通过角色关联的有效菜单权限标识。
     *
     * @param userId 用户 ID
     * @return 去重后的 perms 列表
     */
    @Select("""
            SELECT DISTINCT m.perms
            FROM sys_menu m
                     INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
                     INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND m.status = '0'
              AND m.perms IS NOT NULL
              AND m.perms <> ''
            """)
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 查询系统中全部有效菜单权限标识（超级管理员使用）。
     *
     * @return 去重后的 perms 列表
     */
    @Select("""
            SELECT DISTINCT perms
            FROM sys_menu
            WHERE status = '0'
              AND perms IS NOT NULL
              AND perms <> ''
            """)
    List<String> selectAllPermissionStrings();
}
