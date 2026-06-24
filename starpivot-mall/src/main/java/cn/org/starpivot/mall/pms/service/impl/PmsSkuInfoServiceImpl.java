package cn.org.starpivot.mall.pms.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.pms.domain.bo.SkuCreateBo;
import cn.org.starpivot.mall.pms.domain.bo.SkuPriceBo;
import cn.org.starpivot.mall.pms.domain.bo.SkuPublishStatusBo;
import cn.org.starpivot.mall.pms.domain.bo.SkuReqBo;
import cn.org.starpivot.mall.pms.domain.bo.SkuSaveBo;
import cn.org.starpivot.mall.pms.domain.vo.Attr;
import cn.org.starpivot.mall.pms.domain.vo.Images;
import cn.org.starpivot.mall.pms.domain.vo.SkuVo;
import cn.org.starpivot.mall.pms.entity.PmsSkuImages;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.entity.PmsSkuSaleAttrValue;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSkuImagesMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSkuSaleAttrValueMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.pms.service.PmsSkuInfoService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PmsSkuInfoServiceImpl implements PmsSkuInfoService {

    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    private final PmsSkuImagesMapper pmsSkuImagesMapper;
    private final PmsSpuInfoMapper pmsSpuInfoMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SkuVo> getSkuPageList(SkuReqBo reqBo) {
        Page<SkuVo> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        IPage<SkuVo> pageList = pmsSkuInfoMapper.selectPageList(page, reqBo);
        PageResponse<SkuVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords());
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public SkuVo getSkuById(Long skuId) {
        if (skuId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SKU ID 不能为空");
        }
        PmsSkuInfo sku = pmsSkuInfoMapper.selectById(skuId);
        if (sku == null) {
            throw new BizException("SKU 不存在");
        }
        SkuVo vo = new SkuVo();
        BeanUtils.copyProperties(sku, vo);
        if (sku.getSpuId() != null) {
            PmsSpuInfo spu = pmsSpuInfoMapper.selectById(sku.getSpuId());
            if (spu != null) {
                vo.setSpuName(spu.getSpuName());
                vo.setSpuPublishStatus(spu.getPublishStatus());
            }
        }
        fillSaleAttrs(vo, skuId);
        normalizeSkuImage(vo);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSku(SkuCreateBo bo) {
        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(bo.getSpuId());
        if (spu == null) {
            throw new BizException("所属 SPU 不存在");
        }
        if (!StringUtils.hasText(bo.getSkuName())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SKU 名称不能为空");
        }

        PmsSkuInfo sku = new PmsSkuInfo();
        sku.setSpuId(bo.getSpuId());
        sku.setSkuName(bo.getSkuName().trim());
        sku.setSkuTitle(bo.getSkuTitle());
        sku.setSkuSubtitle(bo.getSkuSubtitle());
        sku.setCatalogId(spu.getCatalogId());
        sku.setBrandId(spu.getBrandId());
        sku.setPrice(bo.getPrice() != null ? bo.getPrice() : BigDecimal.ZERO);
        sku.setSaleCount(0L);
        sku.setSkuDefaultImg(resolveSkuDefaultImg(bo));
        pmsSkuInfoMapper.insert(sku);

        saveSkuSaleAttrValues(sku.getSkuId(), bo.getAttr());
        saveSkuImages(sku.getSkuId(), bo.getImages());
        return sku.getSkuId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrice(SkuPriceBo bo) {
        PmsSkuInfo existing = requireSku(bo.getSkuId());
        existing.setPrice(bo.getPrice());
        pmsSkuInfoMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePublishStatus(SkuPublishStatusBo bo) {
        if (bo.getPublishStatus() == null || (bo.getPublishStatus() != 0 && bo.getPublishStatus() != 1)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "上架状态仅支持 0（下架）或 1（上架）");
        }
        PmsSkuInfo sku = requireSku(bo.getSkuId());
        if (sku.getSpuId() == null) {
            throw new BizException("SKU 未关联 SPU，无法上下架");
        }
        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(sku.getSpuId());
        if (spu == null) {
            throw new BizException("所属 SPU 不存在");
        }
        spu.setPublishStatus(bo.getPublishStatus());
        spu.setUpdateTime(LocalDateTime.now());
        pmsSpuInfoMapper.updateById(spu);
    }

    private PmsSkuInfo requireSku(Long skuId) {
        if (skuId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SKU ID 不能为空");
        }
        PmsSkuInfo existing = pmsSkuInfoMapper.selectById(skuId);
        if (existing == null) {
            throw new BizException("SKU 不存在");
        }
        return existing;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSku(SkuSaveBo bo) {
        if (bo.getSkuId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SKU ID 不能为空");
        }
        PmsSkuInfo existing = pmsSkuInfoMapper.selectById(bo.getSkuId());
        if (existing == null) {
            throw new BizException("SKU 不存在");
        }
        existing.setPrice(bo.getPrice());
        existing.setSkuTitle(bo.getSkuTitle());
        existing.setSkuSubtitle(bo.getSkuSubtitle());
        if (StringUtils.hasText(bo.getSkuDefaultImg())) {
            existing.setSkuDefaultImg(
                    StorageObjectPathUtils.normalizeToObjectName(bo.getSkuDefaultImg().trim()));
        } else {
            existing.setSkuDefaultImg(bo.getSkuDefaultImg());
        }
        pmsSkuInfoMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        List<Long> skuIds =
                ids.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (skuIds.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        pmsSkuImagesMapper.delete(Wrappers.<PmsSkuImages>lambdaQuery().in(PmsSkuImages::getSkuId, skuIds));
        pmsSkuSaleAttrValueMapper.delete(
                Wrappers.<PmsSkuSaleAttrValue>lambdaQuery().in(PmsSkuSaleAttrValue::getSkuId, skuIds));
        pmsSkuInfoMapper.delete(Wrappers.<PmsSkuInfo>lambdaQuery().in(PmsSkuInfo::getSkuId, skuIds));
    }

    private void fillSaleAttrs(SkuVo vo, Long skuId) {
        List<PmsSkuSaleAttrValue> saleAttrs =
                pmsSkuSaleAttrValueMapper.selectList(
                        Wrappers.<PmsSkuSaleAttrValue>lambdaQuery()
                                .eq(PmsSkuSaleAttrValue::getSkuId, skuId)
                                .orderByAsc(PmsSkuSaleAttrValue::getAttrSort));
        if (CollectionUtils.isEmpty(saleAttrs)) {
            vo.setSaleAttrs(Collections.emptyList());
            return;
        }
        vo.setSaleAttrs(
                saleAttrs.stream()
                        .map(
                                sa -> {
                                    Attr attr = new Attr();
                                    attr.setAttrId(sa.getAttrId());
                                    attr.setAttrName(sa.getAttrName());
                                    attr.setAttrValue(sa.getAttrValue());
                                    return attr;
                                })
                        .collect(Collectors.toList()));
        vo.setSaleAttrText(
                saleAttrs.stream()
                        .map(sa -> (sa.getAttrName() != null ? sa.getAttrName() : "")
                                + ":"
                                + (sa.getAttrValue() != null ? sa.getAttrValue() : ""))
                        .collect(Collectors.joining(" ")));
    }

    private void normalizeSkuImage(SkuVo vo) {
        if (vo != null && StringUtils.hasText(vo.getSkuDefaultImg())) {
            vo.setSkuDefaultImg(StorageObjectPathUtils.normalizeToObjectName(vo.getSkuDefaultImg()));
        }
    }

    private String resolveSkuDefaultImg(SkuCreateBo bo) {
        if (StringUtils.hasText(bo.getSkuDefaultImg())) {
            return StorageObjectPathUtils.normalizeToObjectName(bo.getSkuDefaultImg().trim());
        }
        if (CollectionUtils.isEmpty(bo.getImages())) {
            return null;
        }
        String marked =
                bo.getImages().stream()
                        .filter(img -> img != null && img.getDefaultImg() == 1 && StringUtils.hasText(img.getImgUrl()))
                        .map(Images::getImgUrl)
                        .findFirst()
                        .orElse(null);
        if (marked != null) {
            return StorageObjectPathUtils.normalizeToObjectName(marked.trim());
        }
        return bo.getImages().stream()
                .filter(img -> img != null && StringUtils.hasText(img.getImgUrl()))
                .map(Images::getImgUrl)
                .map(url -> StorageObjectPathUtils.normalizeToObjectName(url.trim()))
                .findFirst()
                .orElse(null);
    }

    private void saveSkuSaleAttrValues(Long skuId, List<Attr> attrs) {
        if (CollectionUtils.isEmpty(attrs)) {
            return;
        }
        int sort = 0;
        for (Attr attr : attrs) {
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

    private void saveSkuImages(Long skuId, List<Images> images) {
        if (CollectionUtils.isEmpty(images)) {
            return;
        }
        int sort = 0;
        for (Images img : images) {
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
}
