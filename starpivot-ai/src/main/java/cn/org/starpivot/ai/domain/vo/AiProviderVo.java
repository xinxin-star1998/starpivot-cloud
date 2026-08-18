package cn.org.starpivot.ai.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiProviderVo {

    private Long providerId;

    private String providerCode;

    private String providerName;

    private String baseUrl;

    /** 脱敏后的 Key，不回传明文 */
    private String apiKeyMasked;

    private boolean apiKeyConfigured;

    private String completionsPath;

    private String embeddingsPath;

    private String rerankEndpoint;

    private String chatEnabled;

    private String embeddingEnabled;

    private String rerankEnabled;

    private String defaultChatModel;

    private String defaultEmbeddingModel;

    private String defaultRerankModel;

    private List<AiProviderModelVo> models = new ArrayList<>();

    private String isDefaultChat;

    private String isDefaultEmbedding;

    private String isDefaultRerank;

    private String status;

    private String remark;

    private String updateBy;

    private LocalDateTime updateTime;
}
