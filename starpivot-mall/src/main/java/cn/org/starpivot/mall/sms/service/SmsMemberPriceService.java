package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.MemberPriceReqBo;
import cn.org.starpivot.mall.sms.domain.bo.MemberPriceSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.MemberPriceVo;
import java.util.List;

public interface SmsMemberPriceService {

    PageResponse<MemberPriceVo> pageList(MemberPriceReqBo reqBo);

    MemberPriceVo getById(Long id);

    void add(MemberPriceSaveBo bo);

    void update(MemberPriceSaveBo bo);

    void removeByIds(List<Long> ids);
}
