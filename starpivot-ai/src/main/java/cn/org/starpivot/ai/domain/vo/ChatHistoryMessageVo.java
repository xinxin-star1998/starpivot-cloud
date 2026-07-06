package cn.org.starpivot.ai.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryMessageVo {

    /** USER 或 ASSISTANT */
    private String role;

    private String content;

    /** 消息时间戳（毫秒） */
    private Long createTime;
}
