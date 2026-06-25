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

import cn.org.starpivot.mall.sms.entity.SmsCouponSpuCategoryRelation;

import cn.org.starpivot.mall.sms.entity.SmsCouponSpuRelation;

import cn.org.starpivot.mall.sms.mapper.SmsCouponMapper;

import cn.org.starpivot.mall.sms.mapper.SmsCouponSpuCategoryRelationMapper;

import cn.org.starpivot.mall.sms.mapper.SmsCouponSpuRelationMapper;

import cn.org.starpivot.mall.sms.service.SmsCouponService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.Collections;

import java.util.List;

import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.BeanUtils;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.util.CollectionUtils;

import org.springframework.util.StringUtils;

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

    private static final int USE_TYPE_CATEGORY = 1;

    private static final int USE_TYPE_SPU = 2;

    private final SmsCouponMapper smsCouponMapper;

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

        SmsCoupon entity = new SmsCoupon();

        BeanUtils.copyProperties(bo, entity);

        entity.setId(null);

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

        SmsCoupon entity = new SmsCoupon();

        BeanUtils.copyProperties(bo, entity);

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

            removeRelations(id);

        }

        smsCouponMapper.delete(Wrappers.<SmsCoupon>lambdaQuery().in(SmsCoupon::getId, ids));

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

