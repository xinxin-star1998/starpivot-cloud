package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.RefundReqBo;
import cn.org.starpivot.mall.oms.domain.vo.RefundVo;
import cn.org.starpivot.mall.oms.entity.OmsRefundInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Refundinfoservice服务接口。
 * <p>
 * 封装退款信息相关业务逻辑。
 * </p>
 */

public interface OmsRefundInfoService extends IService<OmsRefundInfo> {

    /**
     * 分页查询列表。
     */
    PageResponse<RefundVo> pageList(RefundReqBo reqBo);

    /**
     * 退款详情。
     */
    RefundVo getDetailById(Long id);
}
