package cn.org.starpivot.ai.memory;

import cn.org.starpivot.ai.domain.vo.RagSourceVo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流式结束后引用可能晚于 / 早于 ChatMemory 落库，用挂起队列保证能写进 ASSISTANT 行。
 */
@Component
public class ChatPendingSourceStore {

    private final ConcurrentHashMap<String, List<RagSourceVo>> pending = new ConcurrentHashMap<>();

    public void put(String conversationId, List<RagSourceVo> sources) {
        if (!StringUtils.hasText(conversationId) || sources == null || sources.isEmpty()) {
            return;
        }
        pending.put(conversationId, List.copyOf(sources));
    }

    public List<RagSourceVo> take(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return List.of();
        }
        List<RagSourceVo> sources = pending.remove(conversationId);
        return sources != null ? sources : List.of();
    }

    public List<RagSourceVo> peek(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return List.of();
        }
        List<RagSourceVo> sources = pending.get(conversationId);
        return sources != null ? sources : List.of();
    }
}
