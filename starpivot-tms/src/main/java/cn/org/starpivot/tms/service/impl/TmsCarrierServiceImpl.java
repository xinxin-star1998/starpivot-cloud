package cn.org.starpivot.tms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.tms.constant.TmsConstants;
import cn.org.starpivot.tms.domain.dto.TmsCarrierQueryDto;
import cn.org.starpivot.tms.domain.dto.TmsCarrierSaveDto;
import cn.org.starpivot.tms.domain.entity.TmsCarrier;
import cn.org.starpivot.tms.domain.vo.TmsCarrierVo;
import cn.org.starpivot.tms.mapper.TmsCarrierMapper;
import cn.org.starpivot.tms.service.TmsCarrierService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TmsCarrierServiceImpl implements TmsCarrierService {

    private final TmsCarrierMapper carrierMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TmsCarrierVo> pageList(TmsCarrierQueryDto query) {
        Page<TmsCarrier> page = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<TmsCarrier> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getCarrierCode()), TmsCarrier::getCarrierCode, query.getCarrierCode())
                .like(StringUtils.hasText(query.getCarrierName()), TmsCarrier::getCarrierName, query.getCarrierName())
                .eq(StringUtils.hasText(query.getStatus()), TmsCarrier::getStatus, query.getStatus())
                .orderByAsc(TmsCarrier::getSortOrder)
                .orderByAsc(TmsCarrier::getId);
        Page<TmsCarrier> result = carrierMapper.selectPage(page, wrapper);
        PageResponse<TmsCarrierVo> response = new PageResponse<>();
        response.setTotal(result.getTotal());
        response.setRows(result.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TmsCarrierVo> listEnabled() {
        return carrierMapper.selectList(new LambdaQueryWrapper<TmsCarrier>()
                        .eq(TmsCarrier::getStatus, TmsConstants.CARRIER_STATUS_NORMAL)
                        .orderByAsc(TmsCarrier::getSortOrder)
                        .orderByAsc(TmsCarrier::getId))
                .stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(TmsCarrierSaveDto dto) {
        LocalDateTime now = LocalDateTime.now();
        TmsCarrier entity;
        if (dto.getId() != null) {
            entity = carrierMapper.selectById(dto.getId());
            if (entity == null) {
                throw new BizException("承运商不存在");
            }
        } else {
            entity = new TmsCarrier();
            entity.setCreateTime(now);
        }
        ensureCarrierCodeUnique(dto.getCarrierCode(), dto.getId());
        entity.setCarrierCode(dto.getCarrierCode().trim());
        entity.setCarrierName(dto.getCarrierName().trim());
        entity.setKuaidi100Com(dto.getKuaidi100Com().trim());
        entity.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        entity.setStatus(StringUtils.hasText(dto.getStatus()) ? dto.getStatus() : TmsConstants.CARRIER_STATUS_NORMAL);
        entity.setRemark(dto.getRemark());
        entity.setUpdateTime(now);
        if (dto.getId() != null) {
            carrierMapper.updateById(entity);
        } else {
            carrierMapper.insert(entity);
        }
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long id) {
        if (carrierMapper.selectById(id) == null) {
            throw new BizException("承运商不存在");
        }
        carrierMapper.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TmsCarrierVo requireEnabledCarrier(Long carrierId) {
        TmsCarrier carrier = carrierMapper.selectById(carrierId);
        if (carrier == null) {
            throw new BizException("承运商不存在");
        }
        if (!TmsConstants.CARRIER_STATUS_NORMAL.equals(carrier.getStatus())) {
            throw new BizException("承运商已停用");
        }
        return toVo(carrier);
    }

    private void ensureCarrierCodeUnique(String carrierCode, Long excludeId) {
        LambdaQueryWrapper<TmsCarrier> wrapper = new LambdaQueryWrapper<TmsCarrier>()
                .eq(TmsCarrier::getCarrierCode, carrierCode.trim());
        if (excludeId != null) {
            wrapper.ne(TmsCarrier::getId, excludeId);
        }
        if (carrierMapper.selectCount(wrapper) > 0) {
            throw new BizException("承运商编码已存在");
        }
    }

    private TmsCarrierVo toVo(TmsCarrier entity) {
        TmsCarrierVo vo = new TmsCarrierVo();
        vo.setId(entity.getId());
        vo.setCarrierCode(entity.getCarrierCode());
        vo.setCarrierName(entity.getCarrierName());
        vo.setKuaidi100Com(entity.getKuaidi100Com());
        vo.setSortOrder(entity.getSortOrder());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }
}
