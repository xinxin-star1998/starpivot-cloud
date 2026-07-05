package cn.org.starpivot.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户站内消息 sys_user_message
 */
@Data
@TableName("sys_user_message")
public class SysUserMessage {

    @TableId(value = "message_id", type = IdType.AUTO)
    private Long messageId;

    private Long userId;

    private String msgType;

    private String title;

    private String content;

    private String bizModule;

    private String bizType;

    private String bizKey;

    private Long bizId;

    private String linkPath;

    /** 0未读 1已读 */
    private String readFlag;

    private LocalDateTime createTime;
}
