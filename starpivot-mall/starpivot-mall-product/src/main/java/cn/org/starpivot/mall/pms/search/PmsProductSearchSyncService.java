package cn.org.starpivot.mall.pms.search;

import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.common.MallProductConstants;
import cn.org.starpivot.mall.config.MallElasticsearchProperties;
import cn.org.starpivot.mall.pms.entity.PmsBrand;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.entity.PmsSpuImages;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsBrandMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuImagesMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 商品搜索索引同步（失败仅记日志，不影响主业务事务）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PmsProductSearchSyncService {

    private final MallElasticsearchProperties elasticsearchProperties;
    private final ObjectProvider<PmsProductElasticsearchService> elasticsearchServiceProvider;
    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSpuImagesMapper pmsSpuImagesMapper;
    private final PmsBrandMapper pmsBrandMapper;

    public boolean isElasticsearchActive() {
        return elasticsearchProperties.isEnabled() && elasticsearchServiceProvider.getIfAvailable() != null;
    }

    public void syncPublishedSpu(Long spuId) {
        if (!isElasticsearchActive() || spuId == null) {
            return;
        }
        PmsProductElasticsearchService es = elasticsearchServiceProvider.getIfAvailable();
        if (es == null) {
            return;
        }
        try {
            PmsProductDocument document = buildDocument(spuId);
            if (document == null) {
                es.deleteById(spuId);
                return;
            }
            es.indexDocument(document);
        } catch (Exception ex) {
            log.warn("Sync product to elasticsearch failed, spuId={}", spuId, ex);
        }
    }

    public void removeSpu(Long spuId) {
        if (!isElasticsearchActive() || spuId == null) {
            return;
        }
        PmsProductElasticsearchService es = elasticsearchServiceProvider.getIfAvailable();
        if (es == null) {
            return;
        }
        es.deleteById(spuId);
    }

    public int reindexAllPublished() {
        if (!isElasticsearchActive()) {
            return 0;
        }
        PmsProductElasticsearchService es = elasticsearchServiceProvider.getIfAvailable();
        if (es == null) {
            return 0;
        }
        List<PmsSpuInfo> published = pmsSpuInfoMapper.selectList(
                Wrappers.<PmsSpuInfo>lambdaQuery()
                        .eq(PmsSpuInfo::getPublishStatus, MallProductConstants.PUBLISH_STATUS_ON)
                        .orderByAsc(PmsSpuInfo::getId));
        List<PmsProductDocument> batch = new ArrayList<>();
        int count = 0;
        for (PmsSpuInfo spu : published) {
            PmsProductDocument document = buildDocument(spu.getId());
            if (document != null) {
                batch.add(document);
                count++;
            }
            if (batch.size() >= 200) {
                es.bulkIndex(batch);
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            es.bulkIndex(batch);
        }
        log.info("Reindexed {} published products to elasticsearch", count);
        return count;
    }

    private PmsProductDocument buildDocument(Long spuId) {
        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(spuId);
        if (spu == null || !Integer.valueOf(MallProductConstants.PUBLISH_STATUS_ON).equals(spu.getPublishStatus())) {
            return null;
        }
        PmsProductDocument document = new PmsProductDocument();
        document.setId(spu.getId());
        document.setSpuName(spu.getSpuName());
        document.setSpuDescription(spu.getSpuDescription());
        document.setCatalogId(spu.getCatalogId());
        document.setBrandId(spu.getBrandId());
        document.setPublishStatus(spu.getPublishStatus());
        if (spu.getCreateTime() != null) {
            document.setCreateTime(spu.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (spu.getBrandId() != null) {
            PmsBrand brand = pmsBrandMapper.selectById(spu.getBrandId());
            if (brand != null) {
                document.setBrandName(brand.getName());
            }
        }
        List<PmsSkuInfo> skus = pmsSkuInfoMapper.selectList(
                Wrappers.<PmsSkuInfo>lambdaQuery().eq(PmsSkuInfo::getSpuId, spuId));
        BigDecimal minPrice = skus.stream()
                .map(PmsSkuInfo::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(null);
        if (minPrice != null) {
            document.setPrice(minPrice.doubleValue());
        }
        PmsSpuImages cover = pmsSpuImagesMapper.selectOne(
                Wrappers.<PmsSpuImages>lambdaQuery()
                        .eq(PmsSpuImages::getSpuId, spuId)
                        .orderByDesc(PmsSpuImages::getDefaultImg)
                        .orderByAsc(PmsSpuImages::getImgSort)
                        .orderByAsc(PmsSpuImages::getId)
                        .last("LIMIT 1"));
        if (cover != null && StringUtils.hasText(cover.getImgUrl())) {
            document.setCoverImg(StorageObjectPathUtils.normalizeToObjectName(cover.getImgUrl().trim()));
        }
        return document;
    }
}
