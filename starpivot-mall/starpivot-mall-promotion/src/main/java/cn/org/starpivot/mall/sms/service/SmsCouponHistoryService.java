package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.CouponHistoryReqBo;
import cn.org.starpivot.mall.sms.domain.vo.CouponHistoryVo;

/**
 * Couponhistoryservice服务接口。
 * <p>
 * 封装优惠券领取历史相关业务逻辑。
 * </p>
 */

public interface SmsCouponHistoryService {

    /**
     * 分页查询列表。
     */
    PageResponse<CouponHistoryVo> pageList(CouponHistoryReqBo reqBo);
}
