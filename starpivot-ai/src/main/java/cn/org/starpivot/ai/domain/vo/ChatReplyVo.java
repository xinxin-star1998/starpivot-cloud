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
public class ChatReplyVo {

    private String conversationId;

    private String reply;

    private List<RagSourceVo> sources;

    /** 自动路由识别到的意图 */
    private String intent;

    /** RAG 使用的改写查询（追问补全后） */
    private String rewrittenQuery;
}
