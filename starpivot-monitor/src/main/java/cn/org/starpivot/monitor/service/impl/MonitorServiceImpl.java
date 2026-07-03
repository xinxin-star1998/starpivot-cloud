package cn.org.starpivot.monitor.service.impl;

import cn.org.starpivot.monitor.domain.vo.DruidMonitorVO;
import cn.org.starpivot.monitor.domain.vo.OnlineUserVO;
import cn.org.starpivot.monitor.domain.vo.RedisCacheVO;
import cn.org.starpivot.monitor.domain.vo.ServerInfoVO;
import cn.org.starpivot.monitor.service.MonitorService;
import cn.org.starpivot.monitor.service.support.DruidMonitorSupport;
import cn.org.starpivot.monitor.service.support.RedisCacheMonitorSupport;
import cn.org.starpivot.monitor.service.support.ServerMetricsCollector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link MonitorService} 默认实现，聚合服务器、Druid 连接池、在线用户及 Redis 缓存监控能力。
 */
@Service
@RequiredArgsConstructor
public class MonitorServiceImpl implements MonitorService {

    private final ServerMetricsCollector serverMetricsCollector;
    private final DruidMonitorSupport druidMonitorSupport;
    private final RedisCacheMonitorSupport redisCacheMonitorSupport;
    private final CloudOnlineUserService cloudOnlineUserService;

    @Override
    public ServerInfoVO getServerInfo() {
        return serverMetricsCollector.collectServerInfo();
    }

    @Override
    public DruidMonitorVO getDruidMonitorInfo() {
        return druidMonitorSupport.collectDruidMonitorInfo();
    }

    @Override
    public DruidMonitorVO getDruidMonitorInfoWithSlowSql(Long slowSqlThreshold) {
        return druidMonitorSupport.collectDruidMonitorInfoWithSlowSql(slowSqlThreshold);
    }

    @Override
    public List<OnlineUserVO> getOnlineUserList(String userName, String ipaddr) {
        return cloudOnlineUserService.getOnlineUserList(userName, ipaddr);
    }

    @Override
    public boolean forceLogout(String sessionId) {
        return cloudOnlineUserService.forceLogout(sessionId);
    }

    @Override
    public List<RedisCacheVO> getCacheList() {
        return redisCacheMonitorSupport.getCacheList();
    }

    @Override
    public List<RedisCacheVO.CacheKeyInfo> getCacheKeys(String cacheName) {
        return redisCacheMonitorSupport.getCacheKeys(cacheName);
    }

    @Override
    public RedisCacheVO.CacheContentInfo getCacheContent(String cacheName, String key) {
        return redisCacheMonitorSupport.getCacheContent(cacheName, key);
    }

    @Override
    public long deleteCache(String cacheName) {
        return redisCacheMonitorSupport.deleteCache(cacheName);
    }

    @Override
    public boolean deleteCacheKey(String cacheName, String key) {
        return redisCacheMonitorSupport.deleteCacheKey(cacheName, key);
    }

    @Override
    public boolean clearAllCache() {
        return redisCacheMonitorSupport.clearAllCache();
    }
}
