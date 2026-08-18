package cn.org.starpivot.ai.agent;

import cn.org.starpivot.ai.metrics.AiMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class CurrentDateTimeTool {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss EEEE", Locale.CHINA);

    private final AiMetrics aiMetrics;
    private final AgentToolStatusNotifier agentToolStatusNotifier;

    @Tool(description = "获取当前日期和时间（中国时区）。当用户问到现在、今天、本周、截止日期或需要时间锚点时使用。")
    public String getCurrentDatetime(ToolContext toolContext) {
        aiMetrics.recordAgentTool("get_current_datetime");
        String conversationId = null;
        if (toolContext != null && toolContext.getContext() != null) {
            Object value = toolContext.getContext().get("conversationId");
            conversationId = value != null ? String.valueOf(value) : null;
        }
        agentToolStatusNotifier.notify(conversationId, "已获取当前时间…");
        return ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).format(FORMATTER) + " (Asia/Shanghai)";
    }
}
