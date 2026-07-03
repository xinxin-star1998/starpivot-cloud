package cn.org.starpivot.mall.pms.support;

import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.pms.domain.vo.*;
import cn.org.starpivot.mall.pms.entity.*;
import cn.org.starpivot.mall.pms.mapper.*;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * SPU 详情/列表 VO 组装与图片路径规范化。
 */
@Component
@RequiredArgsConstructor
public class PmsSpuVoAssembler {

    private final PmsSpuInfoDescMapper pmsSpuInfoDescMapper;
    private final PmsSpuImagesMapper pmsSpuImagesMapper;
    private final PmsProductAttrValueMapper pmsProductAttrValueMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSkuImagesMapper pmsSkuImagesMapper;
    private final PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;

    public ProductVo toVo(PmsSpuInfo spu) {
        ProductVo vo = new ProductVo();
        BeanUtils.copyProperties(spu, vo);
        return vo;
    }

    public void fillSpuRelations(ProductVo vo, Long spuId) {
        PmsSpuInfoDesc desc = pmsSpuInfoDescMapper.selectById(spuId);
        if (desc != null && StringUtils.hasText(desc.getDecript())) {
            vo.setDecript(splitDecript(desc.getDecript()));
        }

        List<PmsSpuImages> spuImages =
                pmsSpuImagesMapper.selectList(
                        Wrappers.<PmsSpuImages>lambdaQuery()
                                .eq(PmsSpuImages::getSpuId, spuId)
                                .orderByAsc(PmsSpuImages::getImgSort)
                                .orderByAsc(PmsSpuImages::getId));
        if (!CollectionUtils.isEmpty(spuImages)) {
            vo.setImages(
                    spuImages.stream()
                            .map(PmsSpuImages::getImgUrl)
                            .filter(StringUtils::hasText)
                            .map(String::trim)
                            .collect(Collectors.toList()));
        }

        List<PmsProductAttrValue> attrValues =
                pmsProductAttrValueMapper.selectList(
                        Wrappers.<PmsProductAttrValue>lambdaQuery().eq(PmsProductAttrValue::getSpuId, spuId));
        if (!CollectionUtils.isEmpty(attrValues)) {
            vo.setBaseAttrs(
                    attrValues.stream()
                            .map(
                                    av -> {
                                        BaseAttrs ba = new BaseAttrs();
                                        ba.setAttrId(av.getAttrId());
                                        ba.setAttrName(av.getAttrName());
                                        ba.setAttrValues(av.getAttrValue());
                                        ba.setShowDesc(av.getQuickShow() != null ? av.getQuickShow().intValue() : 0);
                                        return ba;
                                    })
                            .collect(Collectors.toList()));
        }

        List<PmsSkuInfo> skuList =
                pmsSkuInfoMapper.selectList(
                        Wrappers.<PmsSkuInfo>lambdaQuery().eq(PmsSkuInfo::getSpuId, spuId));
        if (!CollectionUtils.isEmpty(skuList)) {
            List<Long> skuIds =
                    skuList.stream()
                            .map(PmsSkuInfo::getSkuId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

            Map<Long, List<PmsSkuSaleAttrValue>> saleAttrMap = Collections.emptyMap();
            Map<Long, List<PmsSkuImages>> skuImageMap = Collections.emptyMap();
            if (!skuIds.isEmpty()) {
                saleAttrMap =
                        pmsSkuSaleAttrValueMapper
                                .selectList(
                                        Wrappers.<PmsSkuSaleAttrValue>lambdaQuery()
                                                .in(PmsSkuSaleAttrValue::getSkuId, skuIds)
                                                .orderByAsc(PmsSkuSaleAttrValue::getAttrSort))
                                .stream()
                                .collect(Collectors.groupingBy(PmsSkuSaleAttrValue::getSkuId));
                skuImageMap =
                        pmsSkuImagesMapper
                                .selectList(
                                        Wrappers.<PmsSkuImages>lambdaQuery()
                                                .in(PmsSkuImages::getSkuId, skuIds)
                                                .orderByAsc(PmsSkuImages::getImgSort))
                                .stream()
                                .collect(Collectors.groupingBy(PmsSkuImages::getSkuId));
            }

            List<Skus> skus = new ArrayList<>();
            for (PmsSkuInfo sku : skuList) {
                skus.add(buildSkuVo(sku, saleAttrMap, skuImageMap));
            }
            vo.setSkus(skus);
        }
    }

    /** 批量回填列表封面（pms_spu_images：优先 default_img，其次排序） */
    public void fillCoverImages(List<ProductVo> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        List<Long> spuIds =
                rows.stream()
                        .map(ProductVo::getId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
        if (spuIds.isEmpty()) {
            return;
        }
        List<PmsSpuImages> imgs =
                pmsSpuImagesMapper.selectList(
                        Wrappers.<PmsSpuImages>lambdaQuery()
                                .in(PmsSpuImages::getSpuId, spuIds)
                                .orderByDesc(PmsSpuImages::getDefaultImg)
                                .orderByAsc(PmsSpuImages::getImgSort)
                                .orderByAsc(PmsSpuImages::getId));
        Map<Long, String> coverMap = new HashMap<>();
        for (PmsSpuImages img : imgs) {
            if (img.getSpuId() == null || !StringUtils.hasText(img.getImgUrl())) {
                continue;
            }
            coverMap.putIfAbsent(img.getSpuId(), StorageObjectPathUtils.normalizeToObjectName(img.getImgUrl().trim()));
        }
        for (ProductVo vo : rows) {
            if (vo.getId() != null) {
                vo.setCoverImg(coverMap.get(vo.getId()));
            }
        }
    }

    public void normalizeVoImagePaths(ProductVo vo) {
        if (vo == null) {
            return;
        }
        if (vo.getCoverImg() != null) {
            vo.setCoverImg(StorageObjectPathUtils.normalizeToObjectName(vo.getCoverImg()));
        }
        if (!CollectionUtils.isEmpty(vo.getImages())) {
            vo.setImages(
                    vo.getImages().stream()
                            .map(StorageObjectPathUtils::normalizeToObjectName)
                            .filter(StringUtils::hasText)
                            .collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(vo.getDecript())) {
            vo.setDecript(
                    vo.getDecript().stream()
                            .map(StorageObjectPathUtils::normalizeToObjectName)
                            .filter(StringUtils::hasText)
                            .collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(vo.getSkus())) {
            for (Skus sku : vo.getSkus()) {
                if (CollectionUtils.isEmpty(sku.getImages())) {
                    continue;
                }
                for (Images img : sku.getImages()) {
                    if (img != null && StringUtils.hasText(img.getImgUrl())) {
                        img.setImgUrl(StorageObjectPathUtils.normalizeToObjectName(img.getImgUrl()));
                    }
                }
            }
        }
    }

    private Skus buildSkuVo(
            PmsSkuInfo sku,
            Map<Long, List<PmsSkuSaleAttrValue>> saleAttrMap,
            Map<Long, List<PmsSkuImages>> skuImageMap) {
        Skus vo = new Skus();
        vo.setSkuId(sku.getSkuId());
        vo.setSkuName(sku.getSkuName());
        vo.setSkuTitle(sku.getSkuTitle());
        vo.setSkuSubtitle(sku.getSkuSubtitle());
        vo.setPrice(sku.getPrice());
        vo.setStockWarning(sku.getStockWarning() == null ? 0 : sku.getStockWarning());

        List<PmsSkuSaleAttrValue> saleAttrs =
                saleAttrMap.getOrDefault(sku.getSkuId(), Collections.emptyList());
        if (!CollectionUtils.isEmpty(saleAttrs)) {
            vo.setAttr(
                    saleAttrs.stream()
                            .map(
                                    sa -> {
                                        Attr a = new Attr();
                                        a.setAttrId(sa.getAttrId());
                                        a.setAttrName(sa.getAttrName());
                                        a.setAttrValue(sa.getAttrValue());
                                        return a;
                                    })
                            .collect(Collectors.toList()));
            vo.setDescar(
                    saleAttrs.stream().map(PmsSkuSaleAttrValue::getAttrValue).collect(Collectors.toList()));
        } else {
            vo.setAttr(Collections.emptyList());
            vo.setDescar(Collections.emptyList());
        }

        List<PmsSkuImages> skuImages = skuImageMap.getOrDefault(sku.getSkuId(), Collections.emptyList());
        if (!CollectionUtils.isEmpty(skuImages)) {
            vo.setImages(mapSkuImageVos(skuImages));
        }
        return vo;
    }

    private List<Images> mapSkuImageVos(List<PmsSkuImages> skuImages) {
        List<Images> result = new ArrayList<>(skuImages.size());
        for (PmsSkuImages entity : skuImages) {
            Images item = new Images();
            item.setImgUrl(entity.getImgUrl());
            item.setDefaultImg(entity.getDefaultImg() != null ? entity.getDefaultImg().intValue() : 0);
            result.add(item);
        }
        return result;
    }

    private List<String> splitDecript(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}
