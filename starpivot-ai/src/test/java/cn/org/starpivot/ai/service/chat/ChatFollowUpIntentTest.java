package cn.org.starpivot.ai.service.chat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatFollowUpIntentTest {

    @Test
    void shouldInherit_shortFollowUpKeepsStickyTask() {
        assertTrue(ChatFollowUpIntent.shouldInherit("那个呢", ChatIntent.GENERAL, ChatIntent.KNOWLEDGE));
        assertTrue(ChatFollowUpIntent.shouldInherit("继续", ChatIntent.GENERAL, ChatIntent.DEVELOPER));
        assertTrue(ChatFollowUpIntent.shouldInherit("刚才那个再详细说说", ChatIntent.GENERAL, ChatIntent.ANALYST));
    }

    @Test
    void shouldInherit_skipsChitchatAndTaskSwitch() {
        assertFalse(ChatFollowUpIntent.shouldInherit("谢谢", ChatIntent.CHITCHAT, ChatIntent.KNOWLEDGE));
        assertFalse(ChatFollowUpIntent.shouldInherit("帮我写首诗", ChatIntent.GENERAL, ChatIntent.KNOWLEDGE));
        assertFalse(ChatFollowUpIntent.shouldInherit("总结一下", ChatIntent.GENERAL, ChatIntent.KNOWLEDGE));
        assertFalse(ChatFollowUpIntent.shouldInherit("那个呢", ChatIntent.GENERAL, ChatIntent.GENERAL));
        assertFalse(ChatFollowUpIntent.shouldInherit("如何配置权限", ChatIntent.KNOWLEDGE, ChatIntent.DEVELOPER));
        assertFalse(ChatFollowUpIntent.shouldInherit("好的收到", ChatIntent.GENERAL, ChatIntent.KNOWLEDGE));
    }
}
