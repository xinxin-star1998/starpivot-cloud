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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统用户 Mapper 接口。
 * <p>
 * 继承 {@link BaseMapper} 提供基础 CRUD，并定义用户分页、角色/菜单关联及统计的自定义 SQL。
 * </p>
 * <ul>
 *   <li>{@link Mapper} — MyBatis Mapper 组件，由 {@code @MapperScan} 扫描注册</li>
 * </ul>
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 按数据权限过滤的分页查询用户列表。 */
    IPage<SysUser> selectPageList(Page<SysUser> page, @Param("param") Map<String, Object> param);

    /** 按月份统计用户新增数量。 */
    List<Map<String, Object>> countByMonthRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** 查询用户关联的角色列表。 */
    List<SysRole> getRolesByUserId(@Param("userId") Long userId);

    /** 查询用户及其角色信息。 */
    SysUser selectUserWithRoles(@Param("userId") Long userId);

    /** 查询用户拥有的菜单权限列表。 */
    List<SysMenu> getMenuByUserId(@Param("userId") Long userId);

    /** 分页查询已分配指定角色的用户。 */
    IPage<SysUser> getUserListByRoleId(Page<SysUser> page, @Param("param") Map<String, Object> param);

    /** 分页查询未分配指定角色的用户。 */
    IPage<SysUser> unallocatedList(Page<SysUser> page, @Param("param") Map<String, Object> param);

    /** 查询用户所属部门 ID。 */
    Long selectDeptIdByUserId(@Param("userId") Long userId);

    /** 查询用户角色 Key 列表。 */
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    /** 查询用户可访问的菜单列表。 */
    List<SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    /** 查询全部启用状态的菜单。 */
    List<SysMenu> selectAllActiveMenus();
}
