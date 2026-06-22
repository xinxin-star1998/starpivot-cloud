package cn.org.starpivot.generator.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.generator.config.GenExternalProperties;
import cn.org.starpivot.generator.domain.external.ExternalGenSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 外部代码生成会话存储（优先 Redis，不可用时降级内存）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalGenSessionStore {

    private final GenExternalProperties properties;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    private final Map<String, ExternalGenSession> localSessions = new ConcurrentHashMap<>();

    private static String sessionKey(String sessionId) {
        return CacheConstants.generatorSessionKey(sessionId);
    }

    public void save(ExternalGenSession session) {
        session.setExpireAt(Instant.now().plus(Duration.ofMinutes(properties.getSessionTtlMinutes())));
        if (stringRedisTemplate != null) {
            try {
                String json = objectMapper.writeValueAsString(session);
                stringRedisTemplate.opsForValue().set(
                        sessionKey(session.getSessionId()),
                        json,
                        Duration.ofMinutes(properties.getSessionTtlMinutes()));
                return;
            } catch (JsonProcessingException e) {
                log.warn("会话序列化失败，降级内存存储: {}", e.getMessage());
            }
        }
        localSessions.put(session.getSessionId(), session);
    }

    public ExternalGenSession getRequired(String sessionId) {
        ExternalGenSession session = get(sessionId);
        if (session == null) {
            throw new BizException("会话已过期或不存在，请重新连接数据库");
        }
        if (session.getExpireAt() != null && Instant.now().isAfter(session.getExpireAt())) {
            remove(sessionId);
            throw new BizException("会话已过期，请重新连接数据库");
        }
        touch(session);
        return session;
    }

    private void touch(ExternalGenSession session) {
        session.setExpireAt(Instant.now().plus(Duration.ofMinutes(properties.getSessionTtlMinutes())));
        save(session);
    }

    public ExternalGenSession get(String sessionId) {
        if (stringRedisTemplate != null) {
            try {
                String json = stringRedisTemplate.opsForValue().get(sessionKey(sessionId));
                if (json != null) {
                    return objectMapper.readValue(json, ExternalGenSession.class);
                }
            } catch (JsonProcessingException e) {
                log.warn("会话反序列化失败: {}", e.getMessage());
            }
        }
        ExternalGenSession session = localSessions.get(sessionId);
        if (session != null && session.getExpireAt() != null && Instant.now().isAfter(session.getExpireAt())) {
            localSessions.remove(sessionId);
            return null;
        }
        return session;
    }

    public void remove(String sessionId) {
        if (stringRedisTemplate != null) {
            stringRedisTemplate.delete(sessionKey(sessionId));
        }
        localSessions.remove(sessionId);
    }
}

