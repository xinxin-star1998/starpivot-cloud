package cn.org.starpivot.ai.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AiChatSessionAdminVo {

    private Long sessionId;

    private String conversationId;

    private Long userId;

    private String title;

    private Integer messageCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
