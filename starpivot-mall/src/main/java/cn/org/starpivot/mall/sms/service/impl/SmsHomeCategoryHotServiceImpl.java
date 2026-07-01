package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.entity.PmsCategory;
import cn.org.starpivot.mall.pms.mapper.PmsCategoryMapper;
import cn.org.starpivot.mall.portal.PortalConstants;
import cn.org.starpivot.mall.sms.domain.bo.HomeCategoryHotReqBo;
import cn.org.starpivot.mall.sms.domain.bo.HomeCategoryHotSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.HomeCategoryHotVo;
import cn.org.starpivot.mall.sms.entity.SmsHomeCategoryHot;
import cn.org.starpivot.mall.sms.mapper.SmsHomeCategoryHotMapper;
import cn.org.starpivot.mall.sms.service.SmsHomeCategoryHotService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SmsHomeCategoryHotServiceImpl implements SmsHomeCategoryHotService {

    private final SmsHomeCategoryHotMapper smsHomeCategoryHotMapper;
    private final PmsCategoryMapper pmsCategoryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HomeCategoryHotVo> pageList(HomeCategoryHotReqBo reqBo) {
        Page<SmsHomeCategoryHot> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<SmsHomeCategoryHot> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(reqBo.getTitle())) {
            wrapper.like(SmsHomeCategoryHot::getTitle, reqBo.getTitle());
        }
        if (reqBo.getStatus() != null) {
            wrapper.eq(SmsHomeCategoryHot::getStatus, reqBo.getStatus());
        }
        if (reqBo.getCatId() != null) {
            wrapper.eq(SmsHomeCategoryHot::getCatId, reqBo.getCatId());
        }
        wrapper.orderByAsc(SmsHomeCategoryHot::getSort).orderByDesc(SmsHomeCategoryHot::getId);
        IPage<SmsHomeCategoryHot> pageList = smsHomeCategoryHotMapper.selectPage(page, wrapper);

        Map<Long, String> catNameMap = loadCategoryNames(
                pageList.getRecords().stream().map(SmsHomeCategoryHot::getCatId).collect(Collectors.toSet()));

        PageResponse<HomeCategoryHotVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords().stream()
                .map(entity -> toVo(entity, catNameMap))
                .collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public HomeCategoryHotVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "分类热门ID不能为空");
        }
        SmsHomeCategoryHot entity = smsHomeCategoryHotMapper.selectById(id);
        if (entity == null) {
            throw new BizException("分类热门配置不存在");
        }
        Map<Long, String> catNameMap = loadCategoryNames(Set.of(entity.getCatId()));
        return toVo(entity, catNameMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(HomeCategoryHotSaveBo bo) {
        validateCategory(bo.getCatId());
        SmsHomeCategoryHot entity = new SmsHomeCategoryHot();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(PortalConstants.ADV_STATUS_ON);
        }
        smsHomeCategoryHotMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(HomeCategoryHotSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改分类热门时 id 不能为空");
        }
        SmsHomeCategoryHot existing = smsHomeCategoryHotMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("分类热门配置不存在");
        }
        validateCategory(bo.getCatId());
        SmsHomeCategoryHot entity = new SmsHomeCategoryHot();
        BeanUtils.copyProperties(bo, entity);
        smsHomeCategoryHotMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsHomeCategoryHotMapper.delete(Wrappers.<SmsHomeCategoryHot>lambdaQuery().in(SmsHomeCategoryHot::getId, ids));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HomeCategoryHotVo> listActive() {
        List<SmsHomeCategoryHot> list = smsHomeCategoryHotMapper.selectList(
                Wrappers.<SmsHomeCategoryHot>lambdaQuery()
                        .eq(SmsHomeCategoryHot::getStatus, PortalConstants.ADV_STATUS_ON)
                        .orderByAsc(SmsHomeCategoryHot::getSort)
                        .orderByDesc(SmsHomeCategoryHot::getId));
        if (CollectionUtils.isEmpty(list)) {
            return List.of();
        }
        Map<Long, String> catNameMap = loadCategoryNames(
                list.stream().map(SmsHomeCategoryHot::getCatId).collect(Collectors.toSet()));
        Map<Long, PmsCategory> categoryMap = loadCategories(
                list.stream().map(SmsHomeCategoryHot::getCatId).collect(Collectors.toSet()));
        return list.stream()
                .filter(entity -> isCategoryVisible(categoryMap.get(entity.getCatId())))
                .map(entity -> enrichPortalVo(entity, catNameMap, categoryMap))
                .collect(Collectors.toList());
    }

    private boolean isCategoryVisible(PmsCategory category) {
        if (category == null) {
            return false;
        }
        Long showStatus = category.getShowStatus();
        return showStatus == null || showStatus == 1L;
    }

    private void validateCategory(Long catId) {
        if (catId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "关联分类不能为空");
        }
        PmsCategory category = pmsCategoryMapper.selectById(catId);
        if (category == null) {
            throw new BizException("关联分类不存在");
        }
    }

    private Map<Long, String> loadCategoryNames(Set<Long> catIds) {
        if (CollectionUtils.isEmpty(catIds)) {
            return Collections.emptyMap();
        }
        return pmsCategoryMapper.selectBatchIds(catIds).stream()
                .filter(cat -> cat.getCatId() != null)
                .collect(Collectors.toMap(PmsCategory::getCatId, PmsCategory::getName, (a, b) -> a));
    }

    private Map<Long, PmsCategory> loadCategories(Set<Long> catIds) {
        if (CollectionUtils.isEmpty(catIds)) {
            return Collections.emptyMap();
        }
        return pmsCategoryMapper.selectBatchIds(catIds).stream()
                .filter(cat -> cat.getCatId() != null)
                .collect(Collectors.toMap(PmsCategory::getCatId, cat -> cat, (a, b) -> a));
    }

    private HomeCategoryHotVo toVo(SmsHomeCategoryHot entity, Map<Long, String> catNameMap) {
        HomeCategoryHotVo vo = new HomeCategoryHotVo();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getCatId() != null) {
            vo.setCatName(catNameMap.get(entity.getCatId()));
        }
        return vo;
    }

    private HomeCategoryHotVo enrichPortalVo(
            SmsHomeCategoryHot entity, Map<Long, String> catNameMap, Map<Long, PmsCategory> categoryMap) {
        HomeCategoryHotVo vo = toVo(entity, catNameMap);
        PmsCategory category = entity.getCatId() != null ? categoryMap.get(entity.getCatId()) : null;
        if (!StringUtils.hasText(vo.getTitle()) && category != null) {
            vo.setTitle(category.getName());
        }
        if (!StringUtils.hasText(vo.getIcon()) && category != null) {
            vo.setIcon(category.getIcon());
        }
        if (!StringUtils.hasText(vo.getUrl()) && entity.getCatId() != null) {
            vo.setUrl("/portal/search?catalogId=" + entity.getCatId());
        }
        return vo;
    }
}
