package cn.org.starpivot.auth.service;

import cn.org.starpivot.auth.domain.DeviceSessionVo;
import cn.org.starpivot.auth.domain.UserSessionSnapshot;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * 用户活跃会话查询与强制下线。
 */
@Service
@RequiredArgsConstructor
public class SessionService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final RefreshTokenService refreshTokenService;

    public List<DeviceSessionVo> listUserSessions(Long userId) {
        assertSelfOrForbidden(userId);
        String currentSessionId = SecurityContextUtils.getSessionId();
        return refreshTokenService.listSessions(userId).stream()
                .map(snapshot -> toDeviceSession(snapshot, currentSessionId))
                .toList();
    }

    public void forceLogoutSession(Long userId, String deviceSessionId) {
        assertSelfOrForbidden(userId);
        if (!sessionExists(userId, deviceSessionId)) {
            throw new BizException(ErrorCode.NOT_FOUND, "会话不存在或已下线");
        }
        refreshTokenService.revokeSession(userId, deviceSessionId);
    }

    public void forceLogoutAllSessions(Long userId) {
        assertSelfOrForbidden(userId);
        refreshTokenService.revokeAll(userId);
    }

    private boolean sessionExists(Long userId, String deviceSessionId) {
        return refreshTokenService.listSessions(userId).stream()
                .anyMatch(snapshot -> deviceSessionId.equals(snapshot.getSessionId()));
    }

    private void assertSelfOrForbidden(Long userId) {
        Long currentUserId = SecurityContextUtils.getUserId();
        if (currentUserId == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED, "未登录");
        }
        if (!currentUserId.equals(userId)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权操作其他用户的会话");
        }
    }

    private DeviceSessionVo toDeviceSession(UserSessionSnapshot snapshot, String currentSessionId) {
        Map<String, String> info = snapshot.getAttributes();
        String loginTime = info.getOrDefault(RefreshTokenService.FIELD_LOGIN_TIME, "");
        String lastAccessTime = info.getOrDefault(RefreshTokenService.FIELD_LAST_ACCESS_TIME, loginTime);
        return DeviceSessionVo.builder()
                .deviceSessionId(snapshot.getSessionId())
                .ipaddr(info.getOrDefault(RefreshTokenService.FIELD_IP, ""))
                .browser(info.getOrDefault(RefreshTokenService.FIELD_BROWSER, ""))
                .os(info.getOrDefault(RefreshTokenService.FIELD_OS, ""))
                .createdAt(loginTime)
                .lastAccessTime(lastAccessTime)
                .sessionDuration(formatDuration(loginTime))
                .isCurrent(snapshot.getSessionId().equals(currentSessionId))
                .build();
    }

    private String formatDuration(String loginTime) {
        if (loginTime == null || loginTime.isBlank()) {
            return "-";
        }
        try {
            LocalDateTime start = LocalDateTime.parse(loginTime, FORMATTER);
            Duration duration = Duration.between(start, LocalDateTime.now());
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            if (hours > 0) {
                return hours + "小时" + (minutes > 0 ? minutes + "分钟" : "");
            }
            long seconds = duration.toSeconds();
            return seconds < 60 ? "不足1分钟" : minutes + "分钟";
        } catch (DateTimeParseException ex) {
            return "-";
        }
    }
}
