package cn.org.starpivot.system.domain.bo;

import lombok.Data;

/**
 * SSE / Redis 推送载荷。
 */
@Data
public class MessagePushPayload {

    private Long userId;

    private Long unreadCount;

    private SysUserMessageVO message;
}
