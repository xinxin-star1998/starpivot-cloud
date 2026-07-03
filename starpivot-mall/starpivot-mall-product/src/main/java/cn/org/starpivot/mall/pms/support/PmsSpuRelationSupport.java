package cn.org.starpivot.mall.pms.support;

import cn.org.starpivot.api.mall.ware.MallWareInternalClient;
import cn.org.starpivot.api.mall.ware.dto.WareSkuAddStockRequest;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.pms.domain.bo.ProductSaveBo;
import cn.org.starpivot.mall.pms.domain.vo.Attr;
import cn.org.starpivot.mall.pms.domain.vo.BaseAttrs;
import cn.org.starpivot.mall.pms.domain.vo.Images;
import cn.org.starpivot.mall.pms.domain.vo.Skus;
import cn.org.starpivot.mall.pms.entity.*;
import cn.org.starpivot.mall.pms.mapper.*;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SPU 关联表级联写入、清理与文件引用同步。
 */
@Component
@RequiredArgsConstructor
public class PmsSpuRelationSupport {

    private final PmsSpuInfoDescMapper pmsSpuInfoDescMapper;
    private final PmsSpuImagesMapper pmsSpuImagesMapper;
    private final PmsProductAttrValueMapper pmsProductAttrValueMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSkuImagesMapper pmsSkuImagesMapper;
    private final PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    private final PmsSpuCommentMapper pmsSpuCommentMapper;
    private final PmsCommentReplayMapper pmsCommentReplayMapper;
    private final MallWareInternalClient mallWareInternalClient;
    private final MallFileRefSupport mallFileRefSupport;

    /** 在 pms_spu_info 已落库后写入 desc / images / attrs / skus 等关联表 */
    public void saveSpuRelations(Long spuId, ProductSaveBo bo, boolean initStock) {
        saveSpuInfoDesc(spuId, bo);
        saveSpuImages(spuId, bo);
        saveProductAttrValues(spuId, bo);
        saveAllSkus(spuId, bo, initStock);
    }

    public void removeSpuRelations(Long spuId) {
        if (spuId == null) {
            return;
        }
        List<Long> skuIds = listSkuIdsBySpuId(spuId);
        removeSkuCascade(spuId, skuIds);
        removeSpuComments(spuId, skuIds);
        pmsProductAttrValueMapper.delete(
                Wrappers.<PmsProductAttrValue>lambdaQuery().eq(PmsProductAttrValue::getSpuId, spuId));
        pmsSpuImagesMapper.delete(Wrappers.<PmsSpuImages>lambdaQuery().eq(PmsSpuImages::getSpuId, spuId));
        pmsSpuInfoDescMapper.deleteById(spuId);
    }

    public void syncProductFileRefs(Long spuId) {
        List<PmsSpuImages> spuImages =
                pmsSpuImagesMapper.selectList(
                        Wrappers.<PmsSpuImages>lambdaQuery().eq(PmsSpuImages::getSpuId, spuId));
        mallFileRefSupport.syncSpuImages(
                spuId,
                spuImages.stream()
                        .map(PmsSpuImages::getImgUrl)
                        .filter(StringUtils::hasText)
                        .toList());

        List<Long> skuIds = listSkuIdsBySpuId(spuId);
        for (Long skuId : skuIds) {
            List<PmsSkuImages> skuImages =
                    pmsSkuImagesMapper.selectList(
                            Wrappers.<PmsSkuImages>lambdaQuery().eq(PmsSkuImages::getSkuId, skuId));
            mallFileRefSupport.syncSkuImages(
                    skuId,
                    skuImages.stream()
                            .map(PmsSkuImages::getImgUrl)
                            .filter(StringUtils::hasText)
                            .toList());
        }
    }

    public void unbindProductFileRefs(Long spuId) {
        mallFileRefSupport.unbindProduct(spuId, listSkuIdsBySpuId(spuId));
    }

