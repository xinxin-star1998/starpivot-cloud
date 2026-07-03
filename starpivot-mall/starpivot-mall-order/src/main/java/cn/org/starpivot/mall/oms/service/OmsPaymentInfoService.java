package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.PaymentReqBo;
import cn.org.starpivot.mall.oms.domain.vo.PaymentVo;
import cn.org.starpivot.mall.oms.entity.OmsPaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * Paymentinfoservice服务接口。
 * <p>
 * 封装支付信息相关业务逻辑。
 * </p>
 */

public interface OmsPaymentInfoService extends IService<OmsPaymentInfo> {

    /**
     * 分页查询列表。
     */
    PageResponse<PaymentVo> pageList(PaymentReqBo reqBo);
}
