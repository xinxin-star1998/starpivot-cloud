package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.pms.domain.bo.ProductReqBo;
import cn.org.starpivot.mall.pms.domain.vo.ProductVo;
import cn.org.starpivot.mall.pms.entity.PmsBrand;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.entity.PmsSpuImages;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsBrandMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuImagesMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.pms.service.PmsSpuInfoService;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.bo.PortalProductSearchBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductDetailVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductListVo;
import cn.org.starpivot.mall.portal.service.PortalProductService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class PortalProductServiceImpl implements PortalProductService {

    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSpuImagesMapper pmsSpuImagesMapper;
    private final PmsBrandMapper pmsBrandMapper;
    private final PmsSpuInfoService pmsSpuInfoService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PortalProductListVo> search(PortalProductSearchBo bo) {
        ProductReqBo reqBo = new ProductReqBo();
        reqBo.setPageNum(bo.getPageNum());
        reqBo.setPageSize(bo.getPageSize());
        reqBo.setSpuName(bo.getKeyword());
        reqBo.setCatalogId(bo.getCatalogId());
        reqBo.setBrandId(bo.getBrandId());
        reqBo.setPublishStatus(PortalConstants.PUBLISH_STATUS_ON);

        PageResponse<ProductVo> spuPage = pmsSpuInfoService.getPmsSpuInfoPageList(reqBo);
        List<PortalProductListVo> rows = spuPage.getRows().stream()
                .map(this::fromProductVo)
                .collect(Collectors.toList());
        fillListExtras(rows);
        sortRows(rows, bo.getSort());

        PageResponse<PortalProductListVo> response = new PageResponse<>();
        response.setTotal(spuPage.getTotal());
        response.setRows(rows);
        response.setPageNum(spuPage.getPageNum());
        response.setPageSize(spuPage.getPageSize());
        response.setPageCount(spuPage.getPageCount());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PortalProductDetailVo getDetail(Long spuId) {
        if (spuId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "商品ID不能为空");
        }
        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(spuId);
        if (spu == null || !Integer.valueOf(PortalConstants.PUBLISH_STATUS_ON).equals(spu.getPublishStatus())) {
            throw new BizException("商品不存在或已下架");
        }
        ProductVo productVo = pmsSpuInfoService.getPmsSpuInfoById(spuId);
        PortalProductDetailVo vo = new PortalProductDetailVo();
        BeanUtils.copyProperties(productVo, vo);
        if (spu.getBrandId() != null) {
            PmsBrand brand = pmsBrandMapper.selectById(spu.getBrandId());
            if (brand != null) {
                vo.setBrandName(brand.getName());
            }
        }
        return vo;
    }

    private PortalProductListVo fromProductVo(ProductVo productVo) {
        PortalProductListVo vo = new PortalProductListVo();
        BeanUtils.copyProperties(productVo, vo);
        return vo;
    }

    private void fillListExtras(List<PortalProductListVo> rows) {
        if (CollectionUtils.isEmpty(rows)) {
            return;
        }
        List<Long> spuIds = rows.stream().map(PortalProductListVo::getId).filter(Objects::nonNull).distinct().toList();
        if (spuIds.isEmpty()) {
            return;
        }

        List<PmsSkuInfo> skus = pmsSkuInfoMapper.selectList(
                Wrappers.<PmsSkuInfo>lambdaQuery().in(PmsSkuInfo::getSpuId, spuIds));
        Map<Long, BigDecimal> minPriceMap = new HashMap<>();
        for (PmsSkuInfo sku : skus) {
            if (sku.getSpuId() == null || sku.getPrice() == null) {
                continue;
            }
            minPriceMap.merge(sku.getSpuId(), sku.getPrice(), (a, b) -> a.min(b));
        }

        List<Long> brandIds = rows.stream()
                .map(PortalProductListVo::getBrandId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> brandNameMap = brandIds.isEmpty()
                ? Map.of()
                : pmsBrandMapper.selectBatchIds(brandIds).stream()
                        .collect(Collectors.toMap(PmsBrand::getBrandId, PmsBrand::getName, (a, b) -> a));

        Map<Long, String> coverMap = loadCoverMap(spuIds);
        for (PortalProductListVo row : rows) {
            row.setPrice(minPriceMap.get(row.getId()));
            row.setCoverImg(coverMap.get(row.getId()));
            if (row.getBrandId() != null) {
                row.setBrandName(brandNameMap.get(row.getBrandId()));
            }
        }
    }

    private Map<Long, String> loadCoverMap(List<Long> spuIds) {
        List<PmsSpuImages> imgs = pmsSpuImagesMapper.selectList(
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
            coverMap.putIfAbsent(
                    img.getSpuId(),
                    StorageObjectPathUtils.normalizeToObjectName(img.getImgUrl().trim()));
        }
        return coverMap;
    }

    private void sortRows(List<PortalProductListVo> rows, String sort) {
        if (CollectionUtils.isEmpty(rows) || !StringUtils.hasText(sort)) {
            return;
        }
        switch (sort) {
            case "priceAsc" -> rows.sort(Comparator.comparing(
                    PortalProductListVo::getPrice, Comparator.nullsLast(BigDecimal::compareTo)));
            case "priceDesc" -> rows.sort(Comparator.comparing(
                    PortalProductListVo::getPrice, Comparator.nullsLast(BigDecimal::compareTo)).reversed());
            case "newest" -> rows.sort(Comparator.comparing(
                    PortalProductListVo::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())));
            default -> {
            }
        }
    }
}
