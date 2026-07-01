package cn.org.starpivot.system.service;

import cn.org.starpivot.system.domain.bo.RouterVo;

import java.util.List;

/**
 * 前端动态路由构建服务。
 */
public interface SysRouterService {

    /**
     * 根据用户 ID 构建 Vue Router 动态路由树。
     */
    List<RouterVo> buildDynamicRoutes(Long userId);
}
