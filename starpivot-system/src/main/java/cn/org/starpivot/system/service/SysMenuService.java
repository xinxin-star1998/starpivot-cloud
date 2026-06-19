package cn.org.starpivot.system.service;

import cn.org.starpivot.system.domain.dto.MenuDTO;
import cn.org.starpivot.system.domain.entity.SysMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> menuTree();

    List<SysMenu> getUserMenuTree(Long userId);

    boolean insertMenu(MenuDTO menuDTO);

    boolean updateMenu(MenuDTO menuDTO);

    boolean deleteMenuByIds(List<Long> menuIds);

    List<SysMenu> getParent();
}
