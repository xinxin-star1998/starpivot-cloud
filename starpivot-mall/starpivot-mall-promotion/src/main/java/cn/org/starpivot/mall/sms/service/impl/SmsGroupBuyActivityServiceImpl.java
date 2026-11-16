package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.api.product.dto.SkuDto;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.sms.domain.bo.GroupBuyActivityReqBo;
import cn.org.starpivot.mall.sms.domain.bo.GroupBuyActivitySaveBo;
import cn.org.starpivot.mall.sms.domain.vo.GroupBuyActivityVo;
import cn.org.starpivot.mall.sms.entity.SmsGroupBuyActivity;
import cn.org.starpivot.mall.sms.mapper.SmsGroupBuyActivityMapper;
import cn.org.starpivot.mall.sms.service.SmsGroupBuyActivityService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 拼团活动后台管理服务实现。
 */
@Service
@RequiredArgsConstructor
public class SmsGroupBuyActivityServiceImpl implements SmsGroupBuyActivityService {

    private final SmsGroupBuyActivityMapper smsGroupBuyActivityMapper;
    private final ProductFeignSupport productFeignSupport;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GroupBuyActivityVo> pageList(GroupBuyActivityReqBo reqBo) {
        Page<SmsGroupBuyActivity> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<SmsGroupBuyActivity> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(reqBo.getTitle())) {
            wrapper.like(SmsGroupBuyActivity::getTitle, reqBo.getTitle());
        }
        if (reqBo.getStatus() != null) {
            wrapper.eq(SmsGroupBuyActivity::getStatus, reqBo.getStatus());
        }
        if (reqBo.getSkuId() != null) {
            wrapper.eq(SmsGroupBuyActivity::getSkuId, reqBo.getSkuId());
        }
        wrapper.orderByAsc(SmsGroupBuyActivity::getSort).orderByDesc(SmsGroupBuyActivity::getId);
        IPage<SmsGroupBuyActivity> pageList = smsGroupBuyActivityMapper.selectPage(page, wrapper);

        List<Long> skuIds = pageList.getRecords().stream().map(SmsGroupBuyActivity::getSkuId).distinct().toList();
        Map<Long, SkuDto> skuMap = CollectionUtils.isEmpty(skuIds)
                ? Map.of() : productFeignSupport.requireSkuMap(skuIds);

        PageResponse<GroupBuyActivityVo> response = new PageResponse<>();
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
    public GroupBuyActivityVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "活动ID不能为空");
        }
        SmsGroupBuyActivity entity = smsGroupBuyActivityMapper.selectById(id);
        if (entity == null) {
            throw new BizException("拼团活动不存在");
        }
        SkuDto sku = entity.getSkuId() == null ? null : productFeignSupport.requireSku(entity.getSkuId());
        return toVo(entity, sku);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(GroupBuyActivitySaveBo bo) {
        validate(bo);
        SmsGroupBuyActivity entity = new SmsGroupBuyActivity();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        entity.setStatus(bo.getStatus() == null ? 0 : bo.getStatus());
        entity.setValidHours(bo.getValidHours() == null ? 24 : bo.getValidHours());
        entity.setLimitBuy(bo.getLimitBuy() == null ? 1 : bo.getLimitBuy());
        entity.setSort(bo.getSort() == null ? 0 : bo.getSort());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        smsGroupBuyActivityMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(GroupBuyActivitySaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改活动时 id 不能为空");
        }
        validate(bo);
        SmsGroupBuyActivity existing = smsGroupBuyActivityMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("拼团活动不存在");
        }
        SmsGroupBuyActivity entity = new SmsGroupBuyActivity();
        BeanUtils.copyProperties(bo, entity);
        entity.setUpdateTime(LocalDateTime.now());
        smsGroupBuyActivityMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsGroupBuyActivityMapper.deleteBatchIds(ids);
    }

    private void validate(GroupBuyActivitySaveBo bo) {
        if (bo.getStartTime() != null && bo.getEndTime() != null
                && bo.getStartTime().isAfter(bo.getEndTime())) {
            throw new BizException("活动开始时间不能晚于结束时间");
        }
        SkuDto sku = productFeignSupport.requireSku(bo.getSkuId());
        if (sku.getPrice() != null && bo.getGroupPrice() != null
                && bo.getGroupPrice().compareTo(sku.getPrice()) > 0) {
            throw new BizException("拼团价不能高于商品原价：" + sku.getPrice());
        }
        if (bo.getGroupPrice() != null
                && bo.getGroupPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("拼团价必须大于0");
        }
    }

    private GroupBuyActivityVo toVo(SmsGroupBuyActivity entity, SkuDto sku) {
        GroupBuyActivityVo vo = new GroupBuyActivityVo();
        BeanUtils.copyProperties(entity, vo);
        if (sku != null) {
            vo.setSkuName(sku.getSkuName());
            vo.setSkuImg(sku.getSkuDefaultImg());
            vo.setOriginalPrice(sku.getPrice());
        }
        return vo;
    }
}
