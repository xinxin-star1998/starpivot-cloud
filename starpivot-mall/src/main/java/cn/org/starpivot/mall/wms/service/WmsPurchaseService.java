package cn.org.starpivot.mall.wms.service;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseDetailReqBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseDetailSaveBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseDoneBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseMergeBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseReqBo;
import cn.org.starpivot.mall.wms.domain.vo.PurchaseDetailVo;
import cn.org.starpivot.mall.wms.domain.vo.PurchaseVo;
import cn.org.starpivot.mall.wms.entity.WmsPurchase;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface WmsPurchaseService extends IService<WmsPurchase> {

    PageResponse<PurchaseVo> pageList(PurchaseReqBo reqBo);

    PageResponse<PurchaseVo> unreceivePageList(PurchaseReqBo reqBo);

    PurchaseVo getDetailById(Long id);

    void merge(PurchaseMergeBo bo);

    void received(List<Long> ids);

    void done(PurchaseDoneBo bo);

    PageResponse<PurchaseDetailVo> detailPageList(PurchaseDetailReqBo reqBo);

    void addDetail(PurchaseDetailSaveBo bo);

    void removeDetails(List<Long> ids);
}
