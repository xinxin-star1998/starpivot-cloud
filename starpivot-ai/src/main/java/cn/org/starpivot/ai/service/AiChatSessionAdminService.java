package cn.org.starpivot.ai.service;

import cn.org.starpivot.ai.domain.dto.AiChatSessionQueryDto;
import cn.org.starpivot.ai.domain.vo.AiChatSessionAdminVo;
import cn.org.starpivot.ai.domain.vo.ChatHistoryMessageVo;
import cn.org.starpivot.common.entity.PageResponse;

import java.util.List;

public interface AiChatSessionAdminService {

    PageResponse<AiChatSessionAdminVo> pageList(AiChatSessionQueryDto query);

    List<ChatHistoryMessageVo> listMessages(String conversationId);

    void remove(Long sessionId);
}
