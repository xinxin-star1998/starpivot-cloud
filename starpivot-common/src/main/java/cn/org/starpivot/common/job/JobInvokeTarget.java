package cn.org.starpivot.common.job;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 定时任务调用目标解析。
 * 支持全限定名或短类名：{@code CleanOperLogTask.cleanOperLog()}。
 */
public final class JobInvokeTarget {

    private static final Pattern TARGET = Pattern.compile(
            "^([a-zA-Z_][\\w]*(?:\\.[a-zA-Z_][\\w]*)*)\\.([a-zA-Z_][\\w]*)\\((.*)\\)$");

    private JobInvokeTarget() {
    }

    public record Parsed(String className, String methodName, List<Object> args) {
    }

    public static void assertSafe(String invokeTarget, String[] whitelist, String[] blocked) {
        if (invokeTarget == null || invokeTarget.isBlank()) {
            throw new IllegalArgumentException("调用目标不能为空");
        }
        String target = invokeTarget.trim();
        if (blocked != null) {
            for (String fragment : blocked) {
                if (fragment != null && target.contains(fragment)) {
                    throw new IllegalArgumentException("调用目标包含违规字符");
                }
            }
        }
        Parsed parsed = parse(target);
        if (parsed.className().contains(".")) {
            boolean allowed = false;
            if (whitelist != null) {
                for (String prefix : whitelist) {
                    if (prefix != null && parsed.className().startsWith(prefix)) {
                        allowed = true;
                        break;
                    }
                }
            }
            if (!allowed) {
                throw new IllegalArgumentException(
                        "调用目标不在白名单内，仅允许: " + String.join(", ", whitelist == null ? new String[0] : whitelist));
            }
        }
    }

    public static Class<?> resolveClass(String className, String[] whitelist) throws ClassNotFoundException {
        if (className.contains(".")) {
            boolean allowed = false;
            if (whitelist != null) {
                for (String prefix : whitelist) {
                    if (prefix != null && className.startsWith(prefix)) {
                        allowed = true;
                        break;
                    }
                }
            }
            if (!allowed) {
                throw new ClassNotFoundException("调用目标不在白名单包内: " + className);
            }
            return Class.forName(className);
        }
        if (whitelist != null) {
            for (String prefix : whitelist) {
                if (prefix == null || prefix.isBlank()) {
                    continue;
                }
                try {
                    return Class.forName(prefix + "." + className);
                } catch (ClassNotFoundException ignored) {
                    // try next whitelist package
                }
            }
        }
        throw new ClassNotFoundException("白名单包中找不到任务类: " + className);
    }

    public static Parsed parse(String invokeTarget) {
        Matcher matcher = TARGET.matcher(invokeTarget.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("调用目标格式错误，应为: 类名.方法名()，例如 CleanOperLogTask.cleanOperLog()");
        }
        return new Parsed(matcher.group(1), matcher.group(2), parseArgs(matcher.group(3)));
    }

    public static Method resolveMethod(Class<?> type, String methodName, List<Object> args) throws NoSuchMethodException {
        Class<?>[] paramTypes = new Class<?>[args.size()];
        for (int i = 0; i < args.size(); i++) {
            Object arg = args.get(i);
            if (arg instanceof Integer) {
                paramTypes[i] = int.class;
            } else if (arg instanceof Long) {
                paramTypes[i] = long.class;
            } else if (arg instanceof Boolean) {
                paramTypes[i] = boolean.class;
            } else {
                paramTypes[i] = arg.getClass();
            }
        }
        return type.getMethod(methodName, paramTypes);
    }

    private static List<Object> parseArgs(String raw) {
        String text = raw == null ? "" : raw.trim();
        if (text.isEmpty()) {
            return List.of();
        }
        if (text.indexOf('(') >= 0 || text.indexOf(')') >= 0) {
            throw new IllegalArgumentException("调用目标参数不支持嵌套调用");
        }
        List<Object> args = new ArrayList<>();
        for (String part : text.split(",")) {
            String token = part.trim();
            if (token.isEmpty()) {
                throw new IllegalArgumentException("调用目标参数不能为空");
            }
            args.add(parseArg(token));
        }
        return args;
    }

    private static Object parseArg(String token) {
        if (("true".equals(token) || "false".equals(token))) {
            return Boolean.valueOf(token);
        }
        if (token.length() >= 2
                && ((token.startsWith("'") && token.endsWith("'"))
                        || (token.startsWith("\"") && token.endsWith("\"")))) {
            return token.substring(1, token.length() - 1);
        }
        if (token.matches("-?\\d+L")) {
            return Long.valueOf(token.substring(0, token.length() - 1));
        }
        if (token.matches("-?\\d+")) {
            return Integer.valueOf(token);
        }
        throw new IllegalArgumentException("调用目标仅支持数字、布尔或引号字符串参数: " + token);
    }
}
