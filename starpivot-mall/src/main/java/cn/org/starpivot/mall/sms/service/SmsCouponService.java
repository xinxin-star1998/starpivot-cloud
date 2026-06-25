package cn.org.starpivot.mall.sms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.sms.domain.bo.CouponReqBo;
import cn.org.starpivot.mall.sms.domain.bo.CouponSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.CouponVo;

import java.util.List;

/**
 * Couponservice服务接口。
 * <p>
 * 封装优惠券相关业务逻辑。
 * </p>
 */

public interface SmsCouponService {

    /**
     * 分页查询列表。
     */
    PageResponse<CouponVo> pageList(CouponReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    CouponVo getById(Long id);

    /**
     * 新增记录。
     */
    void add(CouponSaveBo bo);

    /**
     * 修改记录。
     */
    void update(CouponSaveBo bo);

    /**
     * 批量删除。
     */
    void removeByIds(List<Long> ids);

    /**
     * 更新发布状态（上架/下架）。
     */
    void updatePublishStatus(Long id, Integer publish);
}
