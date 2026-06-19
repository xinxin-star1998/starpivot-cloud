package cn.org.starpivot.monitor.service;

import cn.org.starpivot.common.domain.PageResponse;
import cn.org.starpivot.monitor.domain.vo.DruidMonitorVO;

import cn.org.starpivot.monitor.domain.vo.OnlineUserVO;
import cn.org.starpivot.monitor.domain.vo.RedisCacheVO;
import cn.org.starpivot.monitor.domain.vo.ServerInfoVO;
import java.util.List;

/**
 * 监控服务接口
 *
 * @author xinxin
 * @since 2026-01-25
 */
public interface MonitorService {

    /**
     * 获取服务器信息
     *
     * @return 服务器信息
     */
    ServerInfoVO getServerInfo();

    /**
     * 获取 Druid 监控信息
     *
     * @return Druid 监控信息
     */
    DruidMonitorVO getDruidMonitorInfo();

    /**
     * 获取 Druid 监控信息（包含慢SQL列表）
     *
     * @param slowSqlThreshold 慢SQL阈值（毫秒）
     * @return Druid 监控信息（包含慢SQL列表）
     */
    DruidMonitorVO getDruidMonitorInfoWithSlowSql(Long slowSqlThreshold);

    /**
     * 获取在线用户列表
     * <p>
     * 说明：基于 JWT 无状态认证，通过 Redis 中存储的刷新令牌（jwt:refresh:user:{userId}）来判断用户是否在线。
     * 从刷新令牌的存储结构中读取完整的登录信息（IP、浏览器、操作系统、登录地点、登录时间等）。
     * </p>
     *
     * @param userName 用户名（可选，用于过滤）
     * @param ipaddr   IP地址（可选，用于过滤）
     * @return 在线用户列表
     */
    List<OnlineUserVO> getOnlineUserList(String userName, String ipaddr);

    /**
     * 强制用户下线
     * <p>
     * 说明：通过删除 Redis 中的刷新令牌来实现强制下线。
     * 由于 JWT 是无状态的，无法直接使已签发的 Token 失效，
     * 因此通过删除刷新令牌，使用户无法刷新 Token，从而间接实现下线效果。
     * </p>
     * <p>
     * 注意：已签发的 Access Token 在过期前仍然有效，这是 JWT 无状态特性的限制。
     * 如需立即失效，可考虑引入 Token 黑名单机制。
     * </p>
     *
     * @param sessionId 会话ID（实际为 Redis 中的刷新令牌 key，格式：jwt:refresh:user:{userId}）
     * @return 是否成功
     */
    boolean forceLogout(String sessionId);



    /**
     * 获取缓存列表
     *
     * @return 缓存列表
     */
    List<RedisCacheVO> getCacheList();

    /**
     * 根据缓存名称获取键名列表
     *
     * @param cacheName 缓存名称
     * @return 键名列表
     */
    List<RedisCacheVO.CacheKeyInfo> getCacheKeys(String cacheName);

    /**
     * 获取缓存内容
     *
     * @param cacheName 缓存名称
     * @param key       缓存键名
     * @return 缓存内容
     */
    RedisCacheVO.CacheContentInfo getCacheContent(String cacheName, String key);

    /**
     * 删除缓存（根据缓存名称删除所有匹配的键）
     *
     * @param cacheName 缓存名称
     * @return 删除的键数量
     */
    long deleteCache(String cacheName);

    /**
     * 删除单个缓存键
     *
     * @param cacheName 缓存名称
     * @param key       缓存键名
     * @return 是否成功
     */
    boolean deleteCacheKey(String cacheName, String key);

    /**
     * 清空所有缓存
     *
     * @return 是否成功
     */
    boolean clearAllCache();

}
