package cn.org.starpivot.ai.memory;

import cn.org.starpivot.ai.domain.entity.AiChatMessage;
import cn.org.starpivot.ai.domain.vo.RagSourceVo;
import cn.org.starpivot.ai.mapper.AiChatMessageMapper;
import cn.org.starpivot.ai.service.AiRuntimeConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Repository
@RequiredArgsConstructor
public class MysqlChatMemoryRepository implements ChatMemoryRepository {

    private static final int HARD_MAX_MESSAGES = 200;

    private final AiChatMessageMapper aiChatMessageMapper;
    private final MysqlChatSessionRepository chatSessionRepository;
    private final AiRuntimeConfigService aiRuntimeConfigService;
    private final ObjectMapper objectMapper;
    private final ChatPendingSourceStore chatPendingSourceStore;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(String conversationId, List<Message> messages) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        List<Message> incoming = trimMessages(messages);
        if (incoming.isEmpty()) {
            deleteByConversationId(conversationId);
            return;
        }

        List<AiChatMessage> existingRecords = loadRecords(conversationId);
        ChatMemorySyncLogic.Plan plan = ChatMemorySyncLogic.plan(
                ChatHistoryConverter.toSnapshots(existingRecords),
                ChatHistoryConverter.toSnapshotsFromMessages(incoming));

        switch (plan.mode()) {
            case NOOP -> chatSessionRepository.syncMessageStats(conversationId, existingRecords.size());
            case APPEND -> {
                appendMessages(conversationId, incoming.subList(plan.appendFromIndex(), incoming.size()), existingRecords);
                trimOldestIfNeeded(conversationId);
                chatSessionRepository.syncMessageStats(conversationId, countRecords(conversationId));
            }
            case TRUNCATE -> {
                truncateTail(conversationId, existingRecords, plan.truncateToSize());
                chatSessionRepository.syncMessageStats(conversationId, plan.truncateToSize());
            }
            case REPLACE -> replaceAll(conversationId, incoming);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> findByConversationId(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return List.of();
        }
        return toMessages(trimToEffectiveLimit(loadRecords(conversationId)));
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
        return loadRecords(conversationId);
    }

    /**
     * 把本轮引用资料挂到该会话最新一条 ASSISTANT 消息上（ChatMemory 落库之后调用）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void attachSourcesToLastAssistant(String conversationId, List<RagSourceVo> sources) {
        if (!StringUtils.hasText(conversationId) || sources == null || sources.isEmpty()) {
            return;
        }
        chatPendingSourceStore.put(conversationId, sources);
        AiChatMessage lastAssistant = aiChatMessageMapper.selectOne(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getConversationId, conversationId)
                .eq(AiChatMessage::getRole, "ASSISTANT")
                .orderByDesc(AiChatMessage::getSortOrder)
                .orderByDesc(AiChatMessage::getMessageId)
                .last("LIMIT 1"));
        if (lastAssistant == null || lastAssistant.getMessageId() == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(sources);
            aiChatMessageMapper.update(
                    null,
                    new LambdaUpdateWrapper<AiChatMessage>()
                            .set(AiChatMessage::getSourcesJson, json)
                            .eq(AiChatMessage::getMessageId, lastAssistant.getMessageId()));
            chatPendingSourceStore.take(conversationId);
        } catch (Exception ex) {
            log.warn("[ChatMemory] attach sources failed conversationId={}: {}", conversationId, ex.getMessage());
        }
    }

    private void applyPendingSources(AiChatMessage entity, String conversationId) {
        List<RagSourceVo> pending = chatPendingSourceStore.take(conversationId);
        if (pending.isEmpty()) {
            return;
        }
        try {
            entity.setSourcesJson(objectMapper.writeValueAsString(pending));
        } catch (Exception ex) {
            log.warn("[ChatMemory] serialize pending sources failed: {}", ex.getMessage());
        }
    }

    public List<RagSourceVo> parseSources(String sourcesJson) {
        if (!StringUtils.hasText(sourcesJson)) {
            return List.of();
        }
        try {
            List<RagSourceVo> sources = objectMapper.readValue(sourcesJson, new TypeReference<>() {});
            return sources != null ? sources : List.of();
        } catch (Exception ex) {
            return List.of();
        }
    }

    private void appendMessages(
            String conversationId, List<Message> delta, List<AiChatMessage> existingRecords) {
        if (delta.isEmpty()) {
            return;
        }
        int nextSortOrder = existingRecords.isEmpty()
                ? 0
                : existingRecords.get(existingRecords.size() - 1).getSortOrder() + 1;
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < delta.size(); i++) {
            Message message = delta.get(i);
            AiChatMessage entity = new AiChatMessage();
            entity.setConversationId(conversationId);
            entity.setRole(message.getMessageType().name());
            entity.setContent(message.getText());
            entity.setSortOrder(nextSortOrder + i);
            entity.setCreateTime(now);
            if ("ASSISTANT".equals(entity.getRole()) && i == delta.size() - 1) {
                applyPendingSources(entity, conversationId);
            }
            aiChatMessageMapper.insert(entity);
        }
    }

    private void truncateTail(String conversationId, List<AiChatMessage> existingRecords, int keepSize) {
        if (existingRecords.size() <= keepSize) {
            return;
        }
        List<Long> removeIds = existingRecords.subList(keepSize, existingRecords.size()).stream()
                .map(AiChatMessage::getMessageId)
                .toList();
        aiChatMessageMapper.deleteBatchIds(removeIds);
    }

    private void replaceAll(String conversationId, List<Message> incoming) {
        aiChatMessageMapper.delete(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getConversationId, conversationId));
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < incoming.size(); i++) {
            Message message = incoming.get(i);
            AiChatMessage entity = new AiChatMessage();
            entity.setConversationId(conversationId);
            entity.setRole(message.getMessageType().name());
            entity.setContent(message.getText());
            entity.setSortOrder(i);
            entity.setCreateTime(now);
            if ("ASSISTANT".equals(entity.getRole()) && i == incoming.size() - 1) {
                applyPendingSources(entity, conversationId);
            }
            aiChatMessageMapper.insert(entity);
        }
        chatSessionRepository.syncMessageStats(conversationId, incoming.size());
    }

    private void trimOldestIfNeeded(String conversationId) {
        int effectiveMax = effectiveMaxMessages();
        List<AiChatMessage> records = loadRecords(conversationId);
        if (records.size() <= effectiveMax) {
            return;
        }
        int removeCount = records.size() - effectiveMax;
        List<Long> removeIds = records.subList(0, removeCount).stream()
                .map(AiChatMessage::getMessageId)
                .toList();
        aiChatMessageMapper.deleteBatchIds(removeIds);
    }

    private List<AiChatMessage> loadRecords(String conversationId) {
        return aiChatMessageMapper.selectList(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getConversationId, conversationId)
                .orderByAsc(AiChatMessage::getSortOrder)
                .orderByAsc(AiChatMessage::getMessageId));
    }

    private int countRecords(String conversationId) {
        Long count = aiChatMessageMapper.selectCount(new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getConversationId, conversationId));
        return count != null ? count.intValue() : 0;
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