    public List<Long> listSkuIdsBySpuId(Long spuId) {
        return pmsSkuInfoMapper.selectList(Wrappers.<PmsSkuInfo>lambdaQuery().eq(PmsSkuInfo::getSpuId, spuId))
                .stream()
                .map(PmsSkuInfo::getSkuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private void saveSpuInfoDesc(Long spuId, ProductSaveBo bo) {
        if (CollectionUtils.isEmpty(bo.getDecript())) {
            return;
        }
        PmsSpuInfoDesc desc = new PmsSpuInfoDesc();
        desc.setSpuId(spuId);
        desc.setDecript(
                bo.getDecript().stream()
                        .filter(StringUtils::hasText)
                        .map(url -> StorageObjectPathUtils.normalizeToObjectName(url.trim()))
                        .collect(Collectors.joining(",")));
        pmsSpuInfoDescMapper.insert(desc);
    }

    private void saveSpuImages(Long spuId, ProductSaveBo bo) {
        if (CollectionUtils.isEmpty(bo.getImages())) {
            return;
        }
        int sort = 0;
        for (String url : bo.getImages()) {
            if (!StringUtils.hasText(url)) {
                continue;
            }
            PmsSpuImages img = new PmsSpuImages();
            img.setSpuId(spuId);
            img.setImgUrl(StorageObjectPathUtils.normalizeToObjectName(url.trim()));
            img.setImgSort(sort++);
            img.setDefaultImg(sort == 1 ? 1 : 0);
            pmsSpuImagesMapper.insert(img);
        }
    }

    private void saveProductAttrValues(Long spuId, ProductSaveBo bo) {
        if (CollectionUtils.isEmpty(bo.getBaseAttrs())) {
            return;
        }
        int sort = 0;
        for (BaseAttrs attr : bo.getBaseAttrs()) {
            if (attr == null || attr.getAttrId() == null || !StringUtils.hasText(attr.getAttrValues())) {
                continue;
            }
            PmsProductAttrValue val = new PmsProductAttrValue();
            val.setSpuId(spuId);
            val.setAttrId(attr.getAttrId());
            val.setAttrName(attr.getAttrName());
            val.setAttrValue(attr.getAttrValues());
            val.setAttrSort(sort++);
            val.setQuickShow(attr.getShowDesc());
            pmsProductAttrValueMapper.insert(val);
        }
    }

    private void saveAllSkus(Long spuId, ProductSaveBo bo, boolean initStock) {
        if (CollectionUtils.isEmpty(bo.getSkus())) {
            return;
        }
        for (Skus skuBo : bo.getSkus()) {
            if (skuBo == null || !StringUtils.hasText(skuBo.getSkuName())) {
                continue;
            }
            saveSku(spuId, bo, skuBo, initStock);
        }
    }

    private void saveSku(Long spuId, ProductSaveBo bo, Skus skuBo, boolean initStock) {
        PmsSkuInfo sku = new PmsSkuInfo();
        sku.setSpuId(spuId);
        sku.setSkuName(skuBo.getSkuName());
        sku.setSkuTitle(skuBo.getSkuTitle());
        sku.setSkuSubtitle(skuBo.getSkuSubtitle());
        sku.setCatalogId(bo.getCatalogId());
        sku.setBrandId(bo.getBrandId());
        sku.setPrice(skuBo.getPrice() != null ? skuBo.getPrice() : BigDecimal.ZERO);
        sku.setSaleCount(0L);
        sku.setStockWarning(skuBo.getStockWarning() != null && skuBo.getStockWarning() > 0 ? skuBo.getStockWarning() : null);
        sku.setSkuDefaultImg(resolveSkuDefaultImg(skuBo));
        pmsSkuInfoMapper.insert(sku);

        saveSkuSaleAttrValues(sku.getSkuId(), skuBo);
        saveSkuImages(sku.getSkuId(), skuBo);

        if (initStock && skuBo.getInitialStock() != null && skuBo.getInitialStock() > 0 && bo.getDefaultWareId() != null) {
            WareSkuAddStockRequest request = new WareSkuAddStockRequest();
            request.setSkuId(sku.getSkuId());
            request.setWareId(bo.getDefaultWareId());
            request.setQuantity(skuBo.getInitialStock());
            Result<Void> result = mallWareInternalClient.addStock(request);
            if (result == null || !result.isSuccess()) {
                throw new BizException(result != null ? result.getMessage() : "初始化库存失败");
            }
        }
    }

    private String resolveSkuDefaultImg(Skus skuBo) {
        if (CollectionUtils.isEmpty(skuBo.getImages())) {
            return null;
        }
        String marked =
                skuBo.getImages().stream()
                        .filter(img -> img != null && img.getDefaultImg() == 1 && StringUtils.hasText(img.getImgUrl()))
                        .map(Images::getImgUrl)
                        .findFirst()
                        .orElse(null);
        if (marked != null) {
            return marked;
        }
        return skuBo.getImages().stream()
                .filter(img -> img != null && StringUtils.hasText(img.getImgUrl()))
                .map(Images::getImgUrl)
                .findFirst()
                .orElse(null);
    }

    private void saveSkuSaleAttrValues(Long skuId, Skus skuBo) {
        if (CollectionUtils.isEmpty(skuBo.getAttr())) {
            return;
        }
        int sort = 0;
        for (Attr attr : skuBo.getAttr()) {
            if (attr == null || attr.getAttrId() == null) {
                continue;
            }
            PmsSkuSaleAttrValue sale = new PmsSkuSaleAttrValue();
            sale.setSkuId(skuId);
            sale.setAttrId(attr.getAttrId());
            sale.setAttrName(attr.getAttrName());
            sale.setAttrValue(attr.getAttrValue());
            sale.setAttrSort(sort++);
            pmsSkuSaleAttrValueMapper.insert(sale);
        }
    }

    private void saveSkuImages(Long skuId, Skus skuBo) {
        if (CollectionUtils.isEmpty(skuBo.getImages())) {
            return;
        }
        int sort = 0;
        for (Images img : skuBo.getImages()) {
            if (img == null || !StringUtils.hasText(img.getImgUrl())) {
                continue;
            }
            PmsSkuImages entity = new PmsSkuImages();
            entity.setSkuId(skuId);
            entity.setImgUrl(StorageObjectPathUtils.normalizeToObjectName(img.getImgUrl().trim()));
            entity.setImgSort(sort++);
            entity.setDefaultImg(img.getDefaultImg());
            pmsSkuImagesMapper.insert(entity);
        }
    }

    private void removeSkuCascade(Long spuId, List<Long> skuIds) {
        if (!CollectionUtils.isEmpty(skuIds)) {
            pmsSkuImagesMapper.delete(Wrappers.<PmsSkuImages>lambdaQuery().in(PmsSkuImages::getSkuId, skuIds));
            pmsSkuSaleAttrValueMapper.delete(
                    Wrappers.<PmsSkuSaleAttrValue>lambdaQuery().in(PmsSkuSaleAttrValue::getSkuId, skuIds));
        }
        pmsSkuInfoMapper.delete(Wrappers.<PmsSkuInfo>lambdaQuery().eq(PmsSkuInfo::getSpuId, spuId));
    }

    private void removeSpuComments(Long spuId, List<Long> skuIds) {
        var commentQuery =
                Wrappers.<PmsSpuComment>lambdaQuery().eq(PmsSpuComment::getSpuId, spuId);
        if (!CollectionUtils.isEmpty(skuIds)) {
            commentQuery.or(w -> w.in(PmsSpuComment::getSkuId, skuIds));
        }
        List<PmsSpuComment> comments = pmsSpuCommentMapper.selectList(commentQuery);
        if (CollectionUtils.isEmpty(comments)) {
            return;
        }
        List<Long> commentIds =
                comments.stream()
                        .map(PmsSpuComment::getId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
        if (!commentIds.isEmpty()) {
            pmsCommentReplayMapper.delete(
                    Wrappers.<PmsCommentReplay>lambdaQuery().in(PmsCommentReplay::getCommentId, commentIds));
        }
        pmsSpuCommentMapper.delete(commentQuery);
    }
}
