package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.CouponHistoryReqBo;
import cn.org.starpivot.mall.sms.domain.vo.CouponHistoryVo;

public interface SmsCouponHistoryService {

    PageResponse<CouponHistoryVo> pageList(CouponHistoryReqBo reqBo);
}
