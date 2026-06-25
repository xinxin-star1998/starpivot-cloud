package cn.org.starpivot.mall.wms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.security.SecurityContextUtils;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.wms.domain.bo.*;
import cn.org.starpivot.mall.wms.domain.vo.PurchaseDetailVo;
import cn.org.starpivot.mall.wms.domain.vo.PurchaseVo;
import cn.org.starpivot.mall.wms.entity.WmsPurchase;
import cn.org.starpivot.mall.wms.entity.WmsPurchaseDetail;
import cn.org.starpivot.mall.wms.entity.WmsWareInfo;
import cn.org.starpivot.mall.wms.enums.WmsPurchaseDetailStatusEnum;
import cn.org.starpivot.mall.wms.enums.WmsPurchaseStatusEnum;
import cn.org.starpivot.mall.wms.mapper.WmsPurchaseDetailMapper;
import cn.org.starpivot.mall.wms.mapper.WmsPurchaseMapper;
import cn.org.starpivot.mall.wms.mapper.WmsWareInfoMapper;
import cn.org.starpivot.mall.wms.service.WmsPurchaseService;
import cn.org.starpivot.mall.wms.service.WmsWareSkuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final WmsWareInfoMapper wmsWareInfoMapper;

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
        ensurePurchaseSummary(vo);
        List<WmsPurchaseDetail> details = purchaseDetailMapper.listByPurchaseId(id);
        Map<Long, String> skuNameMap = loadSkuNameMap(details);
        vo.setDetails(details.stream()
                .map(detail -> toDetailVo(detail, skuNameMap))
                .collect(Collectors.toList()));
        enrichWareNames(Collections.singletonList(vo));
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

        validateMergeWareId(finalPurchaseId, updates);

        for (WmsPurchaseDetail detail : updates) {
            purchaseDetailMapper.updateById(detail);
        }
        syncPurchaseSummary(finalPurchaseId);
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
        Long assigneeId = SecurityContextUtils.getUserId();
        String assigneeName = SecurityContextUtils.getUsername();
        for (WmsPurchase purchase : purchases) {
            if (!WmsPurchaseStatusEnum.canReceive(purchase.getStatus())) {
                throw new BizException("采购单[" + purchase.getId() + "]状态不可领取");
            }
            purchase.setStatus(WmsPurchaseStatusEnum.RECEIVED.getCode());
            purchase.setAssigneeId(assigneeId);
            purchase.setAssigneeName(assigneeName);
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

        List<WmsPurchaseDetail> records = detailPage.getRecords();
        Map<Long, String> skuNameMap = loadSkuNameMap(records);

        PageResponse<PurchaseDetailVo> response = new PageResponse<>();
        response.setTotal(detailPage.getTotal());
        response.setRows(records.stream()
                .map(detail -> toDetailVo(detail, skuNameMap))
                .collect(Collectors.toList()));
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
        List<PurchaseVo> rows = purchasePage.getRecords().stream().map(this::toPurchaseVo).collect(Collectors.toList());
        rows.forEach(this::ensurePurchaseSummary);
        enrichWareNames(rows);
        response.setRows(rows);
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

    private void validateMergeWareId(Long purchaseId, List<WmsPurchaseDetail> mergingDetails) {
        Set<Long> wareIds = mergingDetails.stream()
                .map(WmsPurchaseDetail::getWareId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (wareIds.size() > 1) {
            throw new BizException("所选采购需求仓库不一致，无法合并");
        }
        WmsPurchase existing = baseMapper.selectById(purchaseId);
        if (existing != null && existing.getWareId() != null && !wareIds.isEmpty()
                && !wareIds.contains(existing.getWareId())) {
            throw new BizException("采购需求仓库与目标采购单仓库不一致");
        }
    }

    private void ensurePurchaseSummary(PurchaseVo vo) {
        if (vo == null || vo.getId() == null) {
            return;
        }
        if (vo.getAmount() != null && vo.getWareId() != null) {
            return;
        }
        syncPurchaseSummary(vo.getId());
        WmsPurchase refreshed = baseMapper.selectById(vo.getId());
        if (refreshed != null) {
            vo.setAmount(refreshed.getAmount());
            vo.setWareId(refreshed.getWareId());
        }
    }

    private void syncPurchaseSummary(Long purchaseId) {
        if (purchaseId == null) {
            return;
        }
        List<WmsPurchaseDetail> details = purchaseDetailMapper.listByPurchaseId(purchaseId);
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        BigDecimal amount = BigDecimal.ZERO;
        Long wareId = null;
        for (WmsPurchaseDetail detail : details) {
            if (detail.getSkuPrice() != null && detail.getSkuNum() != null) {
                amount = amount.add(detail.getSkuPrice().multiply(BigDecimal.valueOf(detail.getSkuNum())));
            }
            if (wareId == null && detail.getWareId() != null) {
                wareId = detail.getWareId();
            }
        }
        WmsPurchase update = new WmsPurchase();
        update.setId(purchaseId);
        update.setAmount(amount);
        update.setWareId(wareId);
        update.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(update);
    }

    private void enrichWareNames(List<PurchaseVo> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        List<Long> wareIds = rows.stream()
                .map(PurchaseVo::getWareId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(wareIds)) {
            return;
        }
        Map<Long, String> wareNameMap = wmsWareInfoMapper.selectBatchIds(wareIds).stream()
                .collect(Collectors.toMap(WmsWareInfo::getId, WmsWareInfo::getName, (a, b) -> a));
        for (PurchaseVo row : rows) {
            if (row.getWareId() != null) {
                row.setWareName(wareNameMap.get(row.getWareId()));
            }
        }
    }

    private Map<Long, String> loadSkuNameMap(List<WmsPurchaseDetail> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> skuIds = records.stream()
                .map(WmsPurchaseDetail::getSkuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        return pmsSkuInfoMapper.selectBatchIds(skuIds).stream()
                .collect(Collectors.toMap(PmsSkuInfo::getSkuId, this::resolveSkuDisplayName, (a, b) -> a));
    }

    private String resolveSkuDisplayName(PmsSkuInfo sku) {
        if (sku == null) {
            return null;
        }
        if (StringUtils.hasText(sku.getSkuName())) {
            return sku.getSkuName();
        }
        if (StringUtils.hasText(sku.getSkuTitle())) {
            return sku.getSkuTitle();
        }
        return null;
    }

    private PurchaseDetailVo toDetailVo(WmsPurchaseDetail detail, Map<Long, String> skuNameMap) {
        PurchaseDetailVo vo = new PurchaseDetailVo();
        BeanUtils.copyProperties(detail, vo);
        vo.setSkuName(skuNameMap.get(detail.getSkuId()));
        return vo;
    }
}
