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
}
