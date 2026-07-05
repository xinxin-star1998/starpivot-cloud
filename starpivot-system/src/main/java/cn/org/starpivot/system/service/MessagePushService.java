package cn.org.starpivot.system.service;

import cn.org.starpivot.api.system.constant.MessageConstants;
import cn.org.starpivot.system.domain.bo.MessagePushPayload;
import cn.org.starpivot.system.domain.bo.SysUserMessageVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePushService {

    private final MessageSseRegistry messageSseRegistry;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(Long userId, SysUserMessageVO message, long unreadCount) {
        if (userId == null) {
            return;
        }
        MessagePushPayload payload = new MessagePushPayload();
        payload.setUserId(userId);
        payload.setUnreadCount(unreadCount);
        payload.setMessage(message);
        try {
            String json = objectMapper.writeValueAsString(payload);
            stringRedisTemplate.convertAndSend(MessageConstants.REDIS_CHANNEL_MESSAGE_PUSH, json);
        } catch (Exception ex) {
            log.warn("Redis message push failed, fallback to local SSE, userId={}", userId, ex);
            messageSseRegistry.dispatch(payload);
        }
    }

    public void dispatchFromRedis(String message) {
        try {
            MessagePushPayload payload = objectMapper.readValue(message, MessagePushPayload.class);
            messageSseRegistry.dispatch(payload);
        } catch (Exception ex) {
            log.warn("Parse message push payload failed: {}", message, ex);
        }
    }
}
