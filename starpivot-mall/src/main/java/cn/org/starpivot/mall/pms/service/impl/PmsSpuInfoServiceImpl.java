package cn.org.starpivot.mall.pms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.pms.domain.bo.ProductReqBo;
import cn.org.starpivot.mall.pms.domain.bo.ProductSaveBo;
import cn.org.starpivot.mall.pms.domain.vo.*;
import cn.org.starpivot.mall.pms.entity.*;
import cn.org.starpivot.mall.pms.mapper.*;
import cn.org.starpivot.mall.pms.service.PmsSpuInfoService;
import cn.org.starpivot.mall.wms.service.WmsWareSkuService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
 * SPU服务实现类。
 * <p>
 * 实现 {@link PmsSpuInfoService}，处理SPU相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see PmsSpuInfoService
 */

@Service
@RequiredArgsConstructor
public class PmsSpuInfoServiceImpl implements PmsSpuInfoService {

    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PmsSpuImagesMapper pmsSpuImagesMapper;
    private final PmsSpuInfoDescMapper pmsSpuInfoDescMapper;
    private final PmsProductAttrValueMapper pmsProductAttrValueMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSkuImagesMapper pmsSkuImagesMapper;
    private final PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    private final PmsSpuCommentMapper pmsSpuCommentMapper;
    private final PmsCommentReplayMapper pmsCommentReplayMapper;
    private final WmsWareSkuService wmsWareSkuService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ProductVo> getPmsSpuInfoPageList(ProductReqBo productReqBo) {
        Page<PmsSpuInfo> page = new Page<>(productReqBo.getPageNum(), productReqBo.getPageSize());
        IPage<PmsSpuInfo> pageList = pmsSpuInfoMapper.selectPageList(page, productReqBo);
        List<ProductVo> rows = pageList.getRecords().stream().map(this::toVo).collect(Collectors.toList());
        fillCoverImages(rows);
        rows.forEach(this::normalizeVoImagePaths);
        PageResponse<ProductVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(rows);
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVo getPmsSpuInfoById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SPU ID不能为空");
        }
        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(id);
        if (spu == null) {
            throw new BizException("商品不存在");
        }
        ProductVo vo = toVo(spu);
        fillSpuRelations(vo, id);
        normalizeVoImagePaths(vo);
        fillCoverImages(Collections.singletonList(vo));
        return vo;
    }

    /**
     * 新增 SPU：单事务内按谷粒商城顺序级联写入。
     * <ol>
     *   <li>pms_spu_info</li>
     *   <li>pms_spu_info_desc → pms_spu_images → pms_product_attr_value</li>
     *   <li>每个 SKU：pms_sku_info，并写入 pms_sku_sale_attr_value、pms_sku_images</li>
     * </ol>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addPmsSpuInfo(ProductSaveBo bo) {
        PmsSpuInfo entity = new PmsSpuInfo();
        copySpuMainFields(bo, entity);
        entity.setId(null);
        LocalDateTime now = LocalDateTime.now();
        entity.setCreateTime(now);
        entity.setUpdateTime(now);
        pmsSpuInfoMapper.insert(entity);
        saveSpuRelations(entity.getId(), bo, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePmsSpuInfo(ProductSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改时 id 不能为空");
        }
        PmsSpuInfo existing = pmsSpuInfoMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("商品不存在");
        }
        copySpuMainFields(bo, existing);
        existing.setUpdateTime(LocalDateTime.now());
        pmsSpuInfoMapper.updateById(existing);
        removeSpuRelations(bo.getId());
        saveSpuRelations(bo.getId(), bo, false);
    }

    /**
     * 删除 SPU 并级联清理关联表：
     * pms_sku_sale_attr_value、pms_sku_images、pms_sku_info、
     * pms_comment_replay、pms_spu_comment、
     * pms_product_attr_value、pms_spu_images、pms_spu_info_desc、
     * 最后 pms_spu_info。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePmsSpuInfoByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        List<Long> spuIds =
                ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        for (Long spuId : spuIds) {
            removeSpuRelations(spuId);
        }
        pmsSpuInfoMapper.delete(Wrappers.<PmsSpuInfo>lambdaQuery().in(PmsSpuInfo::getId, spuIds));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePublishStatus(Long id, Integer publishStatus) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SPU ID不能为空");
        }
        if (publishStatus == null || (publishStatus != 0 && publishStatus != 1)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "上架状态仅支持 0（下架）或 1（上架）");
        }
        PmsSpuInfo existing = pmsSpuInfoMapper.selectById(id);
        if (existing == null) {
            throw new BizException("商品不存在");
        }
        existing.setPublishStatus(publishStatus);
        existing.setUpdateTime(LocalDateTime.now());
        pmsSpuInfoMapper.updateById(existing);
    }

    private void copySpuMainFields(ProductSaveBo bo, PmsSpuInfo entity) {
        entity.setSpuName(bo.getSpuName());
        entity.setSpuDescription(bo.getSpuDescription());
        entity.setCatalogId(bo.getCatalogId());
        entity.setBrandId(bo.getBrandId());
        entity.setWeight(bo.getWeight());
        entity.setPublishStatus(bo.getPublishStatus());
    }

    private void fillSpuRelations(ProductVo vo, Long spuId) {
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
                            .map(url -> url.trim())
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
        }return vo;

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

    /** 步骤 2～5：在 pms_spu_info 已落库后写入关联表（pms_spu_bounds 仅前端展示，不落库） */
    private void saveSpuRelations(Long spuId, ProductSaveBo bo, boolean initStock) {
        saveSpuInfoDesc(spuId, bo);
        saveSpuImages(spuId, bo);
        saveProductAttrValues(spuId, bo);
        saveAllSkus(spuId, bo, initStock);
    }

    /** 2. pms_spu_info_desc */
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

    /** 3. pms_spu_images */
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

    /** 4. pms_product_attr_value */
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

    /** 5. 所有 SKU 及子表 */
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

    /**
     * 单条 SKU：先 pms_sku_info，再 pms_sku_sale_attr_value、pms_sku_images。
     */
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
            wmsWareSkuService.addStock(sku.getSkuId(), bo.getDefaultWareId(), skuBo.getInitialStock());
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

    private void removeSpuRelations(Long spuId) {
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

    private List<Long> listSkuIdsBySpuId(Long spuId) {
        return pmsSkuInfoMapper.selectList(Wrappers.<PmsSkuInfo>lambdaQuery().eq(PmsSkuInfo::getSpuId, spuId))
                .stream()
                .map(PmsSkuInfo::getSkuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
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

    private List<String> splitDecript(String raw) {
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }

    private ProductVo toVo(PmsSpuInfo spu) {
        ProductVo vo = new ProductVo();
        BeanUtils.copyProperties(spu, vo);
        return vo;
    }

    /** 批量回填列表封面（pms_spu_images：优先 default_img，其次排序） */
    private void fillCoverImages(List<ProductVo> rows) {
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

    private void normalizeVoImagePaths(ProductVo vo) {
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
}    
