package cn.org.starpivot.monitor.controller;

import cn.org.starpivot.common.annotation.Log;
import cn.org.starpivot.common.enums.BusinessType;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.monitor.domain.vo.RedisCacheVO;
import cn.org.starpivot.monitor.service.MonitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/monitor/cache")
@RequiredArgsConstructor
public class CacheController {

    private final MonitorService monitorService;

    @Log(title = "Redis缓存管理")
    @PreAuthorize("hasAuthority('monitor:cache:query')")
    @GetMapping("/list")
    public Result<List<RedisCacheVO>> getCacheList() {
        return Result.success(monitorService.getCacheList());
    }

    @Log(title = "Redis缓存管理")
    @PreAuthorize("hasAuthority('monitor:cache:query')")
    @GetMapping("/keys")
    public Result<List<RedisCacheVO.CacheKeyInfo>> getCacheKeys(@RequestParam String cacheName) {
        return Result.success(monitorService.getCacheKeys(cacheName));
    }

    @Log(title = "Redis缓存管理")
    @PreAuthorize("hasAuthority('monitor:cache:query')")
    @GetMapping("/content")
    public Result<RedisCacheVO.CacheContentInfo> getCacheContent(
            @RequestParam String cacheName,
            @RequestParam String key) {
        return Result.success(monitorService.getCacheContent(cacheName, key));
    }

    @Log(title = "删除Redis缓存分组", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('monitor:cache:remove')")
    @DeleteMapping("/group/{cacheName}")
    public Result<Long> deleteCache(@PathVariable String cacheName) {
        long deletedCount = monitorService.deleteCache(cacheName);
        return Result.success("删除成功，共删除 " + deletedCount + " 个键", deletedCount);
    }

    @Log(title = "删除Redis缓存键", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAuthority('monitor:cache:remove')")
    @DeleteMapping("/key")
    public Result<?> deleteCacheKey(@RequestParam String cacheName, @RequestParam String key) {
        boolean success = monitorService.deleteCacheKey(cacheName, key);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }

    @Log(title = "清空Redis缓存", businessType = BusinessType.CLEAN)
    @PreAuthorize("hasAuthority('monitor:cache:clear')")
    @DeleteMapping("/clear")
    public Result<?> clearAllCache() {
        boolean success = monitorService.clearAllCache();
        return success ? Result.success("清空成功") : Result.error("清空失败");
    }
}
