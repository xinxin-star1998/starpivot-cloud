package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.system.domain.dto.MenuDTO;
import cn.org.starpivot.system.domain.entity.RoleMenu;
import cn.org.starpivot.system.domain.entity.SysMenu;
import cn.org.starpivot.system.domain.entity.SysRole;
import cn.org.starpivot.system.mapper.RoleMenuMapper;
import cn.org.starpivot.system.mapper.SysMenuMapper;
import cn.org.starpivot.system.service.SysMenuService;
import cn.org.starpivot.system.service.SysUserService;
import cn.org.starpivot.system.service.UserPermissionCacheService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service("sysMenuService")
@RequiredArgsConstructor
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    private static final Long ROOT_PARENT_ID = 0L;
    private static final Long TOP_MENU_PARENT_ID = -1L;

    private final SysUserService sysUserService;
    private final RoleMenuMapper roleMenuMapper;
    private final UserPermissionCacheService userPermissionCacheService;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "menuTree", key = "'all'")
    public List<SysMenu> menuTree() {
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenu::getStatus, AppConstants.Status.NORMAL);
        queryWrapper.orderByAsc(SysMenu::getOrderNum);
        List<SysMenu> allMenu = this.list(queryWrapper);
        return buildMenuTree(allMenu, ROOT_PARENT_ID);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysMenu> getUserMenuTree(Long userId) {
        List<SysRole> roles = sysUserService.getRolesByUserId(userId);

        List<SysMenu> allMenu;
        boolean isAdmin = roles.stream().anyMatch(role -> AppConstants.ADMIN_ROLE_KEY.equals(role.getRoleKey()));
        boolean hasAllDataScope = roles.stream()
                .anyMatch(role -> AppConstants.DataScope.ALL.equals(role.getDataScope()));

        if (isAdmin || hasAllDataScope) {
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysMenu::getStatus, AppConstants.Status.NORMAL)
                    .orderByAsc(SysMenu::getOrderNum);
            allMenu = this.list(queryWrapper);
        } else {
            allMenu = sysUserService.getMenuByUserId(userId);
            if (allMenu == null) {
                allMenu = Collections.emptyList();
            }
        }

        mergeButtonPermsToParent(allMenu);
        return buildUserMenuTree(allMenu, ROOT_PARENT_ID);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "menuTree", allEntries = true)
    public boolean insertMenu(MenuDTO menuDTO) {
        if (!checkMenuNameUnique(menuDTO.getMenuName(), menuDTO.getParentId(), null)) {
            throw new BizException(ErrorCode.MENU_NAME_EXISTS, "菜单名称已存在");
        }

        SysMenu menu = new SysMenu();
        BeanUtils.copyProperties(menuDTO, menu);
        menu.setParentId(menuDTO.getParentId() != null ? menuDTO.getParentId() : ROOT_PARENT_ID);
        menu.setOrderNum(menuDTO.getOrderNum() != null ? menuDTO.getOrderNum() : 0);
        menu.setIsFrame(menuDTO.getIsFrame() != null ? menuDTO.getIsFrame() : 1);
        menu.setIsCache(menuDTO.getIsCache() != null ? menuDTO.getIsCache() : 0);
        menu.setVisible(StringUtils.hasText(menuDTO.getVisible()) ? menuDTO.getVisible() : AppConstants.Visible.SHOW);
        menu.setStatus(StringUtils.hasText(menuDTO.getStatus()) ? menuDTO.getStatus() : AppConstants.Status.NORMAL);

        String currentUser = SecurityContextUtils.getUsername();
        menu.setCreateBy(currentUser);
        menu.setCreateTime(LocalDateTime.now());

        boolean result = this.save(menu);
        if (result) {
            userPermissionCacheService.clearAllUserPermissionCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "menuTree", allEntries = true)
    public boolean updateMenu(MenuDTO menuDTO) {
        SysMenu menu = this.getById(menuDTO.getMenuId());
        if (menu == null) {
            throw new BizException(ErrorCode.MENU_NOT_FOUND, "菜单不存在");
        }

        if (menuDTO.getParentId() != null && menuDTO.getParentId().equals(menuDTO.getMenuId())) {
            throw new BizException(ErrorCode.MENU_PARENT_ERROR, "不能将父菜单设置为自己的子菜单");
        }

        if (!checkMenuNameUnique(menuDTO.getMenuName(), menuDTO.getParentId(), menuDTO.getMenuId())) {
            throw new BizException(ErrorCode.MENU_NAME_EXISTS, "菜单名称已存在");
        }

        BeanUtils.copyProperties(menuDTO, menu, "menuId");
        String currentUser = SecurityContextUtils.getUsername();
        menu.setUpdateBy(currentUser);
        menu.setUpdateTime(LocalDateTime.now());

        boolean result = this.updateById(menu);
        if (result) {
            userPermissionCacheService.clearAllUserPermissionCache();
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "menuTree", allEntries = true)
    public boolean deleteMenuByIds(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return false;
        }

        for (Long menuId : menuIds) {
            LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysMenu::getParentId, menuId);
            if (this.count(wrapper) > 0) {
                throw new BizException(ErrorCode.MENU_HAS_CHILDREN, "菜单ID " + menuId + " 存在子菜单，不允许删除");
            }

            LambdaQueryWrapper<RoleMenu> roleMenuWrapper = new LambdaQueryWrapper<>();
            roleMenuWrapper.eq(RoleMenu::getMenuId, menuId);
            if (roleMenuMapper.selectCount(roleMenuWrapper) > 0) {
                throw new BizException(ErrorCode.MENU_USED_BY_ROLE, "菜单ID " + menuId + " 已被角色使用，不允许删除");
            }
        }

        boolean result = this.removeByIds(menuIds);
        if (result) {
            userPermissionCacheService.clearAllUserPermissionCache();
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<SysMenu> getParent() {
        List<String> menuTypes = Arrays.asList(
                AppConstants.MenuType.CATALOG,
                AppConstants.MenuType.MENU
        );
        QueryWrapper<SysMenu> query = new QueryWrapper<>();
        query.lambda().in(SysMenu::getMenuType, menuTypes).orderByAsc(SysMenu::getOrderNum);
        List<SysMenu> menuList = this.baseMapper.selectList(query);

        SysMenu topMenu = new SysMenu();
        topMenu.setMenuName("顶级菜单");
        topMenu.setLabel("顶级菜单");
        topMenu.setParentId(TOP_MENU_PARENT_ID);
        topMenu.setMenuId(ROOT_PARENT_ID);
        topMenu.setValue(ROOT_PARENT_ID);
        menuList.add(topMenu);

        return buildMenuTreeWithLabelValue(menuList, TOP_MENU_PARENT_ID);
    }

    private boolean checkMenuNameUnique(String menuName, Long parentId, Long menuId) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getMenuName, menuName)
                .eq(SysMenu::getParentId, parentId != null ? parentId : ROOT_PARENT_ID);
        if (menuId != null) {
            wrapper.ne(SysMenu::getMenuId, menuId);
        }
        return this.count(wrapper) == 0;
    }

    private List<SysMenu> buildMenuTree(List<SysMenu> allMenu, long parentId) {
        if (allMenu == null || allMenu.isEmpty()) {
            return Collections.emptyList();
        }

        List<SysMenu> tree = new ArrayList<>();
        List<SysMenu> children = allMenu.stream()
                .filter(menu -> menu != null && menu.getParentId() != null && menu.getParentId().equals(parentId))
                .toList();

        for (SysMenu menu : children) {
            menu.setLabel(menu.getMenuName());
            menu.setValue(menu.getMenuId());
            List<SysMenu> childTree = buildMenuTree(allMenu, menu.getMenuId());
            menu.setChildren(childTree);
            tree.add(menu);
        }
        return tree;
    }

    private List<SysMenu> buildUserMenuTree(List<SysMenu> allMenu, long parentId) {
        if (allMenu == null || allMenu.isEmpty()) {
            return Collections.emptyList();
        }
        List<SysMenu> tree = new ArrayList<>();
        List<SysMenu> children = allMenu.stream()
                .filter(menu -> menu != null
                        && menu.getParentId() != null
                        && menu.getParentId().equals(parentId)
                        && !AppConstants.MenuType.BUTTON.equals(menu.getMenuType()))
                .toList();

        for (SysMenu menu : children) {
            menu.setLabel(menu.getMenuName());
            menu.setValue(menu.getMenuId());
            List<SysMenu> childTree = buildUserMenuTree(allMenu, menu.getMenuId());
            if (!childTree.isEmpty()) {
                menu.setChildren(childTree);
            }
            tree.add(menu);
        }
        return tree;
    }

    private List<SysMenu> buildMenuTreeWithLabelValue(List<SysMenu> menuList, long parentId) {
        List<SysMenu> tree = new ArrayList<>();

        Optional.ofNullable(menuList).orElse(Collections.emptyList())
                .stream()
                .filter(item -> item != null && item.getParentId() != null && item.getParentId().equals(parentId))
                .forEach(item -> {
                    SysMenu menu = new SysMenu();
                    BeanUtils.copyProperties(item, menu);
                    menu.setLabel(item.getMenuName());
                    menu.setValue(item.getMenuId());
                    List<SysMenu> children = buildMenuTreeWithLabelValue(menuList, item.getMenuId());
                    menu.setChildren(children);
                    tree.add(menu);
                });

        return tree;
    }

    private void mergeButtonPermsToParent(List<SysMenu> allMenu) {
        if (allMenu == null || allMenu.isEmpty()) {
            return;
        }

        var buttonPermsByParent = new java.util.HashMap<Long, Set<String>>();

        for (SysMenu menu : allMenu) {
            if (menu == null || !AppConstants.MenuType.BUTTON.equals(menu.getMenuType())) {
                continue;
            }
            Long parentId = menu.getParentId();
            if (parentId == null || !StringUtils.hasText(menu.getPerms())) {
                continue;
            }
            Set<String> permSet = buttonPermsByParent.computeIfAbsent(parentId, k -> new LinkedHashSet<>());
            for (String perm : menu.getPerms().split(",")) {
                if (StringUtils.hasText(perm)) {
                    permSet.add(perm.trim());
                }
            }
        }

        if (buttonPermsByParent.isEmpty()) {
            return;
        }

        for (SysMenu menu : allMenu) {
            if (menu == null || AppConstants.MenuType.BUTTON.equals(menu.getMenuType()) || menu.getMenuId() == null) {
                continue;
            }
            Set<String> childPerms = buttonPermsByParent.get(menu.getMenuId());
            if (childPerms == null || childPerms.isEmpty()) {
                continue;
            }
            Set<String> merged = new LinkedHashSet<>(childPerms);
            if (StringUtils.hasText(menu.getPerms())) {
                for (String perm : menu.getPerms().split(",")) {
                    if (StringUtils.hasText(perm)) {
                        merged.add(perm.trim());
                    }
                }
            }
            menu.setPerms(String.join(",", merged));
        }
    }
}
