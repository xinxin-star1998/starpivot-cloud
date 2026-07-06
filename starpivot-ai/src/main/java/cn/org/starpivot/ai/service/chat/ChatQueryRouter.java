package cn.org.starpivot.ai.service.chat;

import cn.org.starpivot.ai.config.AiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatQueryRouter {

    public static final String AUTO = "auto";

    private static final Pattern CHITCHAT = Pattern.compile(
            "^(你好|您好|hi|hello|hey|在吗|在么|谢谢|感谢|多谢|好的|ok|okay|嗯|哦|再见|拜拜|早上好|晚上好)[!！。?？~～]*$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern REASONING = Pattern.compile(
            "原理|推导|证明|为什么.*(因为|机制)|架构设计|设计方案|对比.*(优劣|区别)|权衡|多步|复杂.*(分析|问题)|如何设计",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DEVELOPER = Pattern.compile(
            "```|代码|编程|程序|java|spring|vue|mysql|redis|mybatis|api|接口|bug|报错|异常|stacktrace|空指针|编译|debug|函数|class |interface |SELECT |INSERT |NullPointer",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern ANALYST = Pattern.compile(
            "数据分析|报表|统计|指标|同比|环比|趋势|图表|sql查询|聚合|dashboard|可视化",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern KNOWLEDGE = Pattern.compile(
            "怎么|如何|怎样|什么是|是什么|在哪|哪里|配置|设置|教程|文档|手册|功能|操作|步骤|star\\s*pivot|平台|知识库|权限|菜单",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern PLATFORM_DOC = Pattern.compile(
            "怎么|如何|配置|文档|平台|star\\s*pivot|知识库|菜单|权限|操作步骤",
            Pattern.CASE_INSENSITIVE);

    private final AiProperties aiProperties;

    public boolean isAuto(String value) {
        return !StringUtils.hasText(value) || AUTO.equalsIgnoreCase(value.trim());
    }

    public ChatIntent classify(String message) {
        if (!StringUtils.hasText(message)) {
            return ChatIntent.GENERAL;
        }
        String text = message.trim();
        if (text.length() <= 16 && CHITCHAT.matcher(text).matches()) {
            return ChatIntent.CHITCHAT;
        }
        if (REASONING.matcher(text).find()) {
            return ChatIntent.REASONING;
        }
        if (DEVELOPER.matcher(text).find()) {
            return ChatIntent.DEVELOPER;
        }
        if (ANALYST.matcher(text).find()) {
            return ChatIntent.ANALYST;
        }
        if (KNOWLEDGE.matcher(text).find()) {
            return ChatIntent.KNOWLEDGE;
        }
        return ChatIntent.GENERAL;
    }

    public RoutedSuggestion suggest(ChatIntent intent) {
        AiProperties.QueryRouterProperties router = aiProperties.getQueryRouter();
        String chatModel = router.resolvedChatModel(aiProperties.getDefaultModel());
        String reasonerModel = router.resolvedReasonerModel("deepseek-reasoner");

        return switch (intent) {
            case CHITCHAT -> new RoutedSuggestion("default", chatModel, false);
            case KNOWLEDGE -> new RoutedSuggestion("support", chatModel, true);
            case DEVELOPER -> new RoutedSuggestion("developer", chatModel, false);
            case ANALYST -> new RoutedSuggestion("analyst", chatModel, true);
            case REASONING -> new RoutedSuggestion("developer", reasonerModel, false);
            case GENERAL -> new RoutedSuggestion("default", chatModel, false);
        };
    }

    public boolean shouldUseRagForManualScene(String promptScene, String message) {
        if (!StringUtils.hasText(promptScene)) {
            return false;
        }
        return switch (promptScene.trim()) {
            case "support", "analyst" -> true;
            case "developer" -> StringUtils.hasText(message) && PLATFORM_DOC.matcher(message).find();
            default -> false;
        };
    }

    public record RoutedSuggestion(String promptScene, String model, boolean useRag) {}
}
