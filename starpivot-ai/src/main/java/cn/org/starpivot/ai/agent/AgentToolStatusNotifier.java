package cn.org.starpivot.ai.agent;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 把 Agent 工具执行进度推到当前流式会话的 status 通道。
 */
@Component
public class AgentToolStatusNotifier {

    private final ConcurrentHashMap<String, Consumer<String>> listeners = new ConcurrentHashMap<>();

    public void listen(String conversationId, Consumer<String> listener) {
        if (!StringUtils.hasText(conversationId) || listener == null) {
            return;
        }
        listeners.put(conversationId.trim(), listener);
    }

    public void clear(String conversationId) {
        if (!StringUtils.hasText(conversationId)) {
            return;
        }
        listeners.remove(conversationId.trim());
    }

    public void notify(String conversationId, String message) {
        if (!StringUtils.hasText(conversationId) || !StringUtils.hasText(message)) {
            return;
        }
        Consumer<String> listener = listeners.get(conversationId.trim());
        if (listener != null) {
            listener.accept(message.trim());
        }
    }
}
