package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.api.product.dto.SkuDto;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.sms.domain.bo.BargainActivityReqBo;
import cn.org.starpivot.mall.sms.domain.bo.BargainActivitySaveBo;
import cn.org.starpivot.mall.sms.domain.vo.BargainActivityVo;
import cn.org.starpivot.mall.sms.entity.SmsBargainActivity;
import cn.org.starpivot.mall.sms.mapper.SmsBargainActivityMapper;
import cn.org.starpivot.mall.sms.service.SmsBargainActivityService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 砍价活动后台管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class SmsBargainActivityServiceImpl implements SmsBargainActivityService {

    private final SmsBargainActivityMapper smsBargainActivityMapper;
    private final ProductFeignSupport productFeignSupport;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BargainActivityVo> pageList(BargainActivityReqBo reqBo) {
        Page<SmsBargainActivity> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<SmsBargainActivity> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(reqBo.getTitle())) {
            wrapper.like(SmsBargainActivity::getTitle, reqBo.getTitle());
        }
        if (reqBo.getStatus() != null) {
            wrapper.eq(SmsBargainActivity::getStatus, reqBo.getStatus());
        }
        if (reqBo.getSkuId() != null) {
            wrapper.eq(SmsBargainActivity::getSkuId, reqBo.getSkuId());
        }
        wrapper.orderByAsc(SmsBargainActivity::getSort).orderByDesc(SmsBargainActivity::getId);
        IPage<SmsBargainActivity> pageList = smsBargainActivityMapper.selectPage(page, wrapper);

        List<Long> skuIds = pageList.getRecords().stream().map(SmsBargainActivity::getSkuId).distinct().toList();
        Map<Long, SkuDto> skuMap = CollectionUtils.isEmpty(skuIds)
                ? Map.of() : productFeignSupport.requireSkuMap(skuIds);

        PageResponse<BargainActivityVo> response = new PageResponse<>();
        response.setTotal(pageList.getTotal());
        response.setRows(pageList.getRecords().stream()
                .map(item -> toVo(item, skuMap.get(item.getSkuId()))).toList());
        response.setPageNum(pageList.getCurrent());
        response.setPageSize(pageList.getSize());
        response.setPageCount(pageList.getPages());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public BargainActivityVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "活动ID不能为空");
        }
        SmsBargainActivity entity = smsBargainActivityMapper.selectById(id);
        if (entity == null) {
            throw new BizException("砍价活动不存在");
        }
        SkuDto sku = entity.getSkuId() == null ? null : productFeignSupport.requireSku(entity.getSkuId());
        return toVo(entity, sku);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(BargainActivitySaveBo bo) {
        validate(bo);
        SmsBargainActivity entity = new SmsBargainActivity();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        entity.setStatus(bo.getStatus() == null ? 0 : bo.getStatus());
        entity.setValidHours(bo.getValidHours() == null ? 24 : bo.getValidHours());
        entity.setLimitBuy(bo.getLimitBuy() == null ? 1 : bo.getLimitBuy());
        entity.setSort(bo.getSort() == null ? 0 : bo.getSort());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        smsBargainActivityMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(BargainActivitySaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改活动时 id 不能为空");
        }
        validate(bo);
        SmsBargainActivity existing = smsBargainActivityMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("砍价活动不存在");
        }
        SmsBargainActivity entity = new SmsBargainActivity();
        BeanUtils.copyProperties(bo, entity);
        entity.setUpdateTime(LocalDateTime.now());
        smsBargainActivityMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsBargainActivityMapper.deleteBatchIds(ids);
    }

    private void validate(BargainActivitySaveBo bo) {
        if (bo.getStartTime() != null && bo.getEndTime() != null
                && bo.getStartTime().isAfter(bo.getEndTime())) {
            throw new BizException("活动开始时间不能晚于结束时间");
        }
        if (bo.getFloorPrice() != null && bo.getOriginalPrice() != null
                && bo.getFloorPrice().compareTo(bo.getOriginalPrice()) >= 0) {
            throw new BizException("砍价底价必须低于商品原价");
        }
        if (bo.getMinAmount() != null && bo.getMaxAmount() != null
                && bo.getMinAmount().compareTo(bo.getMaxAmount()) > 0) {
            throw new BizException("单次最低砍价金额不能高于最高砍价金额");
        }
        productFeignSupport.requireSku(bo.getSkuId());
    }

    private BargainActivityVo toVo(SmsBargainActivity entity, SkuDto sku) {
        BargainActivityVo vo = new BargainActivityVo();
        BeanUtils.copyProperties(entity, vo);
        if (sku != null) {
            vo.setSkuName(sku.getSkuName());
            vo.setSkuImg(sku.getSkuDefaultImg());
        }
        return vo;
    }
}
