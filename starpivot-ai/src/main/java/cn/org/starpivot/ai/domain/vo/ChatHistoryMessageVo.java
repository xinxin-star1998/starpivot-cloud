package cn.org.starpivot.ai.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

    /** 助手消息引用资料（历史回放） */
    private List<RagSourceVo> sources;
}
