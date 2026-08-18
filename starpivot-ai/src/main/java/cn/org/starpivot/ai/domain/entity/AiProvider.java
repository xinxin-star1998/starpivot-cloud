package cn.org.starpivot.ai.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_provider")
public class AiProvider {

    @TableId(type = IdType.AUTO)
    private Long providerId;

    /** 供应商编码：deepseek/kimi/dashscope/openai/zhipu/siliconflow/custom */
    private String providerCode;

    private String providerName;

    private String baseUrl;

    private String apiKey;

    /** OpenAI 兼容 completions 路径，空则使用 /v1/chat/completions */
    private String completionsPath;

    /** OpenAI 兼容 embeddings 路径，空则使用 /v1/embeddings */
    private String embeddingsPath;

    /** DashScope rerank 等非 OpenAI 兼容地址 */
    private String rerankEndpoint;

    /** 是否用于对话：0是 1否 */
    private String chatEnabled;

    /** 是否用于向量：0是 1否 */
    private String embeddingEnabled;

    /** 是否用于重排序：0是 1否 */
    private String rerankEnabled;

    private String defaultChatModel;

    private String defaultEmbeddingModel;

    private String defaultRerankModel;

    /** [{id,label,kind}] kind=chat|embedding|rerank */
    private String modelsJson;

    /** 默认对话供应商：0是 1否 */
    private String isDefaultChat;

    /** 默认向量供应商：0是 1否 */
    private String isDefaultEmbedding;

    /** 默认重排供应商：0是 1否 */
    private String isDefaultRerank;

    /** 状态：0正常 1停用 */
    private String status;

    private String remark;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
