package cn.org.starpivot.ai.memory;

import cn.org.starpivot.ai.domain.entity.AiChatMessage;
import cn.org.starpivot.ai.mapper.AiChatMessageMapper;
import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MysqlChatMemoryRepository implements ChatMemoryRepository {

    private static final int HARD_MAX_MESSAGES = 200;

    private final AiChatMessageMapper aiChatMessageMapper;
    private final MysqlChatSessionRepository chatSessionRepository;
    private final AiRuntimeConfigService aiRuntimeConfigService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(String conversationId, List<Message> messages) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        List<Message> trimmed = trimMessages(messages);
        aiChatMessageMapper.delete(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getConversationId, conversationId));

        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < trimmed.size(); i++) {
            Message message = trimmed.get(i);
            AiChatMessage entity = new AiChatMessage();
            entity.setConversationId(conversationId);
            entity.setRole(message.getMessageType().name());
            entity.setContent(message.getText());
            entity.setSortOrder(i);
            entity.setCreateTime(now);
            aiChatMessageMapper.insert(entity);
        }
        chatSessionRepository.syncMessageStats(conversationId, trimmed.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findByConversationId(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return List.of();
        }
        List<AiChatMessage> records = aiChatMessageMapper.selectList(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getConversationId, conversationId)
                .orderByAsc(AiChatMessage::getSortOrder)
                .orderByAsc(AiChatMessage::getMessageId));
        return toMessages(trimToEffectiveLimit(records));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByConversationId(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        aiChatMessageMapper.delete(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getConversationId, conversationId));
        chatSessionRepository.syncMessageStats(conversationId, 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findConversationIds() {
        return aiChatMessageMapper.selectList(new LambdaQueryWrapper<AiChatMessage>()
                        .select(AiChatMessage::getConversationId))
                .stream()
                .map(AiChatMessage::getConversationId)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AiChatMessage> listRawMessages(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return List.of();
        }
        return aiChatMessageMapper.selectList(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getConversationId, conversationId)
                .orderByAsc(AiChatMessage::getSortOrder)
                .orderByAsc(AiChatMessage::getMessageId));
    }

    private List<Message> trimMessages(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        int effectiveMax = effectiveMaxMessages();
        if (messages.size() <= effectiveMax) {
            return new ArrayList<>(messages);
        }
        return new ArrayList<>(messages.subList(messages.size() - effectiveMax, messages.size()));
    }

    private List<AiChatMessage> trimToEffectiveLimit(List<AiChatMessage> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        int effectiveMax = effectiveMaxMessages();
        if (records.size() <= effectiveMax) {
            return records;
        }
        return records.subList(records.size() - effectiveMax, records.size());
    }

    private int effectiveMaxMessages() {
        int configured = aiRuntimeConfigService.current().getMaxMemoryMessages();
        if (configured <= 0) {
            configured = 30;
        }
        return Math.min(configured, HARD_MAX_MESSAGES);
    }

    private List<Message> toMessages(List<AiChatMessage> records) {
        List<Message> messages = new ArrayList<>();
        for (AiChatMessage record : records) {
            if ("USER".equals(record.getRole())) {
                messages.add(new UserMessage(record.getContent()));
            } else if ("ASSISTANT".equals(record.getRole())) {
                messages.add(new AssistantMessage(record.getContent()));
            }
        }
        return messages;
    }
}
