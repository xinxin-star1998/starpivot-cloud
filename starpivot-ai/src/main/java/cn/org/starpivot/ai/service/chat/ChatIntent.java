package cn.org.starpivot.ai.service.chat;

/**
 * 用户消息意图分类（规则路由 + 可选 LLM 校准）。
 */
public enum ChatIntent {

    /** 寒暄、致谢等 */
    CHITCHAT,

    /** 产品/操作/文档类问答，适合 RAG */
    KNOWLEDGE,

    /** 编程、调试、代码审查 */
    DEVELOPER,

    /** 数据分析、报表指标 */
    ANALYST,

    /** 复杂推理、架构设计 */
    REASONING,

    /** 未命中特定规则 */
    GENERAL;

    /** 适合在多轮追问中沿用的任务型意图（寒暄/写作等不沿用） */
    public boolean isSticky() {
        return this == KNOWLEDGE || this == DEVELOPER || this == ANALYST || this == REASONING;
    }
}
