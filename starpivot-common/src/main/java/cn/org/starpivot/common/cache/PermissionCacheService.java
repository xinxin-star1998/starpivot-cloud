package cn.org.starpivot.common.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * 用户菜单权限 Redis 缓存（各微服务共用）。
 */
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private static final Duration TTL = CacheConstants.TTL_USER_PERMISSIONS;

    private final RedisTemplate<String, Object> redisTemplate;

    @SuppressWarnings("unchecked")
    public List<String> getPermissionStrings(Long userId, Supplier<List<String>> loader) {
        if (userId == null) {
            return List.of();
        }
        String key = CacheConstants.userPermissionKey(userId);
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof List<?> list) {
            return (List<String>) list;
        }
        List<String> permissions = loader.get();
        List<String> safeList = permissions != null ? permissions : List.of();
        // 空权限也缓存，避免无菜单用户反复打库（类似缓存穿透）
        redisTemplate.opsForValue().set(key, safeList, TTL);
        return safeList;
    }
}
