package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.domain.PageReqBo;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.storage.StorageObjectPathUtils;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.entity.PmsSpuImages;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuImagesMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.portal.domain.bo.PortalCollectAddBo;
import cn.org.starpivot.mall.portal.domain.bo.PortalCollectSubjectAddBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalCollectVo;
import cn.org.starpivot.mall.portal.service.PortalCollectService;
import cn.org.starpivot.mall.sms.entity.SmsHomeSubject;
import cn.org.starpivot.mall.sms.mapper.SmsHomeSubjectMapper;
import cn.org.starpivot.mall.ums.entity.UmsMemberCollectSpu;
import cn.org.starpivot.mall.ums.entity.UmsMemberCollectSubject;
import cn.org.starpivot.mall.ums.mapper.UmsMemberCollectSpuMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberCollectSubjectMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PortalCollectServiceImpl implements PortalCollectService {

    private final UmsMemberCollectSpuMapper collectSpuMapper;
    private final UmsMemberCollectSubjectMapper collectSubjectMapper;
    private final SmsHomeSubjectMapper smsHomeSubjectMapper;
    private final PmsSpuInfoMapper pmsSpuInfoMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final PmsSpuImagesMapper pmsSpuImagesMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PortalCollectVo> pageList(Long memberId, PageReqBo pageReq) {
        Page<UmsMemberCollectSpu> page = collectSpuMapper.selectPage(
                new Page<>(pageReq.getPageNum(), pageReq.getPageSize()),
                Wrappers.<UmsMemberCollectSpu>lambdaQuery()
                        .eq(UmsMemberCollectSpu::getMemberId, memberId)
                        .orderByDesc(UmsMemberCollectSpu::getCreateTime));

        List<UmsMemberCollectSpu> records = page.getRecords();
        List<PortalCollectVo> rows = toVoList(records);

        PageResponse<PortalCollectVo> response = new PageResponse<>();
        response.setTotal(page.getTotal());
        response.setRows(rows);
        response.setPageNum(pageReq.getPageNum());
        response.setPageSize(pageReq.getPageSize());
        response.setPageCount((long) page.getPages());
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Long memberId, PortalCollectAddBo bo) {
        Long spuId = bo.getSpuId();
        PmsSpuInfo spu = pmsSpuInfoMapper.selectById(spuId);
        if (spu == null || !Integer.valueOf(PortalConstants.PUBLISH_STATUS_ON).equals(spu.getPublishStatus())) {
            throw new BizException("商品不存在或已下架");
        }

        Long exists = collectSpuMapper.selectCount(
                Wrappers.<UmsMemberCollectSpu>lambdaQuery()
                        .eq(UmsMemberCollectSpu::getMemberId, memberId)
                        .eq(UmsMemberCollectSpu::getSpuId, spuId));
        if (exists != null && exists > 0) {
            return;
        }

        UmsMemberCollectSpu row = new UmsMemberCollectSpu();
        row.setMemberId(memberId);
        row.setSpuId(spuId);
        row.setSpuName(spu.getSpuName());
        row.setSpuImg(resolveCover(spuId));
        row.setCreateTime(LocalDateTime.now());
        collectSpuMapper.insert(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long memberId, Long spuId) {
        collectSpuMapper.delete(
                Wrappers.<UmsMemberCollectSpu>lambdaQuery()
                        .eq(UmsMemberCollectSpu::getMemberId, memberId)
                        .eq(UmsMemberCollectSpu::getSpuId, spuId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCollected(Long memberId, Long spuId) {
        if (memberId == null || spuId == null) {
            return false;
        }
        Long count = collectSpuMapper.selectCount(
                Wrappers.<UmsMemberCollectSpu>lambdaQuery()
                        .eq(UmsMemberCollectSpu::getMemberId, memberId)
                        .eq(UmsMemberCollectSpu::getSpuId, spuId));
        return count != null && count > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addSubject(Long memberId, PortalCollectSubjectAddBo bo) {
        Long subjectId = bo.getSubjectId();
        SmsHomeSubject subject = smsHomeSubjectMapper.selectById(subjectId);
        if (subject == null || !Integer.valueOf(1).equals(subject.getStatus())) {
            throw new BizException("专题不存在或已下线");
        }
        Long exists = collectSubjectMapper.selectCount(
                Wrappers.<UmsMemberCollectSubject>lambdaQuery()
                        .eq(UmsMemberCollectSubject::getMemberId, memberId)
                        .eq(UmsMemberCollectSubject::getSubjectId, subjectId));
        if (exists != null && exists > 0) {
            return;
        }
        UmsMemberCollectSubject row = new UmsMemberCollectSubject();
        row.setMemberId(memberId);
        row.setSubjectId(subjectId);
        row.setSubjectName(subject.getTitle() != null ? subject.getTitle() : subject.getName());
        row.setSubjectImg(StorageObjectPathUtils.normalizeToObjectName(subject.getImg()));
        row.setSubjectUrl(StringUtils.hasText(subject.getUrl()) ? subject.getUrl() : "/portal/subject/" + subjectId);
        collectSubjectMapper.insert(row);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeSubject(Long memberId, Long subjectId) {
        collectSubjectMapper.delete(
                Wrappers.<UmsMemberCollectSubject>lambdaQuery()
                        .eq(UmsMemberCollectSubject::getMemberId, memberId)
                        .eq(UmsMemberCollectSubject::getSubjectId, subjectId));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isSubjectCollected(Long memberId, Long subjectId) {
        if (memberId == null || subjectId == null) {
            return false;
        }
        Long count = collectSubjectMapper.selectCount(
                Wrappers.<UmsMemberCollectSubject>lambdaQuery()
                        .eq(UmsMemberCollectSubject::getMemberId, memberId)
                        .eq(UmsMemberCollectSubject::getSubjectId, subjectId));
        return count != null && count > 0;
    }

    private List<PortalCollectVo> toVoList(List<UmsMemberCollectSpu> records) {
        if (CollectionUtils.isEmpty(records)) {
            return List.of();
        }
        List<Long> spuIds = records.stream().map(UmsMemberCollectSpu::getSpuId).filter(Objects::nonNull).distinct().toList();
        Map<Long, PmsSpuInfo> spuMap = pmsSpuInfoMapper.selectBatchIds(spuIds).stream()
                .collect(Collectors.toMap(PmsSpuInfo::getId, s -> s, (a, b) -> a));

        List<PmsSkuInfo> skus = pmsSkuInfoMapper.selectList(
                Wrappers.<PmsSkuInfo>lambdaQuery().in(PmsSkuInfo::getSpuId, spuIds));
        Map<Long, BigDecimal> minPriceMap = new HashMap<>();
        Map<Long, Long> defaultSkuMap = new HashMap<>();
        for (PmsSkuInfo sku : skus) {
            if (sku.getSpuId() == null || sku.getPrice() == null || sku.getSkuId() == null) {
                continue;
            }
            BigDecimal existing = minPriceMap.get(sku.getSpuId());
            if (existing == null || sku.getPrice().compareTo(existing) < 0) {
                minPriceMap.put(sku.getSpuId(), sku.getPrice());
                defaultSkuMap.put(sku.getSpuId(), sku.getSkuId());
            } else if (sku.getPrice().compareTo(existing) == 0) {
                defaultSkuMap.putIfAbsent(sku.getSpuId(), sku.getSkuId());
            }
        }

        List<PortalCollectVo> rows = new ArrayList<>(records.size());
        for (UmsMemberCollectSpu record : records) {
            PortalCollectVo vo = new PortalCollectVo();
            vo.setId(record.getId());
            vo.setSpuId(record.getSpuId());
            vo.setSpuName(record.getSpuName());
            vo.setSpuImg(record.getSpuImg());
            vo.setCreateTime(record.getCreateTime());
            vo.setPrice(minPriceMap.get(record.getSpuId()));
            vo.setDefaultSkuId(defaultSkuMap.get(record.getSpuId()));
            PmsSpuInfo spu = spuMap.get(record.getSpuId());
            if (spu != null) {
                vo.setPublishStatus(spu.getPublishStatus());
                if (!StringUtils.hasText(vo.getSpuName())) {
                    vo.setSpuName(spu.getSpuName());
                }
            }
            rows.add(vo);
        }
        return rows;
    }

    private String resolveCover(Long spuId) {
        PmsSpuImages img = pmsSpuImagesMapper.selectOne(
                Wrappers.<PmsSpuImages>lambdaQuery()
                        .eq(PmsSpuImages::getSpuId, spuId)
                        .orderByDesc(PmsSpuImages::getDefaultImg)
                        .orderByAsc(PmsSpuImages::getImgSort)
                        .last("LIMIT 1"));
        if (img == null || !StringUtils.hasText(img.getImgUrl())) {
            return null;
        }
        return StorageObjectPathUtils.normalizeToObjectName(img.getImgUrl().trim());
    }
}
