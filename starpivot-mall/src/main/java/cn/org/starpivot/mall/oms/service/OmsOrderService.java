package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.OmsDeliverBo;
import cn.org.starpivot.mall.oms.domain.bo.OmsOrderCloseBo;
import cn.org.starpivot.mall.oms.domain.bo.OmsOrderReqBo;
import cn.org.starpivot.mall.oms.domain.vo.OmsOrderVo;
import cn.org.starpivot.mall.oms.entity.OmsOrder;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OmsOrderService extends IService<OmsOrder> {

    PageResponse<OmsOrderVo> pageList(OmsOrderReqBo reqBo);

    OmsOrderVo getDetailById(Long id);

    void deliver(OmsDeliverBo bo);

    void close(OmsOrderCloseBo bo);
}
