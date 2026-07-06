package cn.org.starpivot.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_chat_session")
public class AiChatSession {

    @TableId(type = IdType.AUTO)
    private Long sessionId;

    private String conversationId;

    private Long userId;

    private String title;

    private Integer messageCount;

    @TableLogic
    private String delFlag;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
