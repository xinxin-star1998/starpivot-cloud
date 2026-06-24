package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.HomeAdvReqBo;
import cn.org.starpivot.mall.sms.domain.bo.HomeAdvSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.HomeAdvVo;
import java.util.List;

public interface SmsHomeAdvService {

    PageResponse<HomeAdvVo> pageList(HomeAdvReqBo reqBo);

    HomeAdvVo getById(Long id);

    void add(HomeAdvSaveBo bo);

    void update(HomeAdvSaveBo bo);

    void removeByIds(List<Long> ids);
}
