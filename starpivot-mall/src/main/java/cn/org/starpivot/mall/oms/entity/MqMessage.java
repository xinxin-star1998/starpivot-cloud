package cn.org.starpivot.mall.oms.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * MQ 消息实体。
 * <p>
 * 对应数据库表 {@code mq_message}。
 * </p>
 * <ul>
 *   <li>{@link TableName} — 映射表名</li>
 *   <li>{@link TableId} — 主键策略</li>
 *   <li>{@link Data} — 自动生成 getter/setter</li>
 * </ul>
 */

@Data
@TableName("mq_message")
public class MqMessage {

    /**
     * Message ID
     */
    /**
     * message ID
     */
    @TableId(value = "message_id", type = IdType.INPUT)
    private String messageId;

    /**
     * content
     */
    private String content;

    /**
     * to Exchange
     */
    private String toExchange;

    /**
     * routing Key
     */
    private String routingKey;

    /**
     * 类型
     */
    private String classType;

    /**
     * 状态
     */
    private Integer messageStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
