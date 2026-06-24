package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.SkuLadderReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SkuLadderSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SkuLadderVo;
import java.util.List;

public interface SmsSkuLadderService {

    PageResponse<SkuLadderVo> pageList(SkuLadderReqBo reqBo);

    SkuLadderVo getById(Long id);

    void add(SkuLadderSaveBo bo);

    void update(SkuLadderSaveBo bo);

    void removeByIds(List<Long> ids);
}
