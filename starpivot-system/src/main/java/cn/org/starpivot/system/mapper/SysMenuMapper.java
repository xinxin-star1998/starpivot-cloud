package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统菜单 Mapper 接口。
 * <p>
 * 提供菜单权限字符串查询及角色-菜单关联的自定义 SQL。
 * </p>
 */
@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    /** 查询用户拥有的菜单权限标识列表。 */
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /** 查询全部菜单权限标识（超级管理员用）。 */
    List<String> selectAllPermissionStrings();

    /** 查询角色已分配的菜单 ID 列表。 */
    List<Long> selectMenuIds(@Param("roleId") Long roleId);

    /** 查询角色可访问的菜单列表。 */
    List<SysMenu> getMenuByRoleId(@Param("roleId") Long roleId);
}
