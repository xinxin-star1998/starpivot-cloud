package cn.org.starpivot.ai.service.impl;

import cn.org.starpivot.ai.domain.dto.AiChatSessionQueryDto;
import cn.org.starpivot.ai.domain.entity.AiChatMessage;
import cn.org.starpivot.ai.domain.entity.AiChatSession;
import cn.org.starpivot.ai.domain.vo.AiChatSessionAdminVo;
import cn.org.starpivot.ai.domain.vo.ChatHistoryMessageVo;
import cn.org.starpivot.ai.mapper.AiChatSessionMapper;
import cn.org.starpivot.ai.memory.MysqlChatMemoryRepository;
import cn.org.starpivot.ai.service.AiChatSessionAdminService;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiChatSessionAdminServiceImpl implements AiChatSessionAdminService {

    private final AiChatSessionMapper aiChatSessionMapper;
    private final MysqlChatMemoryRepository chatMemoryRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AiChatSessionAdminVo> pageList(AiChatSessionQueryDto query) {
        Page<AiChatSession> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<AiChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(query.getUserId() != null, AiChatSession::getUserId, query.getUserId())
                .like(StringUtils.hasText(query.getConversationId()), AiChatSession::getConversationId, query.getConversationId())
                .like(StringUtils.hasText(query.getTitle()), AiChatSession::getTitle, query.getTitle())
                .orderByDesc(AiChatSession::getUpdateTime)
                .orderByDesc(AiChatSession::getSessionId);
        Page<AiChatSession> result = aiChatSessionMapper.selectPage(page, wrapper);
        PageResponse<AiChatSessionAdminVo> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setRows(result.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatHistoryMessageVo> listMessages(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            throw new BizException("会话 ID 不能为空");
        }
        return chatMemoryRepository.listRawMessages(conversationId.trim()).stream()
                .map(this::toHistoryVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long sessionId) {
        AiChatSession session = aiChatSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BizException("会话不存在");
        }
        chatMemoryRepository.deleteByConversationId(session.getConversationId());
        aiChatSessionMapper.deleteById(sessionId);
    }

    private AiChatSessionAdminVo toVo(AiChatSession session) {
        AiChatSessionAdminVo vo = new AiChatSessionAdminVo();
        vo.setSessionId(session.getSessionId());
        vo.setConversationId(session.getConversationId());
        vo.setUserId(session.getUserId());
        vo.setTitle(session.getTitle());
        vo.setMessageCount(session.getMessageCount());
        vo.setCreateTime(session.getCreateTime());
        vo.setUpdateTime(session.getUpdateTime());
        return vo;
    }

    private ChatHistoryMessageVo toHistoryVo(AiChatMessage message) {
        Long createTime = message.getCreateTime() != null
                ? message.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : null;
        return ChatHistoryMessageVo.builder()
                .role(message.getRole())
                .content(message.getContent())
                .createTime(createTime)
                .build();
    }
}
