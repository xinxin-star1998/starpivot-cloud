package cn.org.starpivot.ai.config;

import cn.org.starpivot.ai.domain.vo.AiModelVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "starpivot.ai")
public class AiProperties {

    private String systemDbSchema = "star_pivot";

    /** 全局系统提示词，定义 AI 助手的行为与能力边界 */
    private String systemPrompt =
            """
            你是一个智能 AI 助手，能够进行自然、流畅的多轮对话。
            你可以帮助用户完成写作、翻译、编程、分析、头脑风暴、学习辅导等广泛任务。
            请根据用户问题选择合适的方式回答；不确定时请诚实说明，不要编造。
            """;

    /** 欢迎语模板，支持 {botName} 占位符 */
    private String welcomeMessage = "你好！我是 **{botName}**，有什么我可以帮你的吗？";

    /** 每个会话保留的最大消息条数 */
    private int maxMemoryMessages = 20;

    /** 助手显示名称 */
    private String botName = "AI 助手";

    /** 助手头像地址（完整 URL，留空则前端使用默认头像） */
    private String botAvatar = "";

    /** 可选模型列表，供前端切换；留空则仅使用 defaultModel */
    private List<ModelOption> models = new ArrayList<>();

    /** 默认模型 ID，需与 spring.ai.openai.chat.options.model 保持一致 */
    private String defaultModel = "deepseek-chat";

    /** 默认采样温度 */
    private Double defaultTemperature = 0.7;

    /** 默认 Prompt 场景 ID */
    private String defaultPromptScene = "default";

    /** 场景化 System Prompt 模板；id=default 可被后台 ai_config.system_prompt 覆盖 */
    private List<PromptTemplateOption> promptTemplates = new ArrayList<>();

    /** 对话接口限流（Redis 滑动窗口，按用户） */
    private RateLimitProperties rateLimit = new RateLimitProperties();

    /** RAG 基础设施开关（Nacos）；需与后台 ai_config.rag_enabled 同时为开才生效 */
    private RagProperties rag = new RagProperties();

    /** 查询路由：自动选择场景、模型与是否 RAG */
    private QueryRouterProperties queryRouter = new QueryRouterProperties();

    public String validatedSystemDbSchema() {
        if (!StringUtils.hasText(systemDbSchema)) {
            return "star_pivot";
        }
        return systemDbSchema.trim();
    }

    public String resolvedBotName() {
        return StringUtils.hasText(botName) ? botName.trim() : "AI 助手";
    }

    public String resolvedWelcomeMessage() {
        String template =
                StringUtils.hasText(welcomeMessage)
                        ? welcomeMessage.trim()
                        : "你好！我是 **{botName}**，有什么我可以帮你的吗？";
        return template.replace("{botName}", resolvedBotName());
    }

    public List<AiModelVo> resolvedModels() {
        if (models == null || models.isEmpty()) {
            if (StringUtils.hasText(defaultModel)) {
                return List.of(AiModelVo.builder()
                        .id(defaultModel.trim())
                        .label(defaultModel.trim())
                        .build());
            }
            return List.of();
        }
        return models.stream()
                .filter(option -> option != null && StringUtils.hasText(option.getId()))
                .map(option -> AiModelVo.builder()
                        .id(option.getId().trim())
                        .label(StringUtils.hasText(option.getLabel())
                                ? option.getLabel().trim()
                                : option.getId().trim())
                        .build())
                .toList();
    }

    public String resolvedDefaultModel() {
        if (StringUtils.hasText(defaultModel)) {
            return defaultModel.trim();
        }
        List<AiModelVo> available = resolvedModels();
        return available.isEmpty() ? null : available.get(0).getId();
    }

    public boolean isModelAllowed(String model) {
        if (!StringUtils.hasText(model)) {
            return true;
        }
        List<AiModelVo> available = resolvedModels();
        if (available.isEmpty()) {
            return true;
        }
        String normalized = model.trim();
        return available.stream().anyMatch(item -> normalized.equals(item.getId()));
    }

