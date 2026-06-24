package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.PaymentReqBo;
import cn.org.starpivot.mall.oms.domain.vo.PaymentVo;
import cn.org.starpivot.mall.oms.entity.OmsPaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OmsPaymentInfoService extends IService<OmsPaymentInfo> {

    PageResponse<PaymentVo> pageList(PaymentReqBo reqBo);
}
