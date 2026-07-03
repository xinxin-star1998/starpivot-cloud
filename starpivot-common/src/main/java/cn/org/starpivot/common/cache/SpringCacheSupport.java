package cn.org.starpivot.common.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * 编程式 Spring Cache 失效，用于注解无法表达精细 key 的场景。
 */
@Component
@RequiredArgsConstructor
public class SpringCacheSupport {

    private final CacheManager cacheManager;

    public void evict(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
