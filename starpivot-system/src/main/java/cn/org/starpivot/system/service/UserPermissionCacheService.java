package cn.org.starpivot.system.service;

import cn.org.starpivot.common.cache.CacheConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class UserPermissionCacheService {

    private static final Duration TTL = CacheConstants.TTL_USER_PERMISSIONS;

    private final RedisTemplate<String, Object> redisTemplate;

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

    public void clearAllUserPermissionCache() {
        Set<String> keys = redisTemplate.keys(CacheConstants.userPermissionPattern());
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    private static String cacheKey(Long userId) {
        return CacheConstants.userPermissionKey(userId);
    }
}
