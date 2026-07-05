package cn.org.starpivot.tms.service.impl;

import cn.org.starpivot.api.product.ProductInternalClient;
import cn.org.starpivot.api.product.dto.SkuDto;
import cn.org.starpivot.api.product.dto.SpuDto;
import cn.org.starpivot.api.tms.dto.FreightCalculateRequest;
import cn.org.starpivot.api.tms.dto.FreightCalculateResult;
import cn.org.starpivot.common.domain.Result;
import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.tms.config.TmsProperties;
import cn.org.starpivot.tms.constant.TmsConstants;
import cn.org.starpivot.tms.domain.dto.TmsFreightRuleQueryDto;
import cn.org.starpivot.tms.domain.dto.TmsFreightRuleSaveDto;
import cn.org.starpivot.tms.domain.entity.TmsFreightRule;
import cn.org.starpivot.tms.domain.vo.TmsFreightRuleVo;
import cn.org.starpivot.tms.mapper.TmsFreightRuleMapper;
import cn.org.starpivot.tms.service.TmsFreightRuleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TmsFreightRuleServiceImpl implements TmsFreightRuleService {

    private static final int MONEY_SCALE = 2;

    private final TmsFreightRuleMapper freightRuleMapper;
    private final ProductInternalClient productInternalClient;
    private final TmsProperties tmsProperties;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TmsFreightRuleVo> pageList(TmsFreightRuleQueryDto query) {
        Page<TmsFreightRule> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<TmsFreightRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getRuleName()), TmsFreightRule::getRuleName, query.getRuleName())
                .eq(StringUtils.hasText(query.getRuleType()), TmsFreightRule::getRuleType, query.getRuleType())
                .eq(StringUtils.hasText(query.getStatus()), TmsFreightRule::getStatus, query.getStatus())
                .orderByAsc(TmsFreightRule::getSortOrder)
                .orderByAsc(TmsFreightRule::getId);
        Page<TmsFreightRule> result = freightRuleMapper.selectPage(page, wrapper);
        PageResponse<TmsFreightRuleVo> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setRows(result.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(TmsFreightRuleSaveDto dto) {
        validateRule(dto);
        LocalDateTime now = LocalDateTime.now();
        TmsFreightRule entity;
        if (dto.getId() != null) {
            entity = freightRuleMapper.selectById(dto.getId());
            if (entity == null) {
                throw new BizException("运费规则不存在");
            }
        } else {
            entity = new TmsFreightRule();
            entity.setCreateTime(now);
        }
        if (TmsConstants.DEFAULT_FLAG_YES.equals(dto.getDefaultFlag())) {
            clearDefaultFlag();
        }
        entity.setRuleName(dto.getRuleName().trim());
        entity.setRuleType(dto.getRuleType().trim());
        entity.setDefaultFlag(
                TmsConstants.DEFAULT_FLAG_YES.equals(dto.getDefaultFlag())
                        ? TmsConstants.DEFAULT_FLAG_YES
                        : TmsConstants.DEFAULT_FLAG_NO);
        entity.setBaseFee(dto.getBaseFee());
        entity.setFirstWeightKg(dto.getFirstWeightKg());
        entity.setFirstFee(dto.getFirstFee());
        entity.setContinueFeePerKg(dto.getContinueFeePerKg());
        entity.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : TmsConstants.CARRIER_STATUS_NORMAL);
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setRemark(dto.getRemark());
        entity.setUpdateTime(now);
        if (dto.getId() != null) {
            freightRuleMapper.updateById(entity);
        } else {
            freightRuleMapper.insert(entity);
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        TmsFreightRule rule = freightRuleMapper.selectById(id);
        if (rule == null) {
            throw new BizException("运费规则不存在");
        }
        if (TmsConstants.DEFAULT_FLAG_YES.equals(rule.getDefaultFlag())) {
            throw new BizException("默认规则不可删除，请先指定其他默认规则");
        }
        freightRuleMapper.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public FreightCalculateResult calculate(FreightCalculateRequest request) {
        TmsFreightRule rule = resolveActiveRule();
        if (rule == null) {
            throw new BizException("未配置可用运费规则");
        }
        BigDecimal totalWeight = resolveTotalWeightKg(request);
        BigDecimal freight = applyRule(rule, totalWeight);

        FreightCalculateResult result = new FreightCalculateResult();
        result.setFreightAmount(freight);
        result.setRuleId(rule.getId());
        result.setRuleName(rule.getRuleName());
        result.setRuleType(rule.getRuleType());
        result.setTotalWeightKg(totalWeight);
        return result;
    }

    private TmsFreightRule resolveActiveRule() {
        TmsFreightRule defaultRule = freightRuleMapper.selectOne(new LambdaQueryWrapper<TmsFreightRule>()
                .eq(TmsFreightRule::getDefaultFlag, TmsConstants.DEFAULT_FLAG_YES)
                .eq(TmsFreightRule::getStatus, TmsConstants.CARRIER_STATUS_NORMAL)
                .last("LIMIT 1"));
        if (defaultRule != null) {
            return defaultRule;
        }
        return freightRuleMapper.selectOne(new LambdaQueryWrapper<TmsFreightRule>()
                .eq(TmsFreightRule::getStatus, TmsConstants.CARRIER_STATUS_NORMAL)
                .orderByAsc(TmsFreightRule::getSortOrder)
                .last("LIMIT 1"));
    }

    private BigDecimal resolveTotalWeightKg(FreightCalculateRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getLines())) {
            return BigDecimal.ZERO;
        }
        List<Long> skuIds = request.getLines().stream()
                .map(FreightCalculateRequest.FreightLine::getSkuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, SkuDto> skuMap = loadSkuMap(skuIds);
        List<Long> spuIds = skuMap.values().stream()
                .map(SkuDto::getSpuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, SpuDto> spuMap = loadSpuMap(spuIds);

        BigDecimal total = BigDecimal.ZERO;
        BigDecimal defaultWeight = tmsProperties.getDefaultItemWeightKg();
        for (FreightCalculateRequest.FreightLine line : request.getLines()) {
            if (line.getSkuId() == null) {
                continue;
            }
            int qty = line.getQuantity() == null || line.getQuantity() <= 0 ? 1 : line.getQuantity();
            SkuDto sku = skuMap.get(line.getSkuId());
            BigDecimal unitWeight = defaultWeight;
            if (sku != null && sku.getSpuId() != null) {
                SpuDto spu = spuMap.get(sku.getSpuId());
                if (spu != null && spu.getWeight() != null && spu.getWeight().compareTo(BigDecimal.ZERO) > 0) {
                    unitWeight = spu.getWeight();
                }
            }
            total = total.add(unitWeight.multiply(BigDecimal.valueOf(qty)));
        }
        return total.setScale(3, RoundingMode.HALF_UP);
    }

    private BigDecimal applyRule(TmsFreightRule rule, BigDecimal totalWeightKg) {
        if (TmsConstants.RULE_TYPE_WEIGHT.equals(rule.getRuleType())) {
            BigDecimal firstWeight = rule.getFirstWeightKg() != null ? rule.getFirstWeightKg() : BigDecimal.ONE;
            BigDecimal firstFee = rule.getFirstFee() != null ? rule.getFirstFee() : BigDecimal.ZERO;
            BigDecimal continueFee = rule.getContinueFeePerKg() != null ? rule.getContinueFeePerKg() : BigDecimal.ZERO;
            if (totalWeightKg.compareTo(firstWeight) <= 0) {
                return money(firstFee);
            }
            BigDecimal extra = totalWeightKg.subtract(firstWeight);
            return money(firstFee.add(extra.multiply(continueFee)));
        }
        BigDecimal base = rule.getBaseFee() != null ? rule.getBaseFee() : BigDecimal.ZERO;
        return money(base);
    }

    private Map<Long, SkuDto> loadSkuMap(List<Long> skuIds) {
        if (CollectionUtils.isEmpty(skuIds)) {
            return Map.of();
        }
        Result<List<SkuDto>> result = productInternalClient.listSkusByIds(skuIds);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return Map.of();
        }
        return result.getData().stream()
                .filter(s -> s.getSkuId() != null)
                .collect(Collectors.toMap(SkuDto::getSkuId, s -> s, (a, b) -> a));
    }

    private Map<Long, SpuDto> loadSpuMap(List<Long> spuIds) {
        if (CollectionUtils.isEmpty(spuIds)) {
            return Map.of();
        }
        Result<List<SpuDto>> result = productInternalClient.listSpusByIds(spuIds);
        if (result == null || !result.isSuccess() || result.getData() == null) {
            return Map.of();
        }
        return result.getData().stream()
                .filter(s -> s.getId() != null)
                .collect(Collectors.toMap(SpuDto::getId, s -> s, (a, b) -> a));
    }

    private void validateRule(TmsFreightRuleSaveDto dto) {
        if (TmsConstants.RULE_TYPE_FIXED.equals(dto.getRuleType())) {
            if (dto.getBaseFee() == null) {
                throw new BizException("固定运费规则需填写 baseFee");
            }
        } else if (TmsConstants.RULE_TYPE_WEIGHT.equals(dto.getRuleType())) {
            if (dto.getFirstWeightKg() == null || dto.getFirstFee() == null || dto.getContinueFeePerKg() == null) {
                throw new BizException("按重量规则需填写首重、首费、续重单价");
            }
        } else {
            throw new BizException("不支持的规则类型");
        }
    }

    private void clearDefaultFlag() {
        TmsFreightRule patch = new TmsFreightRule();
        patch.setDefaultFlag(TmsConstants.DEFAULT_FLAG_NO);
        freightRuleMapper.update(patch, new LambdaQueryWrapper<TmsFreightRule>()
                .eq(TmsFreightRule::getDefaultFlag, TmsConstants.DEFAULT_FLAG_YES));
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    private TmsFreightRuleVo toVo(TmsFreightRule entity) {
        TmsFreightRuleVo vo = new TmsFreightRuleVo();
        vo.setId(entity.getId());
        vo.setRuleName(entity.getRuleName());
        vo.setRuleType(entity.getRuleType());
        vo.setDefaultFlag(entity.getDefaultFlag());
        vo.setBaseFee(entity.getBaseFee());
        vo.setFirstWeightKg(entity.getFirstWeightKg());
        vo.setFirstFee(entity.getFirstFee());
        vo.setContinueFeePerKg(entity.getContinueFeePerKg());
        vo.setStatus(entity.getStatus());
        vo.setSortOrder(entity.getSortOrder());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
