package cn.org.starpivot.ai.service.chat;

import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 短追问沿用上一轮任务意图，避免「那个呢 / 还有呢」掉回 GENERAL。
 */
public final class ChatFollowUpIntent {

    private static final int MAX_FOLLOW_UP_LEN = 24;

    private static final Pattern FOLLOW_UP = Pattern.compile(
            "^(那|这个|那个|它|还是|还有|继续|然后)|呢$|的呢|怎么弄|具体(呢|说说)?|详细点|刚才|上面|同理|之前那个");

    private static final Pattern TASK_SWITCH = Pattern.compile(
            "翻译|写一首|写首|写篇|写个(诗|故事)|写封|润色|改成英文|帮我写|总结一下|总结下|帮我算|算一下|重新说|换个话题");

    private ChatFollowUpIntent() {}

    public static boolean shouldInherit(String message, ChatIntent current, ChatIntent previous) {
        if (previous == null || !previous.isSticky()) {
            return false;
        }
        if (current == ChatIntent.CHITCHAT) {
            return false;
        }
        if (current != ChatIntent.GENERAL) {
            return false;
        }
        if (!StringUtils.hasText(message)) {
            return false;
        }
        String text = message.trim();
        if (text.length() > MAX_FOLLOW_UP_LEN || TASK_SWITCH.matcher(text).find()) {
            return false;
        }
        // 必须像指代/延续，禁止「任意 ≤8 字」误粘
        return FOLLOW_UP.matcher(text).find();
    }
}
