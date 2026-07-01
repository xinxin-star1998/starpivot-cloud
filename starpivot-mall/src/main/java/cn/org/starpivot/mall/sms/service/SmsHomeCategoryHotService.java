package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.HomeCategoryHotReqBo;
import cn.org.starpivot.mall.sms.domain.bo.HomeCategoryHotSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.HomeCategoryHotVo;
import java.util.List;

public interface SmsHomeCategoryHotService {

    PageResponse<HomeCategoryHotVo> pageList(HomeCategoryHotReqBo reqBo);

    HomeCategoryHotVo getById(Long id);

    void add(HomeCategoryHotSaveBo bo);

    void update(HomeCategoryHotSaveBo bo);

    void removeByIds(List<Long> ids);

    List<HomeCategoryHotVo> listActive();
}