    public List<AiPromptTemplateSnapshot> resolvedPromptTemplates(String dbSystemPromptOverride) {
        List<PromptTemplateOption> source = promptTemplates != null ? promptTemplates : List.of();
        if (source.isEmpty()) {
            String prompt = StringUtils.hasText(dbSystemPromptOverride) ? dbSystemPromptOverride : systemPrompt;
            return List.of(AiPromptTemplateSnapshot.builder()
                    .id("default")
                    .label("通用助手")
                    .description("默认对话场景")
                    .prompt(prompt)
                    .build());
        }
        return source.stream()
                .filter(option -> option != null && StringUtils.hasText(option.getId()))
                .map(option -> {
                    String id = option.getId().trim();
                    String prompt = option.getPrompt();
                    if ("default".equals(id) && StringUtils.hasText(dbSystemPromptOverride)) {
                        prompt = dbSystemPromptOverride;
                    } else if (!StringUtils.hasText(prompt)) {
                        prompt = systemPrompt;
                    }
                    return AiPromptTemplateSnapshot.builder()
                            .id(id)
                            .label(StringUtils.hasText(option.getLabel()) ? option.getLabel().trim() : id)
                            .description(option.getDescription())
                            .prompt(prompt)
                            .temperature(option.getTemperature())
                            .model(StringUtils.hasText(option.getModel()) ? option.getModel().trim() : null)
                            .build();
                })
                .toList();
    }

    public String resolvedDefaultPromptScene() {
        return StringUtils.hasText(defaultPromptScene) ? defaultPromptScene.trim() : "default";
    }

    @Data
    public static class PromptTemplateOption {

        private String id;

        private String label;

        private String description;

        private String prompt;

        /** 该场景推荐温度，未设则使用全局 defaultTemperature */
        private Double temperature;

        /** 该场景推荐模型，未设则使用全局 defaultModel */
        private String model;
    }

    @Data
    public static class ModelOption {

        private String id;

        private String label;
    }

    @Data
    public static class RateLimitProperties {

        private boolean enabled = true;

        /** 每用户每分钟最大对话请求数（send + stream 合计） */
        private int maxRequestsPerMinute = 30;
    }

    @Data
    public static class QueryRouterProperties {

        private boolean enabled = true;

        /** 常规模型（轻量对话） */
        private String chatModel = "deepseek-chat";

        /** 推理模型（复杂分析/架构） */
        private String reasonerModel = "deepseek-reasoner";

        public String resolvedChatModel(String fallback) {
            return StringUtils.hasText(chatModel) ? chatModel.trim() : fallback;
        }

        public String resolvedReasonerModel(String fallback) {
            return StringUtils.hasText(reasonerModel) ? reasonerModel.trim() : fallback;
        }
    }

    @Data
    public static class RagProperties {

        /** 是否允许 RAG 检索（需配置可用的 Embedding API） */
        private boolean enabled = false;

        private int vectorTopK = 20;

        private int fulltextTopK = 20;

        /** 混合检索候选池大小（精排前） */
        private int retrieveTopK = 20;

        private double minVectorScore = 0.3;

        /** 向量扫描每批加载条数；0 表示使用默认 2000 */
        private int vectorScanBatchSize = 2000;

        /** 向量扫描最大 chunk 数；0 表示不限制（全量扫描） */
        private int vectorScanMaxChunks = 0;

        private String embeddingCacheTtl = "7d";

        /** HyDE：用假设性回答增强向量检索 */
        private boolean hydeEnabled = false;

        private RerankerProperties reranker = new RerankerProperties();
    }

    @Data
    public static class RerankerProperties {

        private boolean enabled = false;

        /** DashScope 兼容：/api/v1/services/rerank/text-rerank/text-rerank */
        private String endpoint = "https://dashscope.aliyuncs.com/api/v1/services/rerank/text-rerank/text-rerank";

        private String apiKey = "";

        private String model = "gte-rerank";

        private long timeoutMs = 800;
    }
}
