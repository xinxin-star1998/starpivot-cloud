package cn.org.starpivot.approval.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQ 本地消息表（Outbox），对应 {@code mq_message}。
 */
@Data
@TableName("mq_message")
public class MqMessage {

    @TableId(value = "message_id", type = IdType.INPUT)
    private String messageId;

    private String content;

    private String toExchange;

    private String routingKey;

    private String classType;

    private Integer messageStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
