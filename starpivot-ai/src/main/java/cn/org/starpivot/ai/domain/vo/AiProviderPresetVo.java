package cn.org.starpivot.ai.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiProviderPresetVo {

    private String providerCode;

    private String providerName;

    private String baseUrl;

    private String completionsPath;

    private String embeddingsPath;

    private String rerankEndpoint;

    private String chatEnabled;

    private String embeddingEnabled;

    private String rerankEnabled;

    private String defaultChatModel;

    private String defaultEmbeddingModel;

    private String defaultRerankModel;

    @Builder.Default
    private List<AiProviderModelVo> models = new ArrayList<>();

    private String remark;
}
