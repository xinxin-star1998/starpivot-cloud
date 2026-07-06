package cn.org.starpivot.ai.memory;

import cn.org.starpivot.ai.domain.entity.AiChatSession;
import cn.org.starpivot.ai.domain.vo.ChatSessionVo;
import cn.org.starpivot.ai.mapper.AiChatSessionMapper;
import cn.org.starpivot.common.exception.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MysqlChatSessionRepository {

    private static final String DEFAULT_TITLE = "新对话";

    private final AiChatSessionMapper aiChatSessionMapper;

    @Transactional(rollbackFor = Exception.class)
    public void upsertSession(Long userId, String conversationId, String title) {
        if (userId == null || !StringUtils.hasText(conversationId)) {
            return;
        }
        AiChatSession existing = findActiveSession(conversationId);
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            AiChatSession session = new AiChatSession();
            session.setConversationId(conversationId);
            session.setUserId(userId);
            session.setTitle(resolveTitle(null, title));
            session.setMessageCount(0);
            session.setCreateTime(now);
            session.setUpdateTime(now);
            aiChatSessionMapper.insert(session);
            return;
        }
        String resolvedTitle = resolveTitle(existing.getTitle(), title);
        existing.setTitle(resolvedTitle);
        existing.setUpdateTime(now);
        aiChatSessionMapper.updateById(existing);
    }

    @Transactional(rollbackFor = Exception.class)
    public void touchSession(Long userId, String conversationId, String firstUserMessage) {
        if (userId == null || !StringUtils.hasText(conversationId)) {
            return;
        }
        AiChatSession session = requireActiveSession(conversationId, userId);
        String title = session.getTitle();
        if (DEFAULT_TITLE.equals(title) && StringUtils.hasText(firstUserMessage)) {
            session.setTitle(summarizeTitle(firstUserMessage));
        }
        session.setUpdateTime(LocalDateTime.now());
        aiChatSessionMapper.updateById(session);
    }

    @Transactional(readOnly = true)
    public List<ChatSessionVo> listSessions(Long userId) {
        if (userId == null) {
            return List.of();
        }
        return aiChatSessionMapper.selectList(new LambdaQueryWrapper<AiChatSession>()
                        .eq(AiChatSession::getUserId, userId)
                        .orderByDesc(AiChatSession::getUpdateTime)
                        .orderByDesc(AiChatSession::getSessionId))
                .stream()
                .map(this::toVo)
                .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void removeSession(Long userId, String conversationId) {
        if (userId == null || !StringUtils.hasText(conversationId)) {
            return;
        }
        AiChatSession session = requireActiveSession(conversationId, userId);
        aiChatSessionMapper.deleteById(session.getSessionId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void renameSession(Long userId, String conversationId, String title) {
        if (userId == null || !StringUtils.hasText(conversationId) || !StringUtils.hasText(title)) {
            return;
        }
        AiChatSession session = requireActiveSession(conversationId, userId);
        session.setTitle(title.trim());
        session.setUpdateTime(LocalDateTime.now());
        aiChatSessionMapper.updateById(session);
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncMessageStats(String conversationId, int messageCount) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        aiChatSessionMapper.update(
                null,
                new LambdaUpdateWrapper<AiChatSession>()
                        .set(AiChatSession::getMessageCount, messageCount)
                        .set(AiChatSession::getUpdateTime, LocalDateTime.now())
                        .eq(AiChatSession::getConversationId, conversationId));
    }

    @Transactional(readOnly = true)
    public AiChatSession findActiveSession(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return null;
        }
        return aiChatSessionMapper.selectOne(new LambdaQueryWrapper<AiChatSession>()
                .eq(AiChatSession::getConversationId, conversationId)
                .last("LIMIT 1"));
    }

    @Transactional(readOnly = true)
    public void assertSessionOwner(String conversationId, Long userId) {
        requireActiveSession(conversationId, userId);
    }

    private AiChatSession requireActiveSession(String conversationId, Long userId) {
        AiChatSession session = findActiveSession(conversationId);
        if (session == null || !userId.equals(session.getUserId())) {
            throw new BizException("无权访问该会话");
        }
        return session;
    }

    private ChatSessionVo toVo(AiChatSession session) {
        long updatedAt = session.getUpdateTime() != null
                ? session.getUpdateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : System.currentTimeMillis();
        return ChatSessionVo.builder()
                .conversationId(session.getConversationId())
                .title(session.getTitle())
                .updatedAt(updatedAt)
                .build();
    }

    private String resolveTitle(String existingTitle, String requestedTitle) {
        String resolvedTitle = StringUtils.hasText(requestedTitle) ? requestedTitle.trim() : DEFAULT_TITLE;
        if (StringUtils.hasText(existingTitle)
                && !DEFAULT_TITLE.equals(existingTitle)
                && DEFAULT_TITLE.equals(resolvedTitle)) {
            return existingTitle;
        }
        return resolvedTitle;
    }

    private String summarizeTitle(String message) {
        String normalized = message.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 24) {
            return normalized;
        }
        return normalized.substring(0, 24) + "...";
    }
}
