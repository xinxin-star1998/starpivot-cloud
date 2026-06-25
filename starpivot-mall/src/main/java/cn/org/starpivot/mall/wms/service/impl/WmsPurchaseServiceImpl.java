package cn.org.starpivot.mall.wms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseDetailReqBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseDetailSaveBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseDoneBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseItemDoneBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseMergeBo;
import cn.org.starpivot.mall.wms.domain.bo.PurchaseReqBo;
import cn.org.starpivot.mall.wms.domain.vo.PurchaseDetailVo;
import cn.org.starpivot.mall.wms.domain.vo.PurchaseVo;
import cn.org.starpivot.mall.wms.entity.WmsPurchase;
import cn.org.starpivot.mall.wms.entity.WmsPurchaseDetail;
import cn.org.starpivot.mall.wms.enums.WmsPurchaseDetailStatusEnum;
import cn.org.starpivot.mall.wms.enums.WmsPurchaseStatusEnum;
import cn.org.starpivot.mall.wms.mapper.WmsPurchaseDetailMapper;
import cn.org.starpivot.mall.wms.mapper.WmsPurchaseMapper;
import cn.org.starpivot.mall.wms.service.WmsPurchaseService;
import cn.org.starpivot.mall.wms.service.WmsWareSkuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 采购单服务实现类。
 * <p>
 * 实现 {@link WmsPurchaseService}，处理采购单相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see WmsPurchaseService
 */

@Service
@RequiredArgsConstructor
public class WmsPurchaseServiceImpl extends ServiceImpl<WmsPurchaseMapper, WmsPurchase> implements WmsPurchaseService {

