package cn.org.starpivot.system.service;

import cn.org.starpivot.common.cache.CacheConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 用户权限缓存服务类。
 * <p>
 * 使用 Redis 缓存用户菜单权限字符串列表，减少重复数据库查询；
 * 角色/菜单变更时需调用清除方法使缓存失效。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务组件</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class UserPermissionCacheService {

    private static final Duration TTL = CacheConstants.TTL_USER_PERMISSIONS;

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取用户权限字符串列表，缓存未命中时通过 loader 加载并写入 Redis。
     *
     * @param userId 用户主键
     * @param loader 缓存未命中时的数据库加载函数
     * @return 权限标识列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getPermissionStrings(Long userId, Supplier<List<String>> loader) {
        if (userId == null) {
            return List.of();
        }
        String key = cacheKey(userId);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof List<?> list) {
            return (List<String>) list;
        }
        List<String> permissions = loader.get();
        List<String> safeList = permissions != null ? permissions : List.of();
        if (!safeList.isEmpty()) {
            redisTemplate.opsForValue().set(key, safeList, TTL);
        }
        return safeList;
    }

    /**
     * 清除指定用户的权限缓存。
     *
     * @param userId 用户主键
     */
    public void clearUserPermissionCacheByUserId(Long userId) {
        if (userId == null) {
            return;
        }
        redisTemplate.delete(cacheKey(userId));
    }

    /** @deprecated 请使用 {@link #clearUserPermissionCacheByUserId(Long)} */
    @Deprecated
    public void clearUserPermissionCache(String username) {
        clearAllUserPermissionCache();
    }

    /**
     * 清除全部用户权限缓存（按 Redis Key 模式 SCAN 批量删除）。
     */
    public void clearAllUserPermissionCache() {
        Set<String> keys = scanKeys(CacheConstants.userPermissionPattern());
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        return keys;
    }

    private static String cacheKey(Long userId) {
        return CacheConstants.userPermissionKey(userId);
    }
}
