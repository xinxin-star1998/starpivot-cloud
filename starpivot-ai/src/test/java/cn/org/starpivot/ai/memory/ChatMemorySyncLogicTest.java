package cn.org.starpivot.ai.memory;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatMemorySyncLogicTest {

    private static ChatMemorySyncLogic.MessageSnapshot user(String content) {
        return ChatMemorySyncLogic.MessageSnapshot.fromEntity("USER", content);
    }

    private static ChatMemorySyncLogic.MessageSnapshot assistant(String content) {
        return ChatMemorySyncLogic.MessageSnapshot.fromEntity("ASSISTANT", content);
    }

    @Test
    void plan_appendWhenIncomingExtendsExisting() {
        List<ChatMemorySyncLogic.MessageSnapshot> existing = List.of(user("hello"));
        List<ChatMemorySyncLogic.MessageSnapshot> incoming = List.of(user("hello"), assistant("hi"));

        ChatMemorySyncLogic.Plan plan = ChatMemorySyncLogic.plan(existing, incoming);

        assertEquals(ChatMemorySyncLogic.Mode.APPEND, plan.mode());
        assertEquals(1, plan.appendFromIndex());
    }

    @Test
    void plan_truncateWhenRegenerateRemovesLastExchange() {
        List<ChatMemorySyncLogic.MessageSnapshot> existing = List.of(
                user("hello"), assistant("hi"), user("again"), assistant("sure"));
        List<ChatMemorySyncLogic.MessageSnapshot> incoming = List.of(user("hello"), assistant("hi"));

        ChatMemorySyncLogic.Plan plan = ChatMemorySyncLogic.plan(existing, incoming);

        assertEquals(ChatMemorySyncLogic.Mode.TRUNCATE, plan.mode());
        assertEquals(2, plan.truncateToSize());
    }

    @Test
    void plan_noopWhenListsIdentical() {
        List<ChatMemorySyncLogic.MessageSnapshot> messages = List.of(user("hello"), assistant("hi"));

        ChatMemorySyncLogic.Plan plan = ChatMemorySyncLogic.plan(messages, messages);

        assertEquals(ChatMemorySyncLogic.Mode.NOOP, plan.mode());
    }

    @Test
    void plan_replaceWhenEmptyIncoming() {
        List<ChatMemorySyncLogic.MessageSnapshot> existing = List.of(user("hello"));

        ChatMemorySyncLogic.Plan plan = ChatMemorySyncLogic.plan(existing, List.of());

        assertEquals(ChatMemorySyncLogic.Mode.REPLACE, plan.mode());
    }

    @Test
    void plan_replaceWhenPrefixMismatch() {
        List<ChatMemorySyncLogic.MessageSnapshot> existing = List.of(user("hello"));
        List<ChatMemorySyncLogic.MessageSnapshot> incoming = List.of(user("different"), assistant("ok"));

        ChatMemorySyncLogic.Plan plan = ChatMemorySyncLogic.plan(existing, incoming);

        assertEquals(ChatMemorySyncLogic.Mode.REPLACE, plan.mode());
    }
}
