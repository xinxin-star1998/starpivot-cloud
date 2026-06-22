package cn.org.starpivot.common.security.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单权限查询（generator / monitor 等服务与 system 共用 sys_menu 表）。
 */
@Mapper
public interface AuthMenuMapper {

    @Select("""
            SELECT DISTINCT m.perms
            FROM sys_menu m
                     INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
                     INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND m.del_flag = '0'
              AND m.status = '0'
              AND m.perms IS NOT NULL
              AND m.perms <> ''
            """)
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT perms
            FROM sys_menu
            WHERE status = '0'
              AND perms IS NOT NULL
              AND perms <> ''
            """)
    List<String> selectAllPermissionStrings();
}
