package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.pms.domain.bo.ProductReqBo;
import cn.org.starpivot.mall.pms.domain.vo.ProductVo;
import cn.org.starpivot.mall.pms.domain.vo.Skus;
import cn.org.starpivot.mall.pms.entity.PmsBrand;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.entity.PmsSpuImages;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsBrandMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuCommentMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuImagesMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.pms.service.PmsSpuInfoService;
import cn.org.starpivot.mall.pms.search.PmsProductElasticsearchService;
import cn.org.starpivot.mall.pms.search.PmsProductSearchSyncService;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.bo.PortalProductSearchBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductDetailVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalProductListVo;
import cn.org.starpivot.mall.portal.service.PortalProductService;
import cn.org.starpivot.mall.portal.service.PortalStockLockService;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Product服务实现类。
 * <p>
 * 实现 {@link PortalProductService}，处理Product相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see PortalProductService
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PortalProductServiceImpl implements PortalProductService {

    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSpuImagesMapper pmsSpuImagesMapper;
    private final PmsBrandMapper pmsBrandMapper;
    private final PmsSpuCommentMapper pmsSpuCommentMapper;
    private final PmsSpuInfoService pmsSpuInfoService;
    private final PortalStockLockService portalStockLockService;
    private final PmsProductSearchSyncService productSearchSyncService;
    private final ObjectProvider<PmsProductElasticsearchService> productElasticsearchServiceProvider;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PortalProductListVo> search(PortalProductSearchBo bo) {
        if (productSearchSyncService.isElasticsearchActive()) {
            try {
                PmsProductElasticsearchService es = productElasticsearchServiceProvider.getIfAvailable();
                if (es != null) {
                    PageResponse<Long> idPage = es.searchSpuIds(bo);
                    return buildPageFromSpuIds(idPage);
                }
            } catch (Exception ex) {
                log.warn("Elasticsearch search failed, fallback to database", ex);
            }
        }
        return searchFromDatabase(bo);
    }

    private PageResponse<PortalProductListVo> searchFromDatabase(PortalProductSearchBo bo) {
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

    private PageResponse<PortalProductListVo> buildPageFromSpuIds(PageResponse<Long> idPage) {
        List<Long> spuIds = idPage.getRows() != null ? idPage.getRows() : List.of();
        List<PortalProductListVo> rows = new ArrayList<>();
        if (!spuIds.isEmpty()) {
            List<PmsSpuInfo> spuList = pmsSpuInfoMapper.selectBatchIds(spuIds);
            Map<Long, PmsSpuInfo> spuMap = spuList.stream()
                    .filter(spu -> spu.getId() != null)
                    .collect(Collectors.toMap(PmsSpuInfo::getId, spu -> spu, (a, b) -> a));
            for (Long spuId : spuIds) {
                PmsSpuInfo spu = spuMap.get(spuId);
                if (spu == null) {
                    continue;
                }
                PortalProductListVo vo = new PortalProductListVo();
                vo.setId(spu.getId());
                vo.setSpuName(spu.getSpuName());
                vo.setSpuDescription(spu.getSpuDescription());
                vo.setCatalogId(spu.getCatalogId());
                vo.setBrandId(spu.getBrandId());
                vo.setCreateTime(spu.getCreateTime());
                rows.add(vo);
            }
            fillListExtras(rows);
        }
        PageResponse<PortalProductListVo> response = new PageResponse<>();
        response.setTotal(idPage.getTotal());
        response.setRows(rows);
        response.setPageNum(idPage.getPageNum());
        response.setPageSize(idPage.getPageSize());
        response.setPageCount(idPage.getPageCount());
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
        fillSkuAvailableStock(vo.getSkus());
        return vo;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortalProductListVo> listRelated(Long spuId, int limit) {
        if (spuId == null || limit <= 0) {
            return List.of();
        }
        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(spuId);
        if (spu == null) {
            return List.of();
        }
        Set<Long> seen = new HashSet<>();
        seen.add(spuId);
        List<PortalProductListVo> result = new ArrayList<>();
        appendRelated(spu.getCatalogId(), null, limit, seen, result);
        if (result.size() < limit && spu.getBrandId() != null) {
            appendRelated(null, spu.getBrandId(), limit, seen, result);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PortalProductListVo> listByOrderedSpuIds(
            List<Long> orderedSpuIds, int pageNum, int pageSize) {
        PageResponse<PortalProductListVo> response = new PageResponse<>();
        response.setPageNum((long) pageNum);
        response.setPageSize((long) pageSize);
        if (CollectionUtils.isEmpty(orderedSpuIds)) {
            response.setTotal(0L);
            response.setRows(List.of());
            response.setPageCount(0L);
            return response;
        }

        List<PmsSpuInfo> published = pmsSpuInfoMapper.selectList(
                Wrappers.<PmsSpuInfo>lambdaQuery()
                        .in(PmsSpuInfo::getId, orderedSpuIds)
                        .eq(PmsSpuInfo::getPublishStatus, PortalConstants.PUBLISH_STATUS_ON));
        Map<Long, PmsSpuInfo> spuMap = published.stream()
                .filter(spu -> spu.getId() != null)
                .collect(Collectors.toMap(PmsSpuInfo::getId, spu -> spu, (a, b) -> a));
        List<Long> validIds = orderedSpuIds.stream().filter(spuMap::containsKey).toList();

        long total = validIds.size();
        int from = Math.max(0, (pageNum - 1) * pageSize);
        if (from >= validIds.size()) {
            response.setTotal(total);
            response.setRows(List.of());
            response.setPageCount(pageSize > 0 ? (long) Math.ceil((double) total / pageSize) : 0L);
            return response;
        }

        int to = Math.min(from + pageSize, validIds.size());
        List<PortalProductListVo> rows = new ArrayList<>();
        for (Long spuId : validIds.subList(from, to)) {
            PmsSpuInfo spu = spuMap.get(spuId);
            if (spu == null) {
                continue;
            }
            PortalProductListVo vo = new PortalProductListVo();
            vo.setId(spu.getId());
            vo.setSpuName(spu.getSpuName());
            vo.setSpuDescription(spu.getSpuDescription());
            vo.setCatalogId(spu.getCatalogId());
            vo.setBrandId(spu.getBrandId());
            vo.setCreateTime(spu.getCreateTime());
            rows.add(vo);
        }
        fillListExtras(rows);

        response.setTotal(total);
        response.setRows(rows);
        response.setPageCount(pageSize > 0 ? (long) Math.ceil((double) total / pageSize) : 0L);
        return response;
    }

    private void appendRelated(
            Long catalogId,
            Long brandId,
            int limit,
            Set<Long> seen,
            List<PortalProductListVo> result) {
        if (result.size() >= limit || (catalogId == null && brandId == null)) {
            return;
        }
        PortalProductSearchBo bo = new PortalProductSearchBo();
        bo.setPageNum(1L);
        bo.setPageSize((long) Math.min(limit + 8, 32));
        bo.setCatalogId(catalogId);
        bo.setBrandId(brandId);
        for (PortalProductListVo row : search(bo).getRows()) {
            if (row.getId() == null || seen.contains(row.getId())) {
                continue;
            }
            seen.add(row.getId());
            result.add(row);
            if (result.size() >= limit) {
                break;
            }
        }
    }

    private void fillSkuAvailableStock(List<Skus> skus) {
        if (CollectionUtils.isEmpty(skus)) {
            return;
        }
        List<Long> skuIds = skus.stream().map(Skus::getSkuId).filter(Objects::nonNull).distinct().toList();
        if (skuIds.isEmpty()) {
            return;
        }
        Map<Long, Integer> stockMap = portalStockLockService.getAvailableStockMap(skuIds);
        for (Skus sku : skus) {
            if (sku.getSkuId() == null) {
                continue;
            }
            sku.setAvailableStock(stockMap.getOrDefault(sku.getSkuId(), 0));
        }
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
        Map<Long, CommentSummary> commentMap = loadCommentSummaryMap(spuIds);
        for (PortalProductListVo row : rows) {
            row.setPrice(minPriceMap.get(row.getId()));
            row.setCoverImg(coverMap.get(row.getId()));
            if (row.getBrandId() != null) {
                row.setBrandName(brandNameMap.get(row.getBrandId()));
            }
            CommentSummary summary = commentMap.get(row.getId());
            if (summary != null) {
                row.setCommentCount(summary.total());
                row.setAvgStar(summary.avgStar());
            }
        }
    }

    private Map<Long, CommentSummary> loadCommentSummaryMap(List<Long> spuIds) {
        if (CollectionUtils.isEmpty(spuIds)) {
            return Map.of();
        }
        List<Map<String, Object>> rows = pmsSpuCommentMapper.selectSummaryBySpuIds(spuIds);
        if (CollectionUtils.isEmpty(rows)) {
            return Map.of();
        }
        Map<Long, CommentSummary> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Object spuIdObj = row.get("spuId");
            if (spuIdObj == null) {
                continue;
            }
            Long spuId = spuIdObj instanceof Number n ? n.longValue() : Long.parseLong(spuIdObj.toString());
            Object totalObj = row.get("total");
            Long total = totalObj instanceof Number n ? n.longValue() : 0L;
            Object avgObj = row.get("avgStar");
            BigDecimal avg = avgObj instanceof BigDecimal bd
                    ? bd
                    : avgObj instanceof Number n ? BigDecimal.valueOf(n.doubleValue()) : null;
            result.put(spuId, new CommentSummary(total, avg));
        }
        return result;
    }

    private record CommentSummary(Long total, BigDecimal avgStar) {}

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
