package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.OmsDeliverBo;
import cn.org.starpivot.mall.oms.domain.bo.OmsOrderCloseBo;
import cn.org.starpivot.mall.oms.domain.bo.OmsOrderReqBo;
import cn.org.starpivot.mall.oms.domain.vo.OmsOrderVo;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Orderservice服务接口。
 * <p>
 * 封装订单相关业务逻辑。
 * </p>
 */

public interface OmsOrderService extends IService<OmsOrder> {

    /**
     * 分页查询列表。
     */
    PageResponse<OmsOrderVo> pageList(OmsOrderReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    OmsOrderVo getDetailById(Long id);

    /**
     * 订单发货。
     */
    void deliver(OmsDeliverBo bo);

    /**
     * 关闭订单。
     */
    void close(OmsOrderCloseBo bo);
}
