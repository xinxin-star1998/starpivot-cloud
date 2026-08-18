package cn.org.starpivot.ai.service;

import cn.org.starpivot.ai.domain.dto.AiProviderQueryDto;
import cn.org.starpivot.ai.domain.dto.AiProviderSaveDto;
import cn.org.starpivot.ai.domain.entity.AiProvider;
import cn.org.starpivot.ai.domain.vo.AiModelVo;
import cn.org.starpivot.ai.domain.vo.AiProviderPresetVo;
import cn.org.starpivot.ai.domain.vo.AiProviderVo;
import cn.org.starpivot.common.entity.PageResponse;

import java.util.List;

public interface AiProviderService {

    PageResponse<AiProviderVo> pageList(AiProviderQueryDto query);

    AiProviderVo getById(Long providerId);

    Long save(AiProviderSaveDto dto);

    void remove(Long providerId);

    void setDefault(Long providerId, String kind);

    List<AiProviderPresetVo> presets();

    List<AiModelVo> listChatModels();

    String testConnection(Long providerId, String kind);

    void refreshRuntime();

    List<AiProvider> listEnabled();

    AiProvider findChatProvider(String model);

    /** 若 requested 属于该供应商则原样返回裸模型 ID，否则回退到该供应商默认对话模型 */
    String resolveChatModel(AiProvider provider, String requested);

    /** 排除指定供应商后的下一个可用对话供应商（故障切换） */
    AiProvider findFailoverChatProvider(Long excludeProviderId);

    AiProvider findDefaultChatProvider();

    AiProvider findDefaultEmbeddingProvider();

    AiProvider findDefaultRerankProvider();
}
