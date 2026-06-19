package cn.org.starpivot.system.service;

import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.system.domain.dto.RoleDTO;
import cn.org.starpivot.system.domain.dto.RolePermissionAssignDTO;
import cn.org.starpivot.system.domain.dto.RoleQueryDTO;
import cn.org.starpivot.system.domain.dto.UserRoleDTO;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.domain.entity.UserRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysRoleService extends IService<SysRole> {

    PageResponse<SysRole> selectRoleList(RoleQueryDTO roleQueryDTO);

    SysRole selectRoleById(Long roleId);

    List<SysRole> selectAllRoles();

    boolean insertRole(RoleDTO roleDTO);

    boolean updateRole(RoleDTO roleDTO);

    boolean deleteRoleByIds(List<Long> roleIds);

    boolean changeRoleStatus(Long roleId, String status);

    List<Long> selectDeptIdsByRoleId(Long roleId);

    boolean assignPermission(RolePermissionAssignDTO rolePermissionAssignDTO);

    List<Long> getMenuIdsByRoleId(Long roleId);

    boolean assignUser(UserRoleDTO userRoleDTO);

    boolean cancelUser(UserRole userRole);
}
