package cn.org.starpivot.ai.service;

import cn.org.starpivot.ai.domain.dto.AiKnowledgeBaseQueryDto;
import cn.org.starpivot.ai.domain.dto.AiKnowledgeBaseSaveDto;
import cn.org.starpivot.ai.domain.vo.AiKnowledgeBaseVo;
import cn.org.starpivot.common.entity.PageResponse;

import java.util.List;

public interface AiKnowledgeBaseService {

    PageResponse<AiKnowledgeBaseVo> pageList(AiKnowledgeBaseQueryDto query);

    List<AiKnowledgeBaseVo> listEnabled();

    AiKnowledgeBaseVo getById(Long kbId);

    Long save(AiKnowledgeBaseSaveDto dto);

    void remove(Long kbId);
}
