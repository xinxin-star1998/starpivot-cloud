package cn.org.starpivot.ai.domain.dto;

import cn.org.starpivot.ai.domain.vo.AiProviderModelVo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AiProviderSaveDto {

    private Long providerId;

    @NotBlank(message = "供应商编码不能为空")
    @Size(max = 32, message = "供应商编码不能超过32字")
    private String providerCode;

    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 64, message = "供应商名称不能超过64字")
    private String providerName;

    @NotBlank(message = "API 地址不能为空")
    @Size(max = 256, message = "API 地址不能超过256字")
    private String baseUrl;

    /** 留空或带 **** 表示不修改已有 Key */
    @Size(max = 512, message = "API Key 不能超过512字")
    private String apiKey;

    @Size(max = 128, message = "对话路径不能超过128字")
    private String completionsPath;

    @Size(max = 128, message = "向量路径不能超过128字")
    private String embeddingsPath;

    @Size(max = 256, message = "重排地址不能超过256字")
    private String rerankEndpoint;

    private String chatEnabled;

    private String embeddingEnabled;

    private String rerankEnabled;

    @Size(max = 64, message = "默认对话模型不能超过64字")
    private String defaultChatModel;

    @Size(max = 64, message = "默认向量模型不能超过64字")
    private String defaultEmbeddingModel;

    @Size(max = 64, message = "默认重排模型不能超过64字")
    private String defaultRerankModel;

    private List<AiProviderModelVo> models = new ArrayList<>();

    private String isDefaultChat;

    private String isDefaultEmbedding;

    private String isDefaultRerank;

    private String status;

    @Size(max = 500, message = "备注不能超过500字")
    private String remark;
}
