package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.RefundReqBo;
import cn.org.starpivot.mall.oms.domain.vo.RefundVo;
import cn.org.starpivot.mall.oms.entity.OmsRefundInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OmsRefundInfoService extends IService<OmsRefundInfo> {

    PageResponse<RefundVo> pageList(RefundReqBo reqBo);
}
