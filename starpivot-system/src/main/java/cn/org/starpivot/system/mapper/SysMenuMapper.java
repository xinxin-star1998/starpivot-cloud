package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

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

    @Select("""
            SELECT DISTINCT perms
            FROM sys_menu
            WHERE status = '0'
              AND perms IS NOT NULL
              AND perms <> ''
            """)
    List<String> selectAllPermissionStrings();

    List<Long> selectMenuIds(@Param("roleId") Long roleId);

    List<SysMenu> getMenuByRoleId(@Param("roleId") Long roleId);
}