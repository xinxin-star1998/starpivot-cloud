package cn.org.starpivot.mall.wms.service.impl;

import cn.org.starpivot.api.product.dto.SkuDto;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.wms.entity.WmsPurchaseDetail;
import cn.org.starpivot.mall.wms.enums.WmsPurchaseDetailStatusEnum;
import cn.org.starpivot.mall.wms.mapper.WmsPurchaseDetailMapper;
import cn.org.starpivot.mall.wms.mapper.WmsWareSkuMapper;
import cn.org.starpivot.mall.wms.service.WmsStockWarningService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存预警服务实现：扣减后同步检查，低于阈值则写入采购需求。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WmsStockWarningServiceImpl implements WmsStockWarningService {

    private final ProductFeignSupport productFeignSupport;
    private final WmsWareSkuMapper wmsWareSkuMapper;
    private final WmsPurchaseDetailMapper purchaseDetailMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void checkAndCreatePurchaseDemand(Long skuId, Long wareId) {
        if (skuId == null) {
            return;
        }
        SkuDto sku = productFeignSupport.requireSku(skuId);
        if (sku.getStockWarning() == null || sku.getStockWarning() <= 0) {
            return;
        }

        int available = wmsWareSkuMapper.sumAvailableStock(skuId);
        if (available >= sku.getStockWarning()) {
            return;
        }

        long pending = purchaseDetailMapper.selectCount(
                Wrappers.<WmsPurchaseDetail>lambdaQuery()
                        .eq(WmsPurchaseDetail::getSkuId, skuId)
                        .eq(WmsPurchaseDetail::getStatus, WmsPurchaseDetailStatusEnum.CREATED.getCode()));
        if (pending > 0) {
            return;
        }

        int replenishQty = Math.max(sku.getStockWarning() * 2 - available, sku.getStockWarning());
        WmsPurchaseDetail detail = new WmsPurchaseDetail();
        detail.setSkuId(skuId);
        detail.setSkuNum(replenishQty);
        detail.setSkuPrice(sku.getPrice());
        detail.setWareId(wareId);
        detail.setStatus(WmsPurchaseDetailStatusEnum.CREATED.getCode());
        purchaseDetailMapper.insert(detail);
        log.info("库存预警：SKU[{}] 可售{} < 阈值{}，已自动生成采购需求，数量={}",
                skuId, available, sku.getStockWarning(), replenishQty);
    }
}
