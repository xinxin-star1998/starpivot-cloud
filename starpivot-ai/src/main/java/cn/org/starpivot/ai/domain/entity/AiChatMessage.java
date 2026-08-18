package cn.org.starpivot.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_chat_message")
public class AiChatMessage {

    @TableId(type = IdType.AUTO)
    private Long messageId;

    private String conversationId;

    private String role;

    private String content;

    /** 助手消息引用资料 JSON（List&lt;RagSourceVo&gt;），用户消息为空 */
    private String sourcesJson;

    private Integer sortOrder;

    private LocalDateTime createTime;
}
