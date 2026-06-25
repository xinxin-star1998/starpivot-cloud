package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.ReturnAuditBo;
import cn.org.starpivot.mall.oms.domain.bo.ReturnReqBo;
import cn.org.starpivot.mall.oms.domain.vo.ReturnVo;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Orderreturnapplyservice服务接口。
 * <p>
 * 封装退货申请相关业务逻辑。
 * </p>
 */

public interface OmsOrderReturnApplyService extends IService<OmsOrderReturnApply> {

    /**
     * 分页查询列表。
     */
    PageResponse<ReturnVo> pageList(ReturnReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    ReturnVo getDetailById(Long id);

    /**
     * audit。
     */
    void audit(ReturnAuditBo bo);

    /** 完成退货：入库、退款记录、关闭订单 */
    void completeReturn(Long id);
}