    private final WmsPurchaseDetailMapper purchaseDetailMapper;
    private final WmsWareSkuService wmsWareSkuService;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseVo> pageList(PurchaseReqBo reqBo) {
        return toPurchasePage(baseMapper.selectPageList(new Page<>(reqBo.getPageNum(), reqBo.getPageSize()), reqBo));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseVo> unreceivePageList(PurchaseReqBo reqBo) {
        return toPurchasePage(baseMapper.selectUnreceivePage(new Page<>(reqBo.getPageNum(), reqBo.getPageSize()), reqBo));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseVo getDetailById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "采购单ID不能为空");
        }
        WmsPurchase purchase = baseMapper.selectById(id);
        if (purchase == null) {
            throw new BizException("采购单不存在");
        }
        PurchaseVo vo = toPurchaseVo(purchase);
        vo.setDetails(purchaseDetailMapper.listByPurchaseId(id).stream()
                .map(this::toDetailVo)
                .collect(Collectors.toList()));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void merge(PurchaseMergeBo bo) {
        Long purchaseId = bo.getPurchaseId();
        if (purchaseId == null) {
            WmsPurchase purchase = new WmsPurchase();
            purchase.setStatus(WmsPurchaseStatusEnum.CREATED.getCode());
            purchase.setCreateTime(LocalDateTime.now());
            purchase.setUpdateTime(LocalDateTime.now());
            baseMapper.insert(purchase);
            purchaseId = purchase.getId();
        } else {
            WmsPurchase existing = baseMapper.selectById(purchaseId);
            if (existing == null) {
                throw new BizException("采购单不存在");
            }
            if (!WmsPurchaseStatusEnum.canMerge(existing.getStatus())) {
                throw new BizException("仅新建或已分配状态的采购单可合并需求");
            }
        }

        Long finalPurchaseId = purchaseId;
        List<WmsPurchaseDetail> updates = bo.getItems().stream().map(detailId -> {
            WmsPurchaseDetail detail = purchaseDetailMapper.selectById(detailId);
            if (detail == null) {
                throw new BizException("采购需求不存在: " + detailId);
            }
            if (!Integer.valueOf(WmsPurchaseDetailStatusEnum.CREATED.getCode()).equals(detail.getStatus())) {
                throw new BizException("仅新建状态的采购需求可合并");
            }
            detail.setPurchaseId(finalPurchaseId);
            detail.setStatus(WmsPurchaseDetailStatusEnum.ASSIGNED.getCode());
            return detail;
        }).collect(Collectors.toList());

        for (WmsPurchaseDetail detail : updates) {
            purchaseDetailMapper.updateById(detail);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void received(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "采购单ID不能为空");
        }
        List<WmsPurchase> purchases = baseMapper.selectBatchIds(ids);
        if (purchases.size() != ids.size()) {
            throw new BizException("存在无效的采购单");
        }

        LocalDateTime now = LocalDateTime.now();
        for (WmsPurchase purchase : purchases) {
            if (!WmsPurchaseStatusEnum.canReceive(purchase.getStatus())) {
                throw new BizException("采购单[" + purchase.getId() + "]状态不可领取");
            }
            purchase.setStatus(WmsPurchaseStatusEnum.RECEIVED.getCode());
            purchase.setUpdateTime(now);
        }
        updateBatchById(purchases);

        for (WmsPurchase purchase : purchases) {
            List<WmsPurchaseDetail> details = purchaseDetailMapper.listByPurchaseId(purchase.getId());
            for (WmsPurchaseDetail detail : details) {
                detail.setStatus(WmsPurchaseDetailStatusEnum.BUYING.getCode());
                purchaseDetailMapper.updateById(detail);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void done(PurchaseDoneBo bo) {
        WmsPurchase purchase = baseMapper.selectById(bo.getId());
        if (purchase == null) {
            throw new BizException("采购单不存在");
        }

        boolean allSuccess = true;
        List<WmsPurchaseDetail> updates = new ArrayList<>();
        for (PurchaseItemDoneBo item : bo.getItems()) {
            WmsPurchaseDetail update = new WmsPurchaseDetail();
            update.setId(item.getItemId());
            if (Integer.valueOf(WmsPurchaseDetailStatusEnum.HAS_ERROR.getCode()).equals(item.getStatus())) {
                allSuccess = false;
                update.setStatus(WmsPurchaseDetailStatusEnum.HAS_ERROR.getCode());
            } else {
                update.setStatus(WmsPurchaseDetailStatusEnum.FINISH.getCode());
                WmsPurchaseDetail entity = purchaseDetailMapper.selectById(item.getItemId());
                if (entity == null) {
                    throw new BizException("采购明细不存在: " + item.getItemId());
                }
                wmsWareSkuService.addStock(entity.getSkuId(), entity.getWareId(), entity.getSkuNum());
            }
            updates.add(update);
        }

        for (WmsPurchaseDetail update : updates) {
            purchaseDetailMapper.updateById(update);
        }

        purchase.setStatus(allSuccess
                ? WmsPurchaseStatusEnum.FINISH.getCode()
                : WmsPurchaseStatusEnum.HAS_ERROR.getCode());
        purchase.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(purchase);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PurchaseDetailVo> detailPageList(PurchaseDetailReqBo reqBo) {
        Page<WmsPurchaseDetail> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        IPage<WmsPurchaseDetail> detailPage = purchaseDetailMapper.selectPageList(page, reqBo);

        PageResponse<PurchaseDetailVo> response = new PageResponse<>();
        response.setTotal(detailPage.getTotal());
        response.setRows(detailPage.getRecords().stream().map(this::toDetailVo).collect(Collectors.toList()));
        response.setPageNum(detailPage.getCurrent());
        response.setPageSize(detailPage.getSize());
        response.setPageCount(detailPage.getPages());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDetail(PurchaseDetailSaveBo bo) {
        PmsSkuInfo sku = pmsSkuInfoMapper.selectById(bo.getSkuId());
        if (sku == null) {
            throw new BizException("SKU不存在");
        }

        WmsPurchaseDetail detail = new WmsPurchaseDetail();
        detail.setSkuId(bo.getSkuId());
        detail.setSkuNum(bo.getSkuNum());
        detail.setSkuPrice(bo.getSkuPrice() != null ? bo.getSkuPrice() : sku.getPrice());
        detail.setWareId(bo.getWareId());
        detail.setStatus(WmsPurchaseDetailStatusEnum.CREATED.getCode());
        purchaseDetailMapper.insert(detail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDetails(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        for (Long id : ids) {
            WmsPurchaseDetail detail = purchaseDetailMapper.selectById(id);
            if (detail == null) {
                continue;
            }
            if (!Integer.valueOf(WmsPurchaseDetailStatusEnum.CREATED.getCode()).equals(detail.getStatus())) {
                throw new BizException("仅新建状态的采购需求可删除");
            }
        }
        purchaseDetailMapper.deleteBatchIds(ids);
    }

    private PageResponse<PurchaseVo> toPurchasePage(IPage<WmsPurchase> purchasePage) {
        PageResponse<PurchaseVo> response = new PageResponse<>();
        response.setTotal(purchasePage.getTotal());
        response.setRows(purchasePage.getRecords().stream().map(this::toPurchaseVo).collect(Collectors.toList()));
        response.setPageNum(purchasePage.getCurrent());
        response.setPageSize(purchasePage.getSize());
        response.setPageCount(purchasePage.getPages());
        return response;
    }

    private PurchaseVo toPurchaseVo(WmsPurchase purchase) {
        PurchaseVo vo = new PurchaseVo();
        BeanUtils.copyProperties(purchase, vo);
        return vo;
    }

    private PurchaseDetailVo toDetailVo(WmsPurchaseDetail detail) {
        PurchaseDetailVo vo = new PurchaseDetailVo();
        BeanUtils.copyProperties(detail, vo);
        return vo;
    }
}
