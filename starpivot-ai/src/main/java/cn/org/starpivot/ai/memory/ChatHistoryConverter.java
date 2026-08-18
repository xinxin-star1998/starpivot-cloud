package cn.org.starpivot.ai.memory;

import cn.org.starpivot.ai.domain.entity.AiChatMessage;
import cn.org.starpivot.ai.domain.vo.ChatHistoryMessageVo;
import cn.org.starpivot.ai.domain.vo.RagSourceVo;
import org.springframework.ai.chat.messages.Message;

import java.time.ZoneId;
import java.util.List;

public final class ChatHistoryConverter {

    private ChatHistoryConverter() {}

    public static ChatHistoryMessageVo toVo(AiChatMessage message) {
        return toVo(message, List.of());
    }

    public static ChatHistoryMessageVo toVo(AiChatMessage message, List<RagSourceVo> sources) {
        Long createTime = message.getCreateTime() != null
                ? message.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                : null;
        return ChatHistoryMessageVo.builder()
                .role(message.getRole())
                .content(message.getContent())
                .createTime(createTime)
                .sources(sources != null && !sources.isEmpty() ? sources : null)
                .build();
    }

    public static List<ChatMemorySyncLogic.MessageSnapshot> toSnapshots(List<AiChatMessage> records) {
        return records.stream()
                .map(record -> ChatMemorySyncLogic.MessageSnapshot.fromEntity(record.getRole(), record.getContent()))
                .toList();
    }

    public static List<ChatMemorySyncLogic.MessageSnapshot> toSnapshotsFromMessages(List<Message> messages) {
        return messages.stream()
                .map(ChatMemorySyncLogic.MessageSnapshot::from)
                .toList();
    }
}
