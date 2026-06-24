package cn.org.starpivot.mall.oms.entity;

import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

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
