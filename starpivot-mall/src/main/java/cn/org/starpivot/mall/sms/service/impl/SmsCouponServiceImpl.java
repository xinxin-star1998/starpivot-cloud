package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.entity.PmsCategory;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsCategoryMapper;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.sms.domain.bo.CouponCategoryBo;
import cn.org.starpivot.mall.sms.domain.bo.CouponReqBo;
import cn.org.starpivot.mall.sms.domain.bo.CouponSaveBo;
import cn.org.starpivot.mall.sms.domain.bo.CouponSpuBo;
import cn.org.starpivot.mall.sms.domain.vo.CouponCategoryVo;
import cn.org.starpivot.mall.sms.domain.vo.CouponSpuVo;
import cn.org.starpivot.mall.sms.domain.vo.CouponVo;
import cn.org.starpivot.mall.sms.entity.SmsCoupon;
import cn.org.starpivot.mall.sms.entity.SmsCouponHistory;
import cn.org.starpivot.mall.sms.entity.SmsCouponSpuCategoryRelation;
import cn.org.starpivot.mall.sms.entity.SmsCouponSpuRelation;
import cn.org.starpivot.mall.sms.mapper.SmsCouponHistoryMapper;
import cn.org.starpivot.mall.sms.mapper.SmsCouponMapper;
import cn.org.starpivot.mall.sms.mapper.SmsCouponSpuCategoryRelationMapper;
import cn.org.starpivot.mall.sms.mapper.SmsCouponSpuRelationMapper;
import cn.org.starpivot.mall.sms.service.SmsCouponService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现类。
 * <p>
 * 实现 {@link SmsCouponService}，处理优惠券相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see SmsCouponService
 */

@Service

@RequiredArgsConstructor

public class SmsCouponServiceImpl implements SmsCouponService {

    private static final int USE_TYPE_ALL = 0;

    private static final int USE_TYPE_CATEGORY = 1;

    private static final int USE_TYPE_SPU = 2;

    private static final int COUPON_TYPE_MEMBER = 1;

    private static final int PUBLISHED = 1;

    private final SmsCouponMapper smsCouponMapper;

    private final SmsCouponHistoryMapper smsCouponHistoryMapper;

    private final SmsCouponSpuRelationMapper smsCouponSpuRelationMapper;

    private final SmsCouponSpuCategoryRelationMapper smsCouponSpuCategoryRelationMapper;

    private final PmsSpuInfoMapper pmsSpuInfoMapper;

    private final PmsCategoryMapper pmsCategoryMapper;

    @Override

    @Transactional(readOnly = true)

    public PageResponse<CouponVo> pageList(CouponReqBo reqBo) {

        Page<SmsCoupon> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());

        LambdaQueryWrapper<SmsCoupon> wrapper = Wrappers.lambdaQuery();

        if (StringUtils.hasText(reqBo.getCouponName())) {

            wrapper.like(SmsCoupon::getCouponName, reqBo.getCouponName());

        }

        if (reqBo.getPublish() != null) {

            wrapper.eq(SmsCoupon::getPublish, reqBo.getPublish());

        }

        if (reqBo.getUseType() != null) {

            wrapper.eq(SmsCoupon::getUseType, reqBo.getUseType());

        }

        if (reqBo.getValidityStart() != null && reqBo.getValidityEnd() != null) {

            wrapper.le(SmsCoupon::getStartTime, reqBo.getValidityEnd());

            wrapper.ge(SmsCoupon::getEndTime, reqBo.getValidityStart());

        }

        wrapper.orderByDesc(SmsCoupon::getId);

        IPage<SmsCoupon> pageList = smsCouponMapper.selectPage(page, wrapper);

        PageResponse<CouponVo> pageResponse = new PageResponse<>();

        pageResponse.setTotal(pageList.getTotal());

        pageResponse.setRows(pageList.getRecords().stream().map(this::toVo).collect(Collectors.toList()));

        pageResponse.setPageNum(pageList.getCurrent());

        pageResponse.setPageSize(pageList.getSize());

        pageResponse.setPageCount(pageList.getPages());

