package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.sms.domain.bo.SkuFullReductionReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SkuFullReductionSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SkuFullReductionVo;
import cn.org.starpivot.mall.sms.entity.SmsSkuFullReduction;
import cn.org.starpivot.mall.sms.mapper.SmsSkuFullReductionMapper;
import cn.org.starpivot.mall.sms.service.SmsSkuFullReductionService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * SKU 满减服务实现类。
 * <p>
 * 实现 {@link SmsSkuFullReductionService}，处理SKU 满减相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see SmsSkuFullReductionService
 */

@Service
@RequiredArgsConstructor
public class SmsSkuFullReductionServiceImpl implements SmsSkuFullReductionService {

    private final SmsSkuFullReductionMapper smsSkuFullReductionMapper;
    private final ProductFeignSupport productFeignSupport;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SkuFullReductionVo> pageList(SkuFullReductionReqBo reqBo) {
        Page<SmsSkuFullReduction> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<SmsSkuFullReduction> wrapper = Wrappers.lambdaQuery();
        if (reqBo.getSkuId() != null) {
            wrapper.eq(SmsSkuFullReduction::getSkuId, reqBo.getSkuId());
        }
        wrapper.orderByDesc(SmsSkuFullReduction::getId);
        IPage<SmsSkuFullReduction> pageList = smsSkuFullReductionMapper.selectPage(page, wrapper);

        List<SmsSkuFullReduction> records = pageList.getRecords();
        Map<Long, String> skuNameMap = loadSkuNameMap(records);

        PageResponse<SkuFullReductionVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(records.stream()
                .map(entity -> toVo(entity, skuNameMap))
                .collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public SkuFullReductionVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "满减ID不能为空");
        }
        SmsSkuFullReduction entity = smsSkuFullReductionMapper.selectById(id);
        if (entity == null) {
            throw new BizException("满减规则不存在");
        }
        Map<Long, String> skuNameMap = loadSkuNameMap(List.of(entity));
        return toVo(entity, skuNameMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SkuFullReductionSaveBo bo) {
        validateSkuExists(bo.getSkuId());
        SmsSkuFullReduction entity = new SmsSkuFullReduction();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        if (entity.getAddOther() == null) {
            entity.setAddOther(0);
        }
        smsSkuFullReductionMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SkuFullReductionSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改满减时 id 不能为空");
        }
        SmsSkuFullReduction existing = smsSkuFullReductionMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("满减规则不存在");
        }
        validateSkuExists(bo.getSkuId());
        SmsSkuFullReduction entity = new SmsSkuFullReduction();
        BeanUtils.copyProperties(bo, entity);
        smsSkuFullReductionMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsSkuFullReductionMapper.delete(
                Wrappers.<SmsSkuFullReduction>lambdaQuery().in(SmsSkuFullReduction::getId, ids));
    }

    private void validateSkuExists(Long skuId) {
        if (skuId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SKU ID不能为空");
        }
        productFeignSupport.requireSku(skuId);
    }

    private Map<Long, String> loadSkuNameMap(List<SmsSkuFullReduction> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> skuIds = records.stream()
                .map(SmsSkuFullReduction::getSkuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        return productFeignSupport.requireSkuMap(skuIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getSkuName(), (a, b) -> a));
    }

    private SkuFullReductionVo toVo(SmsSkuFullReduction entity, Map<Long, String> skuNameMap) {
        SkuFullReductionVo vo = new SkuFullReductionVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setSkuName(skuNameMap.get(entity.getSkuId()));
        return vo;
    }
}
