package cn.org.starpivot.ai.memory;

import org.springframework.ai.chat.messages.Message;

import java.util.List;

/**
 * 纯逻辑：判断 saveAll 应走 append、截断还是全量替换，便于单测。
 */
public final class ChatMemorySyncLogic {

    private ChatMemorySyncLogic() {}

    public enum Mode {
        NOOP,
        APPEND,
        TRUNCATE,
        REPLACE
    }

    public record Plan(Mode mode, int appendFromIndex, int truncateToSize) {

        public static Plan noop() {
            return new Plan(Mode.NOOP, 0, 0);
        }

        public static Plan append(int fromIndex) {
            return new Plan(Mode.APPEND, fromIndex, 0);
        }

        public static Plan truncate(int toSize) {
            return new Plan(Mode.TRUNCATE, 0, toSize);
        }

        public static Plan replace() {
            return new Plan(Mode.REPLACE, 0, 0);
        }
    }

    public record MessageSnapshot(String role, String content) {

        public static MessageSnapshot from(Message message) {
            return new MessageSnapshot(message.getMessageType().name(), message.getText());
        }

        public static MessageSnapshot fromEntity(String role, String content) {
            return new MessageSnapshot(role, content);
        }
    }

    public static Plan plan(List<MessageSnapshot> existing, List<MessageSnapshot> incoming) {
        if (incoming == null || incoming.isEmpty()) {
            return Plan.replace();
        }
        if (existing == null || existing.isEmpty()) {
            return Plan.append(0);
        }
        if (existingIsPrefixOf(incoming, existing)) {
            if (incoming.size() == existing.size()) {
                return Plan.noop();
            }
            return Plan.append(existing.size());
        }
        if (existingIsPrefixOf(existing, incoming)) {
            return Plan.truncate(incoming.size());
        }
        return Plan.replace();
    }

    public static boolean matchesPrefix(List<MessageSnapshot> prefix, List<MessageSnapshot> full) {
        return existingIsPrefixOf(full, prefix);
    }

    private static boolean existingIsPrefixOf(List<MessageSnapshot> full, List<MessageSnapshot> prefix) {
        if (full.size() < prefix.size()) {
            return false;
        }
        for (int i = 0; i < prefix.size(); i++) {
            MessageSnapshot left = prefix.get(i);
            MessageSnapshot right = full.get(i);
            if (!left.role().equals(right.role()) || !contentEquals(left.content(), right.content())) {
                return false;
            }
        }
        return true;
    }

    private static boolean contentEquals(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }
}