        return pageResponse;

    }

    @Override

    @Transactional(readOnly = true)

    public CouponVo getById(Long id) {

        if (id == null) {

            throw new BizException(ErrorCode.PARAM_INVALID, "优惠券ID不能为空");

        }

        SmsCoupon entity = smsCouponMapper.selectById(id);

        if (entity == null) {

            throw new BizException("优惠券不存在");

        }

        CouponVo vo = toVo(entity);

        vo.setSpuList(loadSpuList(id));

        vo.setCategoryList(loadCategoryList(id));

        return vo;

    }

    @Override

    @Transactional(rollbackFor = Exception.class)

    public void add(CouponSaveBo bo) {

        validateSaveBo(bo, null);

        SmsCoupon entity = new SmsCoupon();

        BeanUtils.copyProperties(bo, entity);

        entity.setId(null);

        entity.setReceiveCount(0);

        entity.setUseCount(0);

        smsCouponMapper.insert(entity);

        saveRelations(entity.getId(), bo);

    }

    @Override

    @Transactional(rollbackFor = Exception.class)

    public void update(CouponSaveBo bo) {

        if (bo.getId() == null) {

            throw new BizException(ErrorCode.PARAM_INVALID, "修改优惠券时 id 不能为空");

        }

        SmsCoupon existing = smsCouponMapper.selectById(bo.getId());

        if (existing == null) {

            throw new BizException("优惠券不存在");

        }

        validateSaveBo(bo, existing);

        validatePublishedImmutableFields(existing, bo);

        SmsCoupon entity = new SmsCoupon();

        BeanUtils.copyProperties(bo, entity);

        entity.setReceiveCount(existing.getReceiveCount());

        entity.setUseCount(existing.getUseCount());

        smsCouponMapper.updateById(entity);

        removeRelations(bo.getId());

        saveRelations(bo.getId(), bo);

    }

    @Override

    @Transactional(rollbackFor = Exception.class)

    public void removeByIds(List<Long> ids) {

        if (CollectionUtils.isEmpty(ids)) {

            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");

        }

        for (Long id : ids) {

            SmsCoupon existing = smsCouponMapper.selectById(id);

            if (existing == null) {

                continue;

            }

            int received = existing.getReceiveCount() == null ? 0 : existing.getReceiveCount();

            if (received > 0) {

                throw new BizException("优惠券「" + existing.getCouponName() + "」已有用户领取，不能删除");

            }

            Long historyCount = smsCouponHistoryMapper.selectCount(

                    Wrappers.<SmsCouponHistory>lambdaQuery().eq(SmsCouponHistory::getCouponId, id));

            if (historyCount != null && historyCount > 0) {

                throw new BizException("优惠券「" + existing.getCouponName() + "」已有领取记录，不能删除");

            }

            removeRelations(id);

        }

        smsCouponMapper.delete(Wrappers.<SmsCoupon>lambdaQuery().in(SmsCoupon::getId, ids));

    }

    @Override

    @Transactional(rollbackFor = Exception.class)

    public void updatePublishStatus(Long id, Integer publish) {

        if (id == null || publish == null) {

            throw new BizException(ErrorCode.PARAM_INVALID, "参数不能为空");

        }

        if (publish != 0 && publish != PUBLISHED) {

            throw new BizException(ErrorCode.PARAM_INVALID, "发布状态无效");

        }

        SmsCoupon existing = smsCouponMapper.selectById(id);

        if (existing == null) {

            throw new BizException("优惠券不存在");

        }

        if (Objects.equals(existing.getPublish(), publish)) {

            return;

        }

        if (publish == PUBLISHED) {

            validateSaveBo(toSaveBo(existing), existing);

        }

        SmsCoupon patch = new SmsCoupon();

        patch.setId(id);

        patch.setPublish(publish);

        smsCouponMapper.updateById(patch);

    }

    private CouponSaveBo toSaveBo(SmsCoupon entity) {

        CouponSaveBo bo = new CouponSaveBo();

        BeanUtils.copyProperties(entity, bo);

        bo.setId(entity.getId());

        if (entity.getUseType() != null && entity.getUseType() == USE_TYPE_SPU) {

            bo.setSpuList(loadSpuList(entity.getId()).stream().map(vo -> {

                CouponSpuBo spuBo = new CouponSpuBo();

                spuBo.setSpuId(vo.getSpuId());

                spuBo.setSpuName(vo.getSpuName());

                return spuBo;

            }).collect(Collectors.toList()));

        } else if (entity.getUseType() != null && entity.getUseType() == USE_TYPE_CATEGORY) {

            bo.setCategoryList(loadCategoryList(entity.getId()).stream().map(vo -> {

                CouponCategoryBo categoryBo = new CouponCategoryBo();

                categoryBo.setCategoryId(vo.getCategoryId());

                categoryBo.setCategoryName(vo.getCategoryName());

                return categoryBo;

            }).collect(Collectors.toList()));

        }

        return bo;

    }

    private void validateSaveBo(CouponSaveBo bo, SmsCoupon existing) {

        if (bo.getMinPoint().compareTo(bo.getAmount()) < 0) {

            throw new BizException(ErrorCode.PARAM_INVALID, "使用门槛不能小于面额");

        }

        if (!bo.getEndTime().isAfter(bo.getStartTime())) {

            throw new BizException(ErrorCode.PARAM_INVALID, "使用结束时间必须晚于开始时间");

        }

        if (!bo.getEnableEndTime().isAfter(bo.getEnableStartTime())) {

            throw new BizException(ErrorCode.PARAM_INVALID, "领取结束时间必须晚于开始时间");

        }

        if (bo.getCouponType() != null && bo.getCouponType() == COUPON_TYPE_MEMBER && bo.getMemberLevel() == null) {

            throw new BizException(ErrorCode.PARAM_INVALID, "会员赠券必须选择会员等级");

        }

        if (bo.getUseType() == USE_TYPE_CATEGORY) {

            if (CollectionUtils.isEmpty(bo.getCategoryList())) {

                throw new BizException(ErrorCode.PARAM_INVALID, "指定分类时必须至少选择一个分类");

            }

        } else if (bo.getUseType() == USE_TYPE_SPU) {

            if (CollectionUtils.isEmpty(bo.getSpuList())) {

                throw new BizException(ErrorCode.PARAM_INVALID, "指定商品时必须至少选择一个商品");

            }

        }

        if (existing != null && bo.getPublishCount() != null && existing.getReceiveCount() != null

                && bo.getPublishCount() < existing.getReceiveCount()) {

            throw new BizException(ErrorCode.PARAM_INVALID, "发行数量不能小于已领取数量");

        }

    }

    private void validatePublishedImmutableFields(SmsCoupon existing, CouponSaveBo bo) {

        if (existing.getPublish() == null || existing.getPublish() != PUBLISHED) {

            return;

        }

        if (!decimalEquals(existing.getAmount(), bo.getAmount())) {

            throw new BizException("已发布优惠券不允许修改面额");

        }

        if (!decimalEquals(existing.getMinPoint(), bo.getMinPoint())) {

            throw new BizException("已发布优惠券不允许修改使用门槛");

        }

        if (!Objects.equals(existing.getUseType(), bo.getUseType())) {

            throw new BizException("已发布优惠券不允许修改适用范围");

        }

        if (isScopeChanged(existing.getId(), existing.getUseType(), bo)) {

            throw new BizException("已发布优惠券不允许修改关联商品或分类");

        }

    }

    private boolean decimalEquals(BigDecimal left, BigDecimal right) {

        if (left == null && right == null) {

            return true;

        }

        if (left == null || right == null) {

            return false;

        }

        return left.compareTo(right) == 0;

    }

    private boolean isScopeChanged(Long couponId, Integer useType, CouponSaveBo bo) {

        if (useType == null || useType == USE_TYPE_ALL) {

            return false;

        }

        if (useType == USE_TYPE_SPU) {

            Set<Long> oldIds = loadSpuList(couponId).stream()

                    .map(CouponSpuVo::getSpuId)

                    .filter(Objects::nonNull)

                    .collect(Collectors.toSet());

            Set<Long> newIds = CollectionUtils.isEmpty(bo.getSpuList()) ? Collections.emptySet()

                    : bo.getSpuList().stream()

                            .map(CouponSpuBo::getSpuId)

                            .filter(Objects::nonNull)

                            .collect(Collectors.toSet());

            return !oldIds.equals(newIds);

        }

        if (useType == USE_TYPE_CATEGORY) {

            Set<Long> oldIds = loadCategoryList(couponId).stream()

                    .map(CouponCategoryVo::getCategoryId)

                    .filter(Objects::nonNull)

                    .collect(Collectors.toSet());

            Set<Long> newIds = CollectionUtils.isEmpty(bo.getCategoryList()) ? Collections.emptySet()

                    : bo.getCategoryList().stream()

                            .map(CouponCategoryBo::getCategoryId)

                            .filter(Objects::nonNull)

                            .collect(Collectors.toSet());

            return !oldIds.equals(newIds);

        }

        return false;

    }

    private void saveRelations(Long couponId, CouponSaveBo bo) {

        if (couponId == null || bo.getUseType() == null) {

            return;

        }

        if (bo.getUseType() == USE_TYPE_SPU) {

            saveSpuList(couponId, bo.getSpuList());

        } else if (bo.getUseType() == USE_TYPE_CATEGORY) {

            saveCategoryList(couponId, bo.getCategoryList());

        }

    }

    private void saveSpuList(Long couponId, List<CouponSpuBo> spuList) {

        if (CollectionUtils.isEmpty(spuList)) {

            return;

        }

        for (CouponSpuBo spuBo : spuList) {

            if (spuBo.getSpuId() == null) {

                continue;

            }

            SmsCouponSpuRelation relation = new SmsCouponSpuRelation();

            relation.setCouponId(couponId);

            relation.setSpuId(spuBo.getSpuId());

            if (StringUtils.hasText(spuBo.getSpuName())) {

                relation.setSpuName(spuBo.getSpuName());

            } else {

                PmsSpuInfo spu = pmsSpuInfoMapper.selectById(spuBo.getSpuId());

                if (spu != null) {

                    relation.setSpuName(spu.getSpuName());

                }

            }

            smsCouponSpuRelationMapper.insert(relation);

        }

    }

    private void saveCategoryList(Long couponId, List<CouponCategoryBo> categoryList) {

        if (CollectionUtils.isEmpty(categoryList)) {

            return;

        }

        for (CouponCategoryBo categoryBo : categoryList) {

            if (categoryBo.getCategoryId() == null) {

                continue;

            }

            SmsCouponSpuCategoryRelation relation = new SmsCouponSpuCategoryRelation();

            relation.setCouponId(couponId);

            relation.setCategoryId(categoryBo.getCategoryId());

            if (StringUtils.hasText(categoryBo.getCategoryName())) {

                relation.setCategoryName(categoryBo.getCategoryName());

            } else {

                PmsCategory category = pmsCategoryMapper.selectById(categoryBo.getCategoryId());

                if (category != null) {

                    relation.setCategoryName(category.getName());

                }

            }

            smsCouponSpuCategoryRelationMapper.insert(relation);

        }

    }

    private void removeRelations(Long couponId) {

        smsCouponSpuRelationMapper.delete(

                Wrappers.<SmsCouponSpuRelation>lambdaQuery().eq(SmsCouponSpuRelation::getCouponId, couponId));

        smsCouponSpuCategoryRelationMapper.delete(Wrappers.<SmsCouponSpuCategoryRelation>lambdaQuery()

                .eq(SmsCouponSpuCategoryRelation::getCouponId, couponId));

    }

    private List<CouponSpuVo> loadSpuList(Long couponId) {

        List<SmsCouponSpuRelation> relations = smsCouponSpuRelationMapper.selectList(

                Wrappers.<SmsCouponSpuRelation>lambdaQuery().eq(SmsCouponSpuRelation::getCouponId, couponId));

        if (CollectionUtils.isEmpty(relations)) {

            return Collections.emptyList();

        }

        return relations.stream().map(relation -> {

            CouponSpuVo vo = new CouponSpuVo();

            BeanUtils.copyProperties(relation, vo);

            return vo;

        }).collect(Collectors.toList());

    }

    private List<CouponCategoryVo> loadCategoryList(Long couponId) {

        List<SmsCouponSpuCategoryRelation> relations = smsCouponSpuCategoryRelationMapper.selectList(

                Wrappers.<SmsCouponSpuCategoryRelation>lambdaQuery()

                        .eq(SmsCouponSpuCategoryRelation::getCouponId, couponId));

        if (CollectionUtils.isEmpty(relations)) {

            return Collections.emptyList();

        }

        return relations.stream().map(relation -> {

            CouponCategoryVo vo = new CouponCategoryVo();

            BeanUtils.copyProperties(relation, vo);

            return vo;

        }).collect(Collectors.toList());

    }

    private CouponVo toVo(SmsCoupon entity) {

        CouponVo vo = new CouponVo();

        BeanUtils.copyProperties(entity, vo);

        return vo;

    }

}

