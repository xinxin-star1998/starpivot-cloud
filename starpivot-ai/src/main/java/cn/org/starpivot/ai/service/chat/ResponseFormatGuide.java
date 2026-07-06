package cn.org.starpivot.ai.service.chat;

import org.springframework.util.StringUtils;

/**
 * 各对话场景统一的 Markdown 回答格式约束，追加在 system prompt 末尾。
 */
public final class ResponseFormatGuide {

    private static final String BASE = """
            ## 回答格式（务必遵守）
            - 开门见山：首段用 1～2 句话直接回答核心问题，不要寒暄或复述用户原话
            - 篇幅：简单问题 2～5 行；复杂问题用分层结构，单段不超过 4 行
            - 强调：关键术语、菜单名、按钮名、配置项用行内 `代码` 或 **加粗**
            - 代码与命令：单独使用 ```语言 围栏，围栏前后各空一行，不要把代码挤在正文同一行
            - 代码块内必须保留规范空格与缩进（如 `public static void`，禁止 `publicstaticvoid` 连写）
            - 运算符两侧加空格（`i = 0`、`i < n`），花括号与分号按常规 Java/JS 风格换行，块内 2 空格缩进
            - 列表：多个并列要点用无序列表；操作步骤必须用有序列表，每步独占一行
            - 对比信息：优先用 Markdown 表格；表头下一行必须是分隔行（如 | --- | --- |），每行表格首尾都要有 |
            - 补充说明：次要提示放在 blockquote（> 开头）或文末「**提示**」一小段
            - 诚实：不确定时明确说明，不要编造；不要输出「根据资料1」等内部标记
            """;

    private static final String SUPPORT_EXTRA = """
            - 操作指引：菜单路径写为「一级 → 二级 → 三级」，步骤用 1. 2. 3.
            - 有参考资料时自然融入表述，无需标注资料编号
            - 资料不足：首句说明「暂未找到相关说明」，再给 2～3 条可尝试的排查方向
            """;

    private static final String DEVELOPER_EXTRA = """
            - 结构：先结论 → 再代码示例 → 最后简短原理或注意事项
            - 代码需完整可理解，注明语言；关键字、类型、标识符之间必须有空格，每行一条语句、块内缩进 2 空格
            - 涉及版本差异时写明
            - 排错：用「现象 → 原因 → 处理」；命令与配置项分行展示
            """;

    private static final String ANALYST_EXTRA = """
            - 固定结构：**结论** → **依据** → **建议**（三段，可用小标题）
            - 涉及数字必须说明指标定义与时间范围；缺数据时列出需要哪些字段
            - SQL 或计算思路用 ```sql 围栏，并附简短注释
            """;

    private static final String DEFAULT_EXTRA = """
            - 翻译：保留原文结构，必要时附术语对照
            - 写作：先列大纲再展开，或使用分节小标题
            """;

    private static final String RAG_INSTRUCTION = """
            ## 参考资料使用方式
            下文为检索到的参考资料。请优先依据资料作答，将内容改写成面向用户的清晰表述。
            不要逐条照搬资料前缀（如【资料1】），不要编造资料中不存在的事实。
            """;

    private ResponseFormatGuide() {
    }

    public static String forScene(String promptScene) {
        String scene = StringUtils.hasText(promptScene) ? promptScene.trim() : "default";
        String extra = switch (scene) {
            case "support" -> SUPPORT_EXTRA;
            case "developer" -> DEVELOPER_EXTRA;
            case "analyst" -> ANALYST_EXTRA;
            default -> DEFAULT_EXTRA;
        };
        return BASE + extra;
    }

    public static String ragInstruction() {
        return RAG_INSTRUCTION;
    }
}
