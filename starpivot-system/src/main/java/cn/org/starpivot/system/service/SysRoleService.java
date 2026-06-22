package cn.org.starpivot.system.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.system.domain.dto.RoleDTO;
import cn.org.starpivot.system.domain.dto.RolePermissionAssignDTO;
import cn.org.starpivot.system.domain.dto.RoleQueryDTO;
import cn.org.starpivot.system.domain.dto.UserRoleDTO;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.domain.entity.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色管理服务接口。
 * <p>
 * 提供角色 CRUD、菜单/数据权限分配及用户授权管理。
 * </p>
 */
public interface SysRoleService extends IService<SysRole> {

    /** 分页查询角色列表。 */
    PageResponse<SysRole> selectRoleList(RoleQueryDTO roleQueryDTO);

    /** 根据角色 ID 查询角色详情。 */
    SysRole selectRoleById(Long roleId);

    /** 查询全部角色（下拉选用）。 */
    List<SysRole> selectAllRoles();

    /** 新增角色。 */
    boolean insertRole(RoleDTO roleDTO);

    /** 修改角色信息。 */
    boolean updateRole(RoleDTO roleDTO);

    /** 批量删除角色。 */
    boolean deleteRoleByIds(List<Long> roleIds);

    /** 修改角色启用/停用状态。 */
    boolean changeRoleStatus(Long roleId, String status);

    /** 查询角色关联的部门 ID（数据权限）。 */
    List<Long> selectDeptIdsByRoleId(Long roleId);

    /** 为角色分配菜单与数据权限。 */
    boolean assignPermission(RolePermissionAssignDTO rolePermissionAssignDTO);

    /** 查询角色已分配的菜单 ID 列表。 */
    List<Long> getMenuIdsByRoleId(Long roleId);

    /** 为角色批量分配用户。 */
    boolean assignUser(UserRoleDTO userRoleDTO);

    /** 取消角色对用户的授权。 */
    boolean cancelUser(UserRole userRole);
}
