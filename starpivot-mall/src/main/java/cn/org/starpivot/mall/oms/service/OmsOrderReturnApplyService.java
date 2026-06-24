package cn.org.starpivot.mall.oms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.oms.domain.bo.ReturnAuditBo;
import cn.org.starpivot.mall.oms.domain.bo.ReturnReqBo;
import cn.org.starpivot.mall.oms.domain.vo.ReturnVo;
import cn.org.starpivot.mall.oms.entity.OmsOrderReturnApply;
import com.baomidou.mybatisplus.extension.service.IService;

public interface OmsOrderReturnApplyService extends IService<OmsOrderReturnApply> {

    PageResponse<ReturnVo> pageList(ReturnReqBo reqBo);

    ReturnVo getDetailById(Long id);

    void audit(ReturnAuditBo bo);
}
