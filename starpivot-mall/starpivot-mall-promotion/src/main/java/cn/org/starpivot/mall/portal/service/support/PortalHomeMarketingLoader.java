package cn.org.starpivot.mall.portal.service.support;

import cn.org.starpivot.api.product.dto.SkuDto;
import cn.org.starpivot.api.product.dto.SpuDto;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.common.MallProductConstants;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.portal.domain.vo.PortalHomeBlockVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalHomeProductVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalSeckillPageVo;
import cn.org.starpivot.mall.portal.domain.vo.PortalSeckillSessionVo;
import cn.org.starpivot.mall.portal.service.PortalSeckillStockService;
import cn.org.starpivot.mall.sms.entity.*;
import cn.org.starpivot.mall.sms.mapper.*;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PortalHomeMarketingLoader {

    private static final int MODULE_PRODUCT_LIMIT = 4;
    private static final DateTimeFormatter TIME_LABEL = DateTimeFormatter.ofPattern("HH:mm");

    private final ProductFeignSupport productFeignSupport;
    private final SmsSeckillSessionMapper smsSeckillSessionMapper;
    private final SmsSeckillPromotionMapper smsSeckillPromotionMapper;
    private final SmsSeckillSkuRelationMapper smsSeckillSkuRelationMapper;
    private final SmsHomeSubjectMapper smsHomeSubjectMapper;
    private final SmsHomeSubjectSpuMapper smsHomeSubjectSpuMapper;
    private final PortalSeckillStockService portalSeckillStockService;

    public List<PortalHomeBlockVo> loadHomeBlocks() {
        List<PortalHomeBlockVo> blocks = new ArrayList<>(4);
        blocks.add(buildNewBlock());
        blocks.add(buildSeckillBlock());
        blocks.add(buildBudgetBlock());
        blocks.add(buildSubjectBlock());
        return blocks;
    }

    private PortalHomeBlockVo buildNewBlock() {
        PortalHomeBlockVo block = baseBlock("new", "商城新品", "品质上新 · 抢先体验");
        block.setProducts(listNewestProducts());
        return block;
    }

    private PortalHomeBlockVo buildBudgetBlock() {
        PortalHomeBlockVo block = baseBlock("budget", "9.9包邮", "超值好物 · 包邮到家");
        block.setProducts(listBudgetProducts());
        return block;
    }

    private PortalHomeBlockVo buildSubjectBlock() {
        SmsHomeSubject subject = smsHomeSubjectMapper.selectOne(
                Wrappers.<SmsHomeSubject>lambdaQuery()
                        .eq(SmsHomeSubject::getStatus, 1)
                        .orderByAsc(SmsHomeSubject::getSort)
                        .orderByDesc(SmsHomeSubject::getId)
                        .last("LIMIT 1"));
        PortalHomeBlockVo block = baseBlock("subject", "专题推荐", "精选专题 · 限时好货");
        if (subject == null) {
            return block;
        }
        block.setTitle(StringUtils.hasText(subject.getTitle()) ? subject.getTitle() : subject.getName());
        block.setSubTitle(subject.getSubTitle());
        block.setRefId(subject.getId());
        block.setUrl(StringUtils.hasText(subject.getUrl()) ? subject.getUrl() : "/portal/subject/" + subject.getId());
        block.setCoverImg(normalizeImg(subject.getImg()));
        block.setProducts(listSubjectProducts(subject.getId()));
        return block;
    }

    private PortalHomeBlockVo buildSeckillBlock() {
        PortalHomeBlockVo block = baseBlock("seckill", "限时秒杀", "整点场 · 抢完即止");
        List<SmsSeckillSession> sessions = smsSeckillSessionMapper.selectList(
                Wrappers.<SmsSeckillSession>lambdaQuery()
                        .eq(SmsSeckillSession::getStatus, 1)
                        .orderByAsc(SmsSeckillSession::getStartTime)
                        .orderByAsc(SmsSeckillSession::getId));
        if (CollectionUtils.isEmpty(sessions)) {
            return block;
        }

        SmsSeckillPromotion promotion = findActivePromotion();
        Map<Long, List<SmsSeckillSkuRelation>> relationMap = loadSeckillRelationMap(promotion);

        LocalTime now = LocalTime.now();
        List<PortalSeckillSessionVo> sessionVos = new ArrayList<>();
        Long activeSessionId = null;
        for (SmsSeckillSession session : sessions) {
            PortalSeckillSessionVo sessionVo = toSessionVo(session, now);
            if (promotion != null && session.getId() != null) {
                sessionVo.setProducts(buildSeckillProducts(relationMap.getOrDefault(session.getId(), List.of())));
            }
            sessionVos.add(sessionVo);
            if (activeSessionId == null && !"ended".equals(sessionVo.getState())) {
                activeSessionId = sessionVo.getId();
            }
        }
        if (activeSessionId == null && !sessionVos.isEmpty()) {
            activeSessionId = sessionVos.get(0).getId();
        }

        block.setSessions(sessionVos);
        block.setActiveSessionId(activeSessionId);
        block.setUrl("/portal/seckill");
        Long selectedSessionId = activeSessionId;
        PortalSeckillSessionVo active = sessionVos.stream()
                .filter(item -> Objects.equals(item.getId(), selectedSessionId))
                .findFirst()
                .orElse(null);
        if (active != null) {
            block.setProducts(active.getProducts());
        }
        return block;
    }

    private SmsSeckillPromotion findActivePromotion() {
        LocalDateTime now = LocalDateTime.now();
        return smsSeckillPromotionMapper.selectOne(
                Wrappers.<SmsSeckillPromotion>lambdaQuery()
                        .eq(SmsSeckillPromotion::getStatus, 1)
                        .and(w -> w.isNull(SmsSeckillPromotion::getStartTime).or().le(SmsSeckillPromotion::getStartTime, now))
                        .and(w -> w.isNull(SmsSeckillPromotion::getEndTime).or().ge(SmsSeckillPromotion::getEndTime, now))
                        .orderByDesc(SmsSeckillPromotion::getId)
                        .last("LIMIT 1"));
    }

    private Map<Long, List<SmsSeckillSkuRelation>> loadSeckillRelationMap(SmsSeckillPromotion promotion) {
        if (promotion == null || promotion.getId() == null) {
            return Collections.emptyMap();
        }
        List<SmsSeckillSkuRelation> relations = smsSeckillSkuRelationMapper.selectList(
                Wrappers.<SmsSeckillSkuRelation>lambdaQuery()
                        .eq(SmsSeckillSkuRelation::getPromotionId, promotion.getId())
                        .orderByAsc(SmsSeckillSkuRelation::getSeckillSort)
                        .orderByAsc(SmsSeckillSkuRelation::getId));
        if (CollectionUtils.isEmpty(relations)) {
            return Collections.emptyMap();
        }
        Map<Long, List<SmsSeckillSkuRelation>> grouped = new LinkedHashMap<>();
        for (SmsSeckillSkuRelation relation : relations) {
            if (relation.getPromotionSessionId() == null) {
                continue;
            }
            grouped.computeIfAbsent(relation.getPromotionSessionId(), key -> new ArrayList<>()).add(relation);
        }
        return grouped;
    }

    private List<PortalHomeProductVo> buildSeckillProducts(List<SmsSeckillSkuRelation> relations) {
        return buildSeckillProducts(relations, MODULE_PRODUCT_LIMIT);
    }

    private List<PortalHomeProductVo> buildSeckillProducts(List<SmsSeckillSkuRelation> relations, Integer limit) {
        if (CollectionUtils.isEmpty(relations)) {
            return List.of();
        }
        List<SmsSeckillSkuRelation> limited = limit == null ? relations : relations.stream().limit(limit).toList();
        List<Long> skuIds = limited.stream()
                .map(SmsSeckillSkuRelation::getSkuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (skuIds.isEmpty()) {
            return List.of();
        }
        Map<Long, SkuDto> skuMap = productFeignSupport.requireSkuMap(skuIds);
        Set<Long> spuIds = skuMap.values().stream()
                .map(SkuDto::getSpuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, SpuDto> spuMap = loadPublishedSpuMap(spuIds);
        Map<Long, String> coverMap = productFeignSupport.requireSpuCoverImages(new ArrayList<>(spuIds));

        List<PortalHomeProductVo> products = new ArrayList<>();
        for (SmsSeckillSkuRelation relation : limited) {
            SkuDto sku = skuMap.get(relation.getSkuId());
            if (sku == null || sku.getSpuId() == null || !spuMap.containsKey(sku.getSpuId())) {
                continue;
            }
            SpuDto spu = spuMap.get(sku.getSpuId());
            PortalHomeProductVo product = new PortalHomeProductVo();
            product.setSpuId(spu.getId());
            product.setSkuId(sku.getSkuId());
            product.setSpuName(spu.getSpuName());
            product.setPrice(sku.getPrice());
            product.setPromoPrice(relation.getSeckillPrice());
            product.setCoverImg(firstNonBlank(coverMap.get(spu.getId()), normalizeImg(sku.getSkuDefaultImg())));
            products.add(product);
        }
        return products;
    }

    private void enrichSeckillStock(
            Long promotionId,
            Long sessionId,
            List<PortalHomeProductVo> products,
            List<SmsSeckillSkuRelation> relations) {
        if (promotionId == null || sessionId == null || CollectionUtils.isEmpty(products)) {
            return;
        }
        Map<Long, SmsSeckillSkuRelation> relationMap = relations.stream()
                .filter(r -> r.getSkuId() != null)
                .collect(Collectors.toMap(SmsSeckillSkuRelation::getSkuId, r -> r, (a, b) -> a));
        for (PortalHomeProductVo product : products) {
            SmsSeckillSkuRelation relation = relationMap.get(product.getSkuId());
            if (relation == null) {
                continue;
            }
            product.setSeckillLimit(relation.getSeckillLimit());
            product.setSeckillStockRemain(
                    portalSeckillStockService.getRemainStock(promotionId, sessionId, product.getSkuId()));
        }
    }

    private List<PortalHomeProductVo> listNewestProducts() {
        List<SpuDto> spus = productFeignSupport.requireNewestPublishedSpus(MODULE_PRODUCT_LIMIT);
        return toProductVos(spus, false);
    }

    private List<PortalHomeProductVo> listBudgetProducts() {
        List<SkuDto> skus = productFeignSupport.requireBudgetSkus(MODULE_PRODUCT_LIMIT);
        return buildProductsFromSkus(skus);
    }

    private List<PortalHomeProductVo> listSubjectProducts(Long subjectId) {
        List<SmsHomeSubjectSpu> relations = smsHomeSubjectSpuMapper.selectList(
                Wrappers.<SmsHomeSubjectSpu>lambdaQuery()
                        .eq(SmsHomeSubjectSpu::getSubjectId, subjectId)
                        .orderByAsc(SmsHomeSubjectSpu::getSort)
                        .orderByAsc(SmsHomeSubjectSpu::getId)
                        .last("LIMIT " + MODULE_PRODUCT_LIMIT));
        if (CollectionUtils.isEmpty(relations)) {
            return List.of();
        }
        List<Long> spuIds = relations.stream()
                .map(SmsHomeSubjectSpu::getSpuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, SpuDto> spuMap = loadPublishedSpuMap(new HashSet<>(spuIds));
        List<SpuDto> spus = relations.stream()
                .map(SmsHomeSubjectSpu::getSpuId)
                .map(spuMap::get)
                .filter(Objects::nonNull)
                .toList();
        return toProductVos(spus, false);
    }

    private List<PortalHomeProductVo> toProductVos(List<SpuDto> spus, boolean promoAsPrice) {
        if (CollectionUtils.isEmpty(spus)) {
            return List.of();
        }
        List<Long> spuIds = spus.stream().map(SpuDto::getId).filter(Objects::nonNull).toList();
        Map<Long, String> coverMap = productFeignSupport.requireSpuCoverImages(spuIds);
        List<PortalHomeProductVo> products = new ArrayList<>();
        for (SpuDto spu : spus) {
            if (spu.getId() == null) {
                continue;
            }
            PortalHomeProductVo product = new PortalHomeProductVo();
            product.setSpuId(spu.getId());
            product.setSpuName(spu.getSpuName());
            product.setCoverImg(coverMap.get(spu.getId()));
            products.add(product);
        }
        return products;
    }

    private List<PortalHomeProductVo> buildProductsFromSkus(List<SkuDto> skus) {
        if (CollectionUtils.isEmpty(skus)) {
            return List.of();
        }
        Set<Long> spuIds = skus.stream().map(SkuDto::getSpuId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SpuDto> spuMap = loadPublishedSpuMap(spuIds);
        Map<Long, String> coverMap = productFeignSupport.requireSpuCoverImages(new ArrayList<>(spuIds));
        List<PortalHomeProductVo> products = new ArrayList<>();
        for (SkuDto sku : skus) {
            if (sku.getSpuId() == null) {
                continue;
            }
            SpuDto spu = spuMap.get(sku.getSpuId());
            if (spu == null) {
                continue;
            }
            PortalHomeProductVo product = new PortalHomeProductVo();
            product.setSpuId(spu.getId());
            product.setSkuId(sku.getSkuId());
            product.setSpuName(spu.getSpuName());
            product.setPrice(sku.getPrice());
            product.setCoverImg(firstNonBlank(coverMap.get(spu.getId()), normalizeImg(sku.getSkuDefaultImg())));
            products.add(product);
        }
        return products;
    }

    private Map<Long, SpuDto> loadPublishedSpuMap(Collection<Long> spuIds) {
        if (CollectionUtils.isEmpty(spuIds)) {
            return Collections.emptyMap();
        }
        return productFeignSupport.requireSpuMap(new ArrayList<>(spuIds)).entrySet().stream()
                .filter(e -> Integer.valueOf(MallProductConstants.PUBLISH_STATUS_ON).equals(e.getValue().getPublishStatus()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }

    private PortalSeckillSessionVo toSessionVo(SmsSeckillSession session, LocalTime now) {
        PortalSeckillSessionVo vo = new PortalSeckillSessionVo();
        vo.setId(session.getId());
        vo.setName(session.getName());
        LocalTime start = session.getStartTime() != null ? session.getStartTime().toLocalTime() : LocalTime.MIN;
        LocalTime end = session.getEndTime() != null ? session.getEndTime().toLocalTime() : LocalTime.MAX;
        vo.setStartLabel(start.format(TIME_LABEL));
        vo.setEndLabel(end.format(TIME_LABEL));
        if (!now.isBefore(start) && now.isBefore(end)) {
            vo.setState("ongoing");
        } else if (now.isBefore(start)) {
            vo.setState("upcoming");
        } else {
            vo.setState("ended");
        }
        return vo;
    }

    private PortalHomeBlockVo baseBlock(String code, String title, String subTitle) {
        PortalHomeBlockVo block = new PortalHomeBlockVo();
        block.setCode(code);
        block.setTitle(title);
        block.setSubTitle(subTitle);
        return block;
    }

    private String normalizeImg(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        return StorageObjectPathUtils.normalizeToObjectName(raw.trim());
    }

    private String firstNonBlank(String primary, String fallback) {
        return StringUtils.hasText(primary) ? primary : fallback;
    }

    public PortalSeckillPageVo loadSeckillPage(Long sessionId) {
        PortalSeckillPageVo page = new PortalSeckillPageVo();
        page.setTitle("限时秒杀");
        page.setSubTitle("整点场 · 抢完即止");

        List<SmsSeckillSession> sessions = smsSeckillSessionMapper.selectList(
                Wrappers.<SmsSeckillSession>lambdaQuery()
                        .eq(SmsSeckillSession::getStatus, 1)
                        .orderByAsc(SmsSeckillSession::getStartTime)
                        .orderByAsc(SmsSeckillSession::getId));
        if (CollectionUtils.isEmpty(sessions)) {
            return page;
        }

        SmsSeckillPromotion promotion = findActivePromotion();
        Map<Long, List<SmsSeckillSkuRelation>> relationMap = loadSeckillRelationMap(promotion);

        LocalTime now = LocalTime.now();
        List<PortalSeckillSessionVo> sessionVos = new ArrayList<>();
        Long activeSessionId = null;
        for (SmsSeckillSession session : sessions) {
            PortalSeckillSessionVo sessionVo = toSessionVo(session, now);
            sessionVos.add(sessionVo);
            if (activeSessionId == null && !"ended".equals(sessionVo.getState())) {
                activeSessionId = sessionVo.getId();
            }
        }
        if (activeSessionId == null) {
            activeSessionId = sessionVos.get(0).getId();
        }

        Long selectedSessionId = sessionId != null ? sessionId : activeSessionId;
        Long finalSelectedSessionId = selectedSessionId;
        boolean validSession = sessionVos.stream()
                .anyMatch(item -> Objects.equals(item.getId(), finalSelectedSessionId));
        if (!validSession) {
            selectedSessionId = activeSessionId;
        }

        page.setSessions(sessionVos);
        page.setActiveSessionId(selectedSessionId);
        if (promotion != null && selectedSessionId != null) {
            page.setPromotionId(promotion.getId());
            List<SmsSeckillSkuRelation> relations = relationMap.getOrDefault(selectedSessionId, List.of());
            portalSeckillStockService.warmup(promotion.getId(), relations);
            List<PortalHomeProductVo> products = buildSeckillProducts(relations, null);
            enrichSeckillStock(promotion.getId(), selectedSessionId, products, relations);
            page.setProducts(products);
        }
        return page;
    }
}
