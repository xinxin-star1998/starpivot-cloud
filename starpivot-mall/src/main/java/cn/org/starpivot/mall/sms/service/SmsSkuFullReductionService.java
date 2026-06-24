package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.SkuFullReductionReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SkuFullReductionSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SkuFullReductionVo;
import java.util.List;

public interface SmsSkuFullReductionService {

    PageResponse<SkuFullReductionVo> pageList(SkuFullReductionReqBo reqBo);

    SkuFullReductionVo getById(Long id);

    void add(SkuFullReductionSaveBo bo);

    void update(SkuFullReductionSaveBo bo);

    void removeByIds(List<Long> ids);
}
