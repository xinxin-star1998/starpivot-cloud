package cn.org.starpivot.mq.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一消息信封，包装业务 payload 与元数据。
 *
 * @param <T> 业务载荷类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEnvelope<T> {

    private String messageId;
    private String traceId;
    private String eventType;
    private LocalDateTime occurredAt;
    private String producer;
    @Builder.Default
    private String version = "1";
    private T payload;
}
