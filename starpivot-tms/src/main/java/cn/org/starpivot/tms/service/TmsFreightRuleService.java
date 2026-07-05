package cn.org.starpivot.tms.service;

import cn.org.starpivot.api.tms.dto.FreightCalculateRequest;
import cn.org.starpivot.api.tms.dto.FreightCalculateResult;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.tms.domain.dto.TmsFreightRuleQueryDto;
import cn.org.starpivot.tms.domain.dto.TmsFreightRuleSaveDto;
import cn.org.starpivot.tms.domain.vo.TmsFreightRuleVo;

public interface TmsFreightRuleService {

    PageResponse<TmsFreightRuleVo> pageList(TmsFreightRuleQueryDto query);

    Long save(TmsFreightRuleSaveDto dto);

    void remove(Long id);

    FreightCalculateResult calculate(FreightCalculateRequest request);
}
