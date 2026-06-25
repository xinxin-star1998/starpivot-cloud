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

/**
 * Purchaseservice服务接口。
 * <p>
 * 封装采购单相关业务逻辑。
 * </p>
 */

public interface WmsPurchaseService extends IService<WmsPurchase> {

    /**
     * 分页查询列表。
     */
    PageResponse<PurchaseVo> pageList(PurchaseReqBo reqBo);

    /**
     * unreceivePageList。
     */
    PageResponse<PurchaseVo> unreceivePageList(PurchaseReqBo reqBo);

    /**
     * 根据 ID 获取详情。
     */
    PurchaseVo getDetailById(Long id);

    /**
     * merge。
     */
    void merge(PurchaseMergeBo bo);

    /**
     * received。
     */
    void received(List<Long> ids);

    /**
     * done。
     */
    void done(PurchaseDoneBo bo);

    /**
     * detailPageList。
     */
    PageResponse<PurchaseDetailVo> detailPageList(PurchaseDetailReqBo reqBo);

    /**
     * 新增记录。
     */
    void addDetail(PurchaseDetailSaveBo bo);

    /**
     * 删除记录。
     */
    void removeDetails(List<Long> ids);
}
