package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.domain.dto.ChatSendDto;
import cn.org.starpivot.ai.domain.dto.SessionRenameDto;
import cn.org.starpivot.ai.domain.vo.ChatHistoryMessageVo;
import cn.org.starpivot.ai.domain.vo.ChatSessionVo;
import cn.org.starpivot.ai.memory.ChatHistoryConverter;
import cn.org.starpivot.ai.memory.MysqlChatMemoryRepository;
import cn.org.starpivot.ai.memory.MysqlChatSessionRepository;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.security.SecurityContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final MysqlChatMemoryRepository chatMemoryRepository;
    private final MysqlChatSessionRepository chatSessionRepository;

    public void clearHistory(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            throw new BizException("conversationId 不能为空");
        }
        chatMemoryRepository.deleteByConversationId(resolveConversationId(conversationId));
    }

    public ChatSessionVo createSession() {
        Long userId = requireUserId();
        String conversationId = buildConversationId(userId);
        chatSessionRepository.upsertSession(userId, conversationId, "新对话");
        return ChatSessionVo.builder()
                .conversationId(conversationId)
                .title("新对话")
                .updatedAt(System.currentTimeMillis())
                .build();
    }

    public List<ChatSessionVo> listSessions() {
        return chatSessionRepository.listSessions(requireUserId());
    }

    public List<ChatHistoryMessageVo> listMessages(String conversationId) {
        return chatMemoryRepository.listRawMessages(resolveConversationId(conversationId)).stream()
                .map(ChatHistoryConverter::toVo)
                .collect(Collectors.toList());
    }

    public void deleteSession(String conversationId) {
        String resolvedConversationId = resolveConversationId(conversationId);
        chatMemoryRepository.deleteByConversationId(resolvedConversationId);
        chatSessionRepository.removeSession(requireUserId(), resolvedConversationId);
    }

    public ChatSessionVo renameSession(SessionRenameDto dto) {
        String resolvedConversationId = resolveConversationId(dto.getConversationId());
        Long userId = requireUserId();
        chatSessionRepository.renameSession(userId, resolvedConversationId, dto.getTitle().trim());
        return ChatSessionVo.builder()
                .conversationId(resolvedConversationId)
                .title(dto.getTitle().trim())
                .updatedAt(System.currentTimeMillis())
                .build();
    }

    public String resolveConversationId(String conversationId) {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            throw new BizException("请先登录后再使用 AI 对话");
        }
        if (StringUtils.hasText(conversationId)) {
            String resolved = conversationId.trim();
            if (!resolved.startsWith("user-" + userId + ":")) {
                throw new BizException("无权访问该会话");
            }
            if (chatSessionRepository.findActiveSession(resolved) == null) {
                chatSessionRepository.upsertSession(userId, resolved, "新对话");
            } else {
                chatSessionRepository.assertSessionOwner(resolved, userId);
            }
            return resolved;
        }

        String newConversationId = buildConversationId(userId);
        chatSessionRepository.upsertSession(userId, newConversationId, "新对话");
        return newConversationId;
    }

    public void touchSession(String conversationId, String userMessage) {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            return;
        }
        chatSessionRepository.touchSession(userId, conversationId, userMessage);
    }

    public void prepareConversation(ChatSendDto dto, String conversationId) {
        if (Boolean.TRUE.equals(dto.getRegenerate())) {
            popLastExchange(conversationId);
        }
    }

    private void popLastExchange(String conversationId) {
        List<Message> messages = new ArrayList<>(chatMemoryRepository.findByConversationId(conversationId));
        if (messages.isEmpty()) {
            return;
        }

        Message last = messages.get(messages.size() - 1);
        if (last instanceof AssistantMessage) {
            messages.remove(messages.size() - 1);
        }
        if (!messages.isEmpty()) {
            Message previous = messages.get(messages.size() - 1);
            if (previous instanceof UserMessage) {
                messages.remove(messages.size() - 1);
            }
        }

        if (messages.isEmpty()) {
            chatMemoryRepository.deleteByConversationId(conversationId);
        } else {
            chatMemoryRepository.saveAll(conversationId, messages);
        }
    }

    private Long requireUserId() {
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            throw new BizException("请先登录后再使用 AI 对话");
        }
        return userId;
    }

    private String buildConversationId(Long userId) {
        return "user-" + userId + ":" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }
}
