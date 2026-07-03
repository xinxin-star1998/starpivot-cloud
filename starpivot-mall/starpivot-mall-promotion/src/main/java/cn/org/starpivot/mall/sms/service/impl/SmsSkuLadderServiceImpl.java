package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.common.ProductFeignSupport;
import cn.org.starpivot.mall.sms.domain.bo.SkuLadderReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SkuLadderSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SkuLadderVo;
import cn.org.starpivot.mall.sms.entity.SmsSkuLadder;
import cn.org.starpivot.mall.sms.mapper.SmsSkuLadderMapper;
import cn.org.starpivot.mall.sms.service.SmsSkuLadderService;
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
 * SKU 阶梯价服务实现类。
 * <p>
 * 实现 {@link SmsSkuLadderService}，处理SKU 阶梯价相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see SmsSkuLadderService
 */

@Service
@RequiredArgsConstructor
public class SmsSkuLadderServiceImpl implements SmsSkuLadderService {

    private final SmsSkuLadderMapper smsSkuLadderMapper;
    private final ProductFeignSupport productFeignSupport;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SkuLadderVo> pageList(SkuLadderReqBo reqBo) {
        Page<SmsSkuLadder> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<SmsSkuLadder> wrapper = Wrappers.lambdaQuery();
        if (reqBo.getSkuId() != null) {
            wrapper.eq(SmsSkuLadder::getSkuId, reqBo.getSkuId());
        }
        wrapper.orderByDesc(SmsSkuLadder::getId);
        IPage<SmsSkuLadder> pageList = smsSkuLadderMapper.selectPage(page, wrapper);

        List<SmsSkuLadder> records = pageList.getRecords();
        Map<Long, String> skuNameMap = loadSkuNameMap(records);

        PageResponse<SkuLadderVo> pageResponse = new PageResponse<>();
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
    public SkuLadderVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "阶梯价ID不能为空");
        }
        SmsSkuLadder entity = smsSkuLadderMapper.selectById(id);
        if (entity == null) {
            throw new BizException("阶梯价规则不存在");
        }
        Map<Long, String> skuNameMap = loadSkuNameMap(List.of(entity));
        return toVo(entity, skuNameMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SkuLadderSaveBo bo) {
        validateSkuExists(bo.getSkuId());
        SmsSkuLadder entity = new SmsSkuLadder();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        if (entity.getAddOther() == null) {
            entity.setAddOther(0);
        }
        smsSkuLadderMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SkuLadderSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改阶梯价时 id 不能为空");
        }
        SmsSkuLadder existing = smsSkuLadderMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("阶梯价规则不存在");
        }
        validateSkuExists(bo.getSkuId());
        SmsSkuLadder entity = new SmsSkuLadder();
        BeanUtils.copyProperties(bo, entity);
        smsSkuLadderMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsSkuLadderMapper.delete(Wrappers.<SmsSkuLadder>lambdaQuery().in(SmsSkuLadder::getId, ids));
    }

    private void validateSkuExists(Long skuId) {
        if (skuId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SKU ID不能为空");
        }
        productFeignSupport.requireSku(skuId);
    }

    private Map<Long, String> loadSkuNameMap(List<SmsSkuLadder> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> skuIds = records.stream()
                .map(SmsSkuLadder::getSkuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        return productFeignSupport.requireSkuMap(skuIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getSkuName(), (a, b) -> a));
    }

    private SkuLadderVo toVo(SmsSkuLadder entity, Map<Long, String> skuNameMap) {
        SkuLadderVo vo = new SkuLadderVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setSkuName(skuNameMap.get(entity.getSkuId()));
        return vo;
    }
}
