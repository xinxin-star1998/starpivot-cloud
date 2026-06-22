package cn.org.starpivot.mq.core;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 消息发送结果。
 */
@Data
@AllArgsConstructor
public class PublishResult {

    private String messageId;
    private String routingKey;
    private String eventType;
}
