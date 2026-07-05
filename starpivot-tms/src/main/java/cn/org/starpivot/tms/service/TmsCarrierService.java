package cn.org.starpivot.tms.service;

import cn.org.starpivot.tms.domain.dto.TmsCarrierQueryDto;
import cn.org.starpivot.tms.domain.dto.TmsCarrierSaveDto;
import cn.org.starpivot.tms.domain.vo.TmsCarrierVo;
import cn.org.starpivot.common.entity.PageResponse;

import java.util.List;

public interface TmsCarrierService {

    PageResponse<TmsCarrierVo> pageList(TmsCarrierQueryDto query);

    List<TmsCarrierVo> listEnabled();

    Long save(TmsCarrierSaveDto dto);

    void remove(Long id);

    TmsCarrierVo requireEnabledCarrier(Long carrierId);
}
