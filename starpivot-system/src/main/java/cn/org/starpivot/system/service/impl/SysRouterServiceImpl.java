package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.system.domain.bo.MetaVo;
import cn.org.starpivot.system.domain.bo.RouterVo;
import cn.org.starpivot.system.domain.entity.SysMenu;
import cn.org.starpivot.system.service.SysMenuService;
import cn.org.starpivot.system.service.SysRouterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRouterServiceImpl implements SysRouterService {

    private final SysMenuService sysMenuService;

    @Override
    public List<RouterVo> buildDynamicRoutes(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        List<SysMenu> menuTree = sysMenuService.getUserMenuTree(userId);
        if (CollectionUtils.isEmpty(menuTree)) {
            return Collections.emptyList();
        }
        return buildRouterTree(menuTree);
    }

    private List<RouterVo> buildRouterTree(List<SysMenu> menuTree) {
        List<RouterVo> routerTree = new ArrayList<>();
        for (SysMenu menu : menuTree) {
            RouterVo router = convertMenuToRouter(menu);
            if (!CollectionUtils.isEmpty(menu.getChildren())) {
                router.setChildren(buildRouterTree(menu.getChildren()));
            }
            routerTree.add(router);
        }
        return routerTree;
    }

    private RouterVo convertMenuToRouter(SysMenu menu) {
        RouterVo router = new RouterVo();
        router.setMenuId(menu.getMenuId());
        router.setPerms(menu.getPerms());
        router.setMenuType(menu.getMenuType());
        router.setIsFrame(menu.getIsFrame());
        router.setName(menu.getRouteName());
        router.setPath(menu.getPath());
        router.setHidden("1".equals(menu.getVisible()));
        router.setComponent(menu.getComponent());
        router.setQuery(menu.getQuery());

        MetaVo meta = new MetaVo();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(menu.getIcon());
        meta.setNoCache(menu.getIsCache() != null && menu.getIsCache() == 1);

        if (menu.getIsFrame() != null && menu.getIsFrame() == 0 && menu.getPath() != null
                && (menu.getPath().startsWith("http://") || menu.getPath().startsWith("https://"))) {
            meta.setLink(menu.getPath());
        }

        router.setMeta(meta);
        return router;
    }
}
