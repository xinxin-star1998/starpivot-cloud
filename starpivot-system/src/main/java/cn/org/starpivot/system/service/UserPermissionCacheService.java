package cn.org.starpivot.system.service;

import org.springframework.stereotype.Service;

@Service
public class UserPermissionCacheService {
    public void clearUserPermissionCache(String username) {
        // P0: 预留缓存清理扩展点
    }

    public void clearAllUserPermissionCache() {
        // P0: 预留缓存清理扩展点
    }
}
