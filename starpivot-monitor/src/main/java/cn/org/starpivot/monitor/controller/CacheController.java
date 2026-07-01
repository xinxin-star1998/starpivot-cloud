package cn.org.starpivot.monitor.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.monitor.domain.vo.RedisCacheVO;
import cn.org.starpivot.monitor.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Redis 缓存监控与管理 REST 接口。
 * <p>
 * {@link RestController}：返回 JSON 响应；
 * {@link RequestMapping}：统一前缀 {@code /monitor/cache}；
 * {@link RequiredArgsConstructor}：注入 {@link MonitorService}。
 */
@RestController
@RequestMapping("/monitor/cache")
@RequiredArgsConstructor
public class CacheController {

    private final MonitorService monitorService;

    /**
     * 查询所有缓存分组及键数量统计。
     *
     * @return 缓存分组列表
     */
    @Log(title = "Redis缓存管理")
    @PreAuthorize("hasAuthority('monitor:cache:query')")
    @GetMapping("/cachePageList")
    public Result<List<RedisCacheVO>> getCacheList() {
        return Result.success(monitorService.getCacheList());
    }

    /**
     * 查询指定缓存分组下的键列表。
     *
     * @param cacheName 缓存分组名称
     * @return 键信息列表
     */
    @Log(title = "Redis缓存管理")
    @PreAuthorize("hasAuthority('monitor:cache:query')")
    @GetMapping("/keys")
    public Result<List<RedisCacheVO.CacheKeyInfo>> getCacheKeys(@RequestParam String cacheName) {
        return Result.success(monitorService.getCacheKeys(cacheName));
    }

    /**
     * 查看指定缓存键的内容。
     *
     * @param cacheName 缓存分组名称
     * @param key       缓存键
     * @return 键值内容及元信息
     */
    @Log(title = "Redis缓存管理")
    @PreAuthorize("hasAuthority('monitor:cache:query')")
    @GetMapping("/content")
    public Result<RedisCacheVO.CacheContentInfo> getCacheContent(
            @RequestParam String cacheName,
            @RequestParam String key) {
        return Result.success(monitorService.getCacheContent(cacheName, key));
    }

    /**
     * 删除指定缓存分组下的全部键。
     *
     * @param cacheName 缓存分组名称
     * @return 删除的键数量
     */
    @Log(title = "删除Redis缓存分组", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('monitor:cache:remove')")
    @DeleteMapping("/group/{cacheName}")
    public Result<Long> deleteCache(@PathVariable String cacheName) {
        long deletedCount = monitorService.deleteCache(cacheName);
        return Result.success("删除成功，共删除 " + deletedCount + " 个键", deletedCount);
    }

    /**
     * 删除指定缓存键。
     *
     * @param cacheName 缓存分组名称
     * @param key       缓存键
     * @return 操作结果
     */
    @Log(title = "删除Redis缓存键", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('monitor:cache:remove')")
    @DeleteMapping("/key")
    public Result<?> deleteCacheKey(@RequestParam String cacheName, @RequestParam String key) {
        boolean success = monitorService.deleteCacheKey(cacheName, key);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    /**
     * 清空全部 Redis 缓存。
     *
     * @return 操作结果
     */
    @Log(title = "清空Redis缓存", businessType = BusinessType.CLEAN)
    @PreAuthorize("hasAuthority('monitor:cache:clear')")
    @DeleteMapping("/clear")
    public Result<?> clearAllCache() {
        boolean success = monitorService.clearAllCache();
        return success ? Result.success("清空成功") : Result.error("清空失败");
    }
}
