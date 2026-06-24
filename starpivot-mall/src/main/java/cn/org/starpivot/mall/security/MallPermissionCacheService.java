package cn.org.starpivot.mall.security;

import cn.org.starpivot.common.cache.CacheConstants;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 商城 B 端权限缓存，与 system 模块 {@code UserPermissionCacheService} 策略一致。
 */
@Service
@RequiredArgsConstructor
public class MallPermissionCacheService {

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
        if (!safeList.isEmpty()) {
            redisTemplate.opsForValue().set(key, safeList, TTL);
        }
        return safeList;
    }
}
