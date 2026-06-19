package cn.org.starpivot.system.mapper;

import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.domain.entity.SysDept;
import cn.org.starpivot.system.domain.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    IPage<SysUser> selectPageList(Page<SysUser> page, @Param("param") Map<String, Object> param);

    List<Map<String, Object>> countByMonthRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    List<SysRole> getRolesByUserId(@Param("userId") Long userId);

    SysUser selectUserWithRoles(@Param("userId") Long userId);

    List<SysMenu> getMenuByUserId(@Param("userId") Long userId);

    IPage<SysUser> getUserListByRoleId(Page<SysUser> page, @Param("param") Map<String, Object> param);

    IPage<SysUser> unallocatedList(Page<SysUser> page, @Param("param") Map<String, Object> param);

    Long selectDeptIdByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT r.role_key
            FROM sys_role r
                     INNER JOIN sys_user_role ur ON r.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND r.del_flag = '0'
              AND r.status = '0'
            """)
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT m.menu_id, m.menu_name, m.parent_id, m.order_num, m.path, m.component, m.query,
                   m.route_name, m.is_frame, m.is_cache, m.menu_type, m.visible, m.status, m.perms,
                   m.icon, m.create_by, m.create_time, m.update_by, m.update_time, m.remark
            FROM sys_menu m
                     INNER JOIN sys_role_menu rm ON m.menu_id = rm.menu_id
                     INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id
            WHERE ur.user_id = #{userId}
              AND m.status = '0'
            ORDER BY m.parent_id, m.order_num
            """)
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT m.menu_id, m.menu_name, m.parent_id, m.order_num, m.path, m.component, m.query,
                   m.route_name, m.is_frame, m.is_cache, m.menu_type, m.visible, m.status, m.perms,
                   m.icon, m.create_by, m.create_time, m.update_by, m.update_time, m.remark
            FROM sys_menu m
            WHERE m.status = '0'
            ORDER BY m.parent_id, m.order_num
            """)
    List<SysMenu> selectAllActiveMenus();
}
