package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.SpuBoundsReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SpuBoundsSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SpuBoundsVo;
import java.util.List;

public interface SmsSpuBoundsService {

    PageResponse<SpuBoundsVo> pageList(SpuBoundsReqBo reqBo);

    SpuBoundsVo getById(Long id);

    SpuBoundsVo getBySpuId(Long spuId);

    void add(SpuBoundsSaveBo bo);

    void update(SpuBoundsSaveBo bo);

    void removeByIds(List<Long> ids);
}
