package cn.org.starpivot.ai.memory;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 从用户话里轻量抽出菜单/功能等主题词，供追问改写与路由使用。
 */
@Component
public class ConversationTopicExtractor {

    private static final Pattern MENU_PATH = Pattern.compile(
            "[\\u4e00-\\u9fa5A-Za-z0-9_]{2,16}(?:\\s*(?:→|->|/|＞|>)\\s*[\\u4e00-\\u9fa5A-Za-z0-9_]{2,16}){1,4}");
    private static final Pattern QUOTED = Pattern.compile("[「“\"']([^」”\"']{2,24})[」”\"']");
    private static final Pattern BACKTICK = Pattern.compile("`([^`]{2,32})`");
    private static final Pattern ACTION_OBJECT = Pattern.compile(
            "(?:怎么|如何|怎样)?(?:新建|新增|创建|配置|设置|打开|进入|删除|编辑|修改|查看)?\\s*([\\u4e00-\\u9fa5A-Za-z0-9_]{2,16})(?:功能|页面|菜单|模块|按钮|权限)?");

    public List<String> extract(String message) {
        if (!StringUtils.hasText(message)) {
            return List.of();
        }
        String text = message.trim();
        Set<String> topics = new LinkedHashSet<>();

        Matcher menu = MENU_PATH.matcher(text);
        while (menu.find() && topics.size() < 3) {
            add(topics, menu.group().replaceAll("\\s+", ""));
        }
        Matcher quoted = QUOTED.matcher(text);
        while (quoted.find() && topics.size() < 3) {
            add(topics, quoted.group(1));
        }
        Matcher tick = BACKTICK.matcher(text);
        while (tick.find() && topics.size() < 3) {
            add(topics, tick.group(1));
        }
        Matcher action = ACTION_OBJECT.matcher(text);
        while (action.find() && topics.size() < 3) {
            add(topics, action.group(1));
        }
        return new ArrayList<>(topics);
    }

    private static void add(Set<String> topics, String raw) {
        if (!StringUtils.hasText(raw)) {
            return;
        }
        String value = raw.trim()
                .replaceAll("^[的了吗呢啊哦呀]+|[的了吗呢啊哦呀]+$", "")
                .trim();
        if (value.length() < 2 || value.length() > 24) {
            return;
        }
        if (isStopword(value)) {
            return;
        }
        topics.add(value);
    }

    private static boolean isStopword(String value) {
        return switch (value) {
            case "这个", "那个", "什么", "怎么", "如何", "怎样", "一下", "帮我", "请", "谢谢", "可以", "用户", "系统" ->
                    true;
            default -> false;
        };
    }
}
