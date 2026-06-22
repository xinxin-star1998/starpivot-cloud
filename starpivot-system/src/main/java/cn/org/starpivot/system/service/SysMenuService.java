package cn.org.starpivot.system.service;

import cn.org.starpivot.system.domain.dto.MenuDTO;
import cn.org.starpivot.system.domain.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 菜单管理服务接口。
 * <p>
 * 提供系统菜单树维护及按用户权限过滤的菜单查询。
 * </p>
 */
public interface SysMenuService extends IService<SysMenu> {

    /** 查询完整菜单树（管理端）。 */
    List<SysMenu> menuTree();

    /** 根据用户 ID 查询其有权限的菜单树。 */
    List<SysMenu> getUserMenuTree(Long userId);

    /** 新增菜单节点。 */
    boolean insertMenu(MenuDTO menuDTO);

    /** 修改菜单信息。 */
    boolean updateMenu(MenuDTO menuDTO);

    /** 批量删除菜单。 */
    boolean deleteMenuByIds(List<Long> menuIds);

    /** 查询可作为父级的菜单列表。 */
    List<SysMenu> getParent();
}
