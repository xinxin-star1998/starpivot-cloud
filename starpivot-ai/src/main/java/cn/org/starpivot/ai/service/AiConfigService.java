package cn.org.starpivot.ai.service;

import cn.org.starpivot.ai.domain.dto.AiConfigQueryDto;
import cn.org.starpivot.ai.domain.dto.AiConfigSaveDto;
import cn.org.starpivot.ai.domain.vo.AiConfigVo;
import cn.org.starpivot.common.entity.PageResponse;

public interface AiConfigService {

    PageResponse<AiConfigVo> pageList(AiConfigQueryDto query);

    AiConfigVo getById(Long configId);

    Long save(AiConfigSaveDto dto);

    void remove(Long configId);

    void setDefault(Long configId);
}
