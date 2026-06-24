package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.SeckillPromotionReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SeckillPromotionSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SeckillPromotionVo;
import java.util.List;

public interface SmsSeckillPromotionService {

    PageResponse<SeckillPromotionVo> pageList(SeckillPromotionReqBo reqBo);

    SeckillPromotionVo getById(Long id);

    void add(SeckillPromotionSaveBo bo);

    void update(SeckillPromotionSaveBo bo);

    void removeByIds(List<Long> ids);
}
