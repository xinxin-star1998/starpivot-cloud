package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.CouponReqBo;
import cn.org.starpivot.mall.sms.domain.bo.CouponSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.CouponVo;
import java.util.List;

public interface SmsCouponService {

    PageResponse<CouponVo> pageList(CouponReqBo reqBo);

    CouponVo getById(Long id);

    void add(CouponSaveBo bo);

    void update(CouponSaveBo bo);

    void removeByIds(List<Long> ids);
}
