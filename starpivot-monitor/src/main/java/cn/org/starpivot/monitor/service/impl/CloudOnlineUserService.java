package cn.org.starpivot.monitor.service.impl;

import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.monitor.domain.entity.MonitorDept;
import cn.org.starpivot.monitor.domain.entity.MonitorUser;
import cn.org.starpivot.monitor.domain.vo.OnlineUserVO;
import cn.org.starpivot.monitor.service.MonitorDeptQueryService;
import cn.org.starpivot.monitor.service.MonitorUserQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 Redis RefreshToken 的在线用户查询与强制下线服务。
 * <p>
 * 扫描 {@link cn.org.starpivot.common.cache.CacheConstants#LOGIN_TOKENS} 前缀下的会话键，
 * 关联 {@link MonitorUserQueryService}、{@link MonitorDeptQueryService} 补全用户与部门信息，
 * 并支持按会话 ID 删除 RefreshToken 实现强退。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudOnlineUserService {

    static final String REFRESH_PREFIX = CacheConstants.LOGIN_TOKENS + ":" + CacheConstants.LOGIN_REFRESH + ":";
    
    // Hash 字段名常量（与 RefreshTokenService 保持一致）
    private static final String FIELD_IP = "ip";
    private static final String FIELD_BROWSER = "browser";
    private static final String FIELD_OS = "os";
    private static final String FIELD_LOGIN_LOCATION = "loginLocation";
    private static final String FIELD_LOGIN_TIME = "loginTime";
    private static final String FIELD_LAST_ACCESS_TIME = "lastAccessTime";

    private final StringRedisTemplate stringRedisTemplate;
    private final MonitorUserQueryService monitorUserQueryService;
    private final MonitorDeptQueryService monitorDeptQueryService;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取当前在线用户列表，支持按用户名与 IP 模糊过滤。
     * <p>
     * 从 Redis 扫描 RefreshToken 键，解析用户 ID 后批量查询用户与部门信息，
     * 并从 Hash 字段读取 IP、浏览器、操作系统、登录地点及时间等客户端信息。
     * </p>
     *
     * @param userName 用户名或昵称关键字，为空时不按用户名过滤
     * @param ipaddr   IP 地址关键字，为空时不按 IP 过滤
     * @return {@link OnlineUserVO} 列表，Redis 未配置或查询异常时返回空列表
     */
    public List<OnlineUserVO> getOnlineUserList(String userName, String ipaddr) {
        List<OnlineUserVO> result = new ArrayList<>();
        if (stringRedisTemplate == null || monitorUserQueryService == null) {
            return result;
        }
        try {
            String normalizedUserName = StringUtils.hasText(userName) ? userName.trim() : null;
            String normalizedIpaddr = StringUtils.hasText(ipaddr) ? ipaddr.trim() : null;

            Set<String> keys = scanKeys(REFRESH_PREFIX + "*");
            if (keys.isEmpty()) {
                return result;
            }

            Map<String, Long> keyToUserId = new HashMap<>();
            List<Long> userIds = new ArrayList<>();
            for (String key : keys) {
                if (!key.startsWith(REFRESH_PREFIX)) {
                    continue;
                }
                try {
                    Long userId = Long.parseLong(key.substring(REFRESH_PREFIX.length()));
                    userIds.add(userId);
                    keyToUserId.put(key, userId);
                } catch (NumberFormatException e) {
                    log.warn("解析在线用户 key 失败: {}", key);
                }
            }

            Map<Long, MonitorUser> userMap = monitorUserQueryService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(MonitorUser::getUserId, u -> u, (a, b) -> a));

            Set<Long> deptIds = userMap.values().stream()
                    .map(MonitorUser::getDeptId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Map<Long, String> deptNameMap = monitorDeptQueryService.listByIds(deptIds).stream()
                    .collect(Collectors.toMap(MonitorDept::getDeptId, MonitorDept::getDeptName, (a, b) -> a));

            for (Map.Entry<String, Long> entry : keyToUserId.entrySet()) {
                String key = entry.getKey();
                MonitorUser user = userMap.get(entry.getValue());
                if (user == null) {
                    continue;
                }
                
                // 从 Redis Hash 中获取客户端信息
                Map<String, String> clientInfo = getClientInfoFromRedis(key);
                
                OnlineUserVO vo = new OnlineUserVO();
                vo.setSessionId(key);
                vo.setUserId(user.getUserId());
                vo.setUserName(user.getUserName());
                vo.setNickName(user.getNickName());
                if (user.getDeptId() != null) {
                    vo.setDeptName(deptNameMap.get(user.getDeptId()));
                }
                vo.setIpaddr(clientInfo.getOrDefault(FIELD_IP, ""));
                vo.setBrowser(clientInfo.getOrDefault(FIELD_BROWSER, ""));
                vo.setOs(clientInfo.getOrDefault(FIELD_OS, ""));
                vo.setLoginLocation(clientInfo.getOrDefault(FIELD_LOGIN_LOCATION, ""));
                
                // 解析登录时间和最后访问时间
                String loginTimeStr = clientInfo.get(FIELD_LOGIN_TIME);
                String lastAccessTimeStr = clientInfo.get(FIELD_LAST_ACCESS_TIME);
                try {
                    vo.setLoginTime(loginTimeStr != null ? LocalDateTime.parse(loginTimeStr, FORMATTER) : LocalDateTime.now());
                    vo.setLastAccessTime(lastAccessTimeStr != null ? LocalDateTime.parse(lastAccessTimeStr, FORMATTER) : vo.getLoginTime());
                } catch (Exception e) {
                    vo.setLoginTime(LocalDateTime.now());
                    vo.setLastAccessTime(vo.getLoginTime());
                }

                // 用户名过滤
                if (normalizedUserName != null) {
                    String display = user.getUserName() != null ? user.getUserName() : "";
                    String nick = user.getNickName() != null ? user.getNickName() : "";
                    if (!display.contains(normalizedUserName) && !nick.contains(normalizedUserName)) {
                        continue;
                    }
                }
                
                // IP地址过滤
                if (normalizedIpaddr != null && !normalizedIpaddr.isEmpty()) {
                    String userIp = vo.getIpaddr();
                    if (userIp == null || userIp.isEmpty() || !userIp.contains(normalizedIpaddr)) {
                        continue;
                    }
                }
                
                result.add(vo);
            }
        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
        }
        return result;
    }
    
    /**
     * 从 Redis 读取指定会话键的客户端信息。
     * <p>优先读取 Hash 字段；若为空则兼容旧版字符串存储方式，返回空的客户端字段。</p>
     *
     * @param key RefreshToken 对应的 Redis 键
     * @return 包含 IP、浏览器、操作系统、登录地点及时间等字段的映射，读取失败时返回部分空值
     */
    private Map<String, String> getClientInfoFromRedis(String key) {
        Map<String, String> info = new HashMap<>();
        try {
            Map<Object, Object> hash = stringRedisTemplate.opsForHash().entries(key);
            
            if (hash.isEmpty()) {
                // 兼容旧版本（字符串存储方式）
                String token = stringRedisTemplate.opsForValue().get(key);
                if (token != null) {
                    // 旧版本只有token，返回空的客户端信息
                    info.put(FIELD_IP, "");
                    info.put(FIELD_BROWSER, "");
                    info.put(FIELD_OS, "");
                    info.put(FIELD_LOGIN_LOCATION, "");
                    info.put(FIELD_LOGIN_TIME, "");
                    info.put(FIELD_LAST_ACCESS_TIME, "");
                }
            } else {
                info.put(FIELD_IP, (String) hash.get(FIELD_IP));
                info.put(FIELD_BROWSER, (String) hash.get(FIELD_BROWSER));
                info.put(FIELD_OS, (String) hash.get(FIELD_OS));
                info.put(FIELD_LOGIN_LOCATION, (String) hash.get(FIELD_LOGIN_LOCATION));
                info.put(FIELD_LOGIN_TIME, (String) hash.get(FIELD_LOGIN_TIME));
                info.put(FIELD_LAST_ACCESS_TIME, (String) hash.get(FIELD_LAST_ACCESS_TIME));
            }
        } catch (Exception e) {
            log.warn("获取客户端信息失败, key={}", key, e);
        }
        return info;
    }

    /**
     * 强制指定会话下线，删除 Redis 中的 RefreshToken。
     * <p>
     * 支持完整 Redis 键、{@code REFRESH_PREFIX} 前缀键，以及纯数字用户 ID（自动补前缀）。
     * </p>
     *
     * @param sessionId 会话 ID、完整 Redis 键或用户 ID
     * @return 删除成功返回 {@code true}，参数无效或删除失败返回 {@code false}
     */
    public boolean forceLogout(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return false;
        }
        try {
            if (sessionId.startsWith(REFRESH_PREFIX)) {
                stringRedisTemplate.delete(sessionId);
                return true;
            }
            if (sessionId.matches("\\d+")) {
                stringRedisTemplate.delete(REFRESH_PREFIX + sessionId);
                return true;
            }
            stringRedisTemplate.delete(sessionId);
            return true;
        } catch (Exception e) {
            log.error("强制下线失败: {}", sessionId, e);
            return false;
        }
    }

    /**
     * 使用 SCAN 命令非阻塞扫描匹配模式的 Redis 键。
     *
     * @param pattern 键匹配模式，如 {@code login_tokens:refresh:*}
     * @return 匹配的键集合，无匹配时返回空集合
     */
    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(100).build();
        try (Cursor<String> cursor = stringRedisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                keys.add(cursor.next());
            }
        }
        return keys;
    }
}
