package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.sms.domain.bo.SeckillPromotionReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SeckillPromotionSaveBo;
import cn.org.starpivot.mall.sms.domain.bo.SeckillSkuRelationBo;
import cn.org.starpivot.mall.sms.domain.vo.SeckillPromotionVo;
import cn.org.starpivot.mall.sms.domain.vo.SeckillSkuRelationVo;
import cn.org.starpivot.mall.sms.entity.SmsSeckillPromotion;
import cn.org.starpivot.mall.sms.entity.SmsSeckillSession;
import cn.org.starpivot.mall.sms.entity.SmsSeckillSkuRelation;
import cn.org.starpivot.mall.sms.mapper.SmsSeckillPromotionMapper;
import cn.org.starpivot.mall.sms.mapper.SmsSeckillSessionMapper;
import cn.org.starpivot.mall.sms.mapper.SmsSeckillSkuRelationMapper;
import cn.org.starpivot.mall.sms.service.SmsSeckillPromotionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.time.LocalDateTime;
import java.util.Collections;
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
public class SmsSeckillPromotionServiceImpl implements SmsSeckillPromotionService {

    private final SmsSeckillPromotionMapper smsSeckillPromotionMapper;
    private final SmsSeckillSkuRelationMapper smsSeckillSkuRelationMapper;
    private final SmsSeckillSessionMapper smsSeckillSessionMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SeckillPromotionVo> pageList(SeckillPromotionReqBo reqBo) {
        Page<SmsSeckillPromotion> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<SmsSeckillPromotion> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(reqBo.getTitle())) {
            wrapper.like(SmsSeckillPromotion::getTitle, reqBo.getTitle());
        }
        if (reqBo.getStatus() != null) {
            wrapper.eq(SmsSeckillPromotion::getStatus, reqBo.getStatus());
        }
        wrapper.orderByDesc(SmsSeckillPromotion::getId);
        IPage<SmsSeckillPromotion> pageList = smsSeckillPromotionMapper.selectPage(page, wrapper);

        PageResponse<SeckillPromotionVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public SeckillPromotionVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "活动ID不能为空");
        }
        SmsSeckillPromotion entity = smsSeckillPromotionMapper.selectById(id);
        if (entity == null) {
            throw new BizException("秒杀活动不存在");
        }
        SeckillPromotionVo vo = toVo(entity);
        vo.setSkuList(loadSkuList(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SeckillPromotionSaveBo bo) {
        SmsSeckillPromotion entity = new SmsSeckillPromotion();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        entity.setCreateTime(LocalDateTime.now());
        if (entity.getStatus() == null) {
            entity.setStatus(0);
        }
        smsSeckillPromotionMapper.insert(entity);
        saveSkuList(entity.getId(), bo.getSkuList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SeckillPromotionSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改活动时 id 不能为空");
        }
        SmsSeckillPromotion existing = smsSeckillPromotionMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("秒杀活动不存在");
        }
        SmsSeckillPromotion entity = new SmsSeckillPromotion();
        BeanUtils.copyProperties(bo, entity);
        smsSeckillPromotionMapper.updateById(entity);
        smsSeckillSkuRelationMapper.delete(Wrappers.<SmsSeckillSkuRelation>lambdaQuery()
                .eq(SmsSeckillSkuRelation::getPromotionId, bo.getId()));
        saveSkuList(bo.getId(), bo.getSkuList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsSeckillSkuRelationMapper.delete(
                Wrappers.<SmsSeckillSkuRelation>lambdaQuery().in(SmsSeckillSkuRelation::getPromotionId, ids));
        smsSeckillPromotionMapper.delete(
                Wrappers.<SmsSeckillPromotion>lambdaQuery().in(SmsSeckillPromotion::getId, ids));
    }

    private void saveSkuList(Long promotionId, List<SeckillSkuRelationBo> skuList) {
        if (CollectionUtils.isEmpty(skuList)) {
            return;
        }
        for (SeckillSkuRelationBo skuBo : skuList) {
            if (skuBo.getSkuId() == null) {
                continue;
            }
            SmsSeckillSkuRelation relation = new SmsSeckillSkuRelation();
            relation.setPromotionId(promotionId);
            relation.setPromotionSessionId(skuBo.getPromotionSessionId());
            relation.setSkuId(skuBo.getSkuId());
            relation.setSeckillPrice(skuBo.getSeckillPrice());
            relation.setSeckillCount(skuBo.getSeckillCount());
            relation.setSeckillLimit(skuBo.getSeckillLimit());
            relation.setSeckillSort(skuBo.getSeckillSort() != null ? skuBo.getSeckillSort() : 0);
            smsSeckillSkuRelationMapper.insert(relation);
        }
    }

    private List<SeckillSkuRelationVo> loadSkuList(Long promotionId) {
        List<SmsSeckillSkuRelation> relations = smsSeckillSkuRelationMapper.selectList(
                Wrappers.<SmsSeckillSkuRelation>lambdaQuery()
                        .eq(SmsSeckillSkuRelation::getPromotionId, promotionId)
                        .orderByAsc(SmsSeckillSkuRelation::getSeckillSort)
                        .orderByAsc(SmsSeckillSkuRelation::getId));
        if (CollectionUtils.isEmpty(relations)) {
            return Collections.emptyList();
        }
        List<Long> skuIds = relations.stream()
                .map(SmsSeckillSkuRelation::getSkuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Long> sessionIds = relations.stream()
                .map(SmsSeckillSkuRelation::getPromotionSessionId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> skuNameMap = CollectionUtils.isEmpty(skuIds)
                ? Collections.emptyMap()
                : pmsSkuInfoMapper.selectBatchIds(skuIds).stream()
                        .collect(Collectors.toMap(PmsSkuInfo::getSkuId, PmsSkuInfo::getSkuName, (a, b) -> a));
        Map<Long, String> sessionNameMap = CollectionUtils.isEmpty(sessionIds)
                ? Collections.emptyMap()
                : smsSeckillSessionMapper.selectBatchIds(sessionIds).stream()
                        .collect(Collectors.toMap(SmsSeckillSession::getId, SmsSeckillSession::getName, (a, b) -> a));

        return relations.stream().map(relation -> {
            SeckillSkuRelationVo vo = new SeckillSkuRelationVo();
            BeanUtils.copyProperties(relation, vo);
            vo.setSkuName(skuNameMap.get(relation.getSkuId()));
            vo.setSessionName(sessionNameMap.get(relation.getPromotionSessionId()));
            return vo;
        }).collect(Collectors.toList());
    }

    private SeckillPromotionVo toVo(SmsSeckillPromotion entity) {
        SeckillPromotionVo vo = new SeckillPromotionVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
