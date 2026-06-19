package cn.org.starpivot.monitor.service.impl;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 cloud 版 RefreshToken（starpivot:refresh:{userId}）的在线用户查询与强退。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudOnlineUserService {

    static final String REFRESH_PREFIX = "starpivot:refresh:";

    private final StringRedisTemplate stringRedisTemplate;
    private final MonitorUserQueryService monitorUserQueryService;
    private final MonitorDeptQueryService monitorDeptQueryService;

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
                MonitorUser user = userMap.get(entry.getValue());
                if (user == null) {
                    continue;
                }
                OnlineUserVO vo = new OnlineUserVO();
                vo.setSessionId(entry.getKey());
                vo.setUserId(user.getUserId());
                vo.setUserName(user.getUserName());
                vo.setNickName(user.getNickName());
                if (user.getDeptId() != null) {
                    vo.setDeptName(deptNameMap.get(user.getDeptId()));
                }
                vo.setIpaddr("");
                vo.setBrowser("");
                vo.setOs("");
                vo.setLoginLocation("");
                vo.setLoginTime(LocalDateTime.now());
                vo.setLastAccessTime(vo.getLoginTime());

                if (normalizedUserName != null) {
                    String display = user.getUserName() != null ? user.getUserName() : "";
                    String nick = user.getNickName() != null ? user.getNickName() : "";
                    if (!display.contains(normalizedUserName) && !nick.contains(normalizedUserName)) {
                        continue;
                    }
                }
                if (normalizedIpaddr != null && !normalizedIpaddr.isEmpty()) {
                    continue;
                }
                result.add(vo);
            }
        } catch (Exception e) {
            log.error("获取在线用户列表失败", e);
        }
        return result;
    }

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
