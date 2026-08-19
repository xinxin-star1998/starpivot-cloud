package cn.org.starpivot.ai.provider;

import cn.org.starpivot.ai.domain.vo.AiProviderModelVo;
import cn.org.starpivot.ai.domain.vo.AiProviderPresetVo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiProviderPresetCatalog {

    public List<AiProviderPresetVo> list() {
        return List.of(deepseek(), kimi(), dashscope(), openai(), zhipu(), siliconflow(), custom());
    }

    public AiProviderPresetVo find(String code) {
        if (code == null) {
            return custom();
        }
        return list().stream()
                .filter(item -> code.equalsIgnoreCase(item.getProviderCode()))
                .findFirst()
                .orElse(custom());
    }

    private static AiProviderPresetVo deepseek() {
        return AiProviderPresetVo.builder()
                .providerCode("deepseek")
                .providerName("DeepSeek")
                .baseUrl("https://api.deepseek.com")
                .chatEnabled("0")
                .embeddingEnabled("1")
                .rerankEnabled("1")
                .defaultChatModel("deepseek-chat")
                .models(List.of(
                        model("deepseek-chat", "DeepSeek Chat（便宜）", "chat"),
                        model("deepseek-reasoner", "DeepSeek Reasoner（推理）", "chat"),
                        model("deepseek-v4-flash", "DeepSeek V4 Flash", "chat"),
                        model("deepseek-v4-pro", "DeepSeek V4 Pro（较贵）", "chat")))
                .remark("OpenAI 兼容。日常默认 deepseek-chat；推理用 deepseek-reasoner。V4 更贵。")
                .build();
    }

    private static AiProviderPresetVo kimi() {
        return AiProviderPresetVo.builder()
                .providerCode("kimi")
                .providerName("Kimi（月之暗面）")
                .baseUrl("https://api.moonshot.cn")
                .chatEnabled("0")
                .embeddingEnabled("1")
                .rerankEnabled("1")
                .defaultChatModel("kimi-k3")
                .models(List.of(
                        model("kimi-k3", "Kimi K3", "chat"),
                        model("kimi-k2.6", "Kimi K2.6", "chat"),
                        model("kimi-k2.7-code", "Kimi K2.7 Code", "chat"),
                        model("kimi-k2.7-code-highspeed", "Kimi K2.7 Code 高速", "chat")))
                .remark("OpenAI 兼容。官方推荐 kimi-k3；moonshot-v1 / kimi-k2-0711 已停用。")
                .build();
    }

    private static AiProviderPresetVo dashscope() {
        return AiProviderPresetVo.builder()
                .providerCode("dashscope")
                .providerName("阿里百炼（通义）")
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode")
                .rerankEndpoint("https://dashscope.aliyuncs.com/api/v1/services/rerank/text-rerank/text-rerank")
                .chatEnabled("0")
                .embeddingEnabled("0")
                .rerankEnabled("0")
                .defaultChatModel("qwen-plus")
                .defaultEmbeddingModel("text-embedding-v3")
                .defaultRerankModel("gte-rerank")
                .models(List.of(
                        model("qwen3.8-max", "通义千问 3.8 Max", "chat"),
                        model("qwen3.7-max", "通义千问 3.7 Max", "chat"),
                        model("qwen3.7-plus", "通义千问 3.7 Plus", "chat"),
                        model("qwen3.7-flash", "通义千问 3.7 Flash", "chat"),
                        model("qwen3-coder-plus", "通义千问 Coder Plus", "chat"),
                        model("qwen-plus", "通义千问 Plus", "chat"),
                        model("qwen-flash", "通义千问 Flash", "chat"),
                        model("text-embedding-v3", "Embedding v3", "embedding"),
                        model("gte-rerank", "GTE Rerank", "rerank")))
                .remark("对话/向量走 compatible-mode（不要加 /v1）。重排走 DashScope rerank 接口。")
                .build();
    }

    private static AiProviderPresetVo openai() {
        return AiProviderPresetVo.builder()
                .providerCode("openai")
                .providerName("OpenAI")
                .baseUrl("https://api.openai.com")
                .chatEnabled("0")
                .embeddingEnabled("0")
                .rerankEnabled("1")
                .defaultChatModel("gpt-4.1")
                .defaultEmbeddingModel("text-embedding-3-small")
                .models(List.of(
                        model("gpt-4.1", "GPT-4.1", "chat"),
                        model("gpt-4.1-mini", "GPT-4.1 mini", "chat"),
                        model("gpt-4o", "GPT-4o", "chat"),
                        model("gpt-4o-mini", "GPT-4o mini", "chat"),
                        model("text-embedding-3-small", "Embedding 3 Small", "embedding"),
                        model("text-embedding-3-large", "Embedding 3 Large", "embedding")))
                .remark("官方 OpenAI。base-url 不要带 /v1。")
                .build();
    }

    private static AiProviderPresetVo zhipu() {
        return AiProviderPresetVo.builder()
                .providerCode("zhipu")
                .providerName("智谱 GLM")
                .baseUrl("https://open.bigmodel.cn/api/paas/v4")
                .completionsPath("/chat/completions")
                .embeddingsPath("/embeddings")
                .chatEnabled("0")
                .embeddingEnabled("0")
                .rerankEnabled("1")
                .defaultChatModel("glm-5")
                .defaultEmbeddingModel("embedding-3")
                .models(List.of(
                        model("glm-5", "GLM-5", "chat"),
                        model("glm-5-turbo", "GLM-5 Turbo", "chat"),
                        model("glm-4.7-flash", "GLM-4.7 Flash", "chat"),
                        model("glm-4.5-air", "GLM-4.5 Air", "chat"),
                        model("embedding-3", "Embedding 3", "embedding")))
                .remark("智谱接口已含 /v4，需自定义 completions/embeddings 路径。")
                .build();
    }

    private static AiProviderPresetVo siliconflow() {
        return AiProviderPresetVo.builder()
                .providerCode("siliconflow")
                .providerName("硅基流动")
                .baseUrl("https://api.siliconflow.cn")
                .chatEnabled("0")
                .embeddingEnabled("0")
                .rerankEnabled("1")
                .defaultChatModel("Qwen/Qwen3-8B-Instruct")
                .defaultEmbeddingModel("BAAI/bge-m3")
                .models(List.of(
                        model("Qwen/Qwen3-8B-Instruct", "Qwen3 8B", "chat"),
                        model("deepseek-ai/DeepSeek-V3", "DeepSeek V3", "chat"),
                        model("BAAI/bge-m3", "bge-m3", "embedding")))
                .remark("OpenAI 兼容聚合网关。模型 ID 以控制台为准。")
                .build();
    }

    private static AiProviderPresetVo custom() {
        return AiProviderPresetVo.builder()
                .providerCode("custom")
                .providerName("自定义 OpenAI 兼容")
                .baseUrl("https://")
                .chatEnabled("0")
                .embeddingEnabled("1")
                .rerankEnabled("1")
                .models(List.of())
                .remark("任意 OpenAI 兼容网关。base-url 一般不要带 /v1。")
                .build();
    }

    private static AiProviderModelVo model(String id, String label, String kind) {
        return AiProviderModelVo.builder().id(id).label(label).kind(kind).build();
    }
}
