package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.sms.domain.bo.SpuBoundsReqBo;
import cn.org.starpivot.mall.sms.domain.bo.SpuBoundsSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.SpuBoundsVo;
import cn.org.starpivot.mall.sms.entity.SmsSpuBounds;
import cn.org.starpivot.mall.sms.mapper.SmsSpuBoundsMapper;
import cn.org.starpivot.mall.sms.service.SmsSpuBoundsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

/**
 * SPU 积分/成长值服务实现类。
 * <p>
 * 实现 {@link SmsSpuBoundsService}，处理SPU 积分/成长值相关业务。
 * </p>
 * <ul>
 *   <li>{@link Service} — Spring 服务 Bean</li>
 *   <li>{@link RequiredArgsConstructor} — 构造器注入依赖</li>
 * </ul>
 *
 * @see SmsSpuBoundsService
 */

@Service
@RequiredArgsConstructor
public class SmsSpuBoundsServiceImpl implements SmsSpuBoundsService {

    private final SmsSpuBoundsMapper smsSpuBoundsMapper;
    private final PmsSpuInfoMapper pmsSpuInfoMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SpuBoundsVo> pageList(SpuBoundsReqBo reqBo) {
        Page<SmsSpuBounds> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<SmsSpuBounds> wrapper = Wrappers.lambdaQuery();
        if (reqBo.getSpuId() != null) {
            wrapper.eq(SmsSpuBounds::getSpuId, reqBo.getSpuId());
        }
        wrapper.orderByDesc(SmsSpuBounds::getId);
        IPage<SmsSpuBounds> pageList = smsSpuBoundsMapper.selectPage(page, wrapper);

        List<SmsSpuBounds> records = pageList.getRecords();
        Map<Long, String> spuNameMap = loadSpuNameMap(records);

        PageResponse<SpuBoundsVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(records.stream()
                .map(entity -> toVo(entity, spuNameMap))
                .collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public SpuBoundsVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "积分配置ID不能为空");
        }
        SmsSpuBounds entity = smsSpuBoundsMapper.selectById(id);
        if (entity == null) {
            throw new BizException("积分配置不存在");
        }
        Map<Long, String> spuNameMap = loadSpuNameMap(List.of(entity));
        return toVo(entity, spuNameMap);
    }

    @Override
    @Transactional(readOnly = true)
    public SpuBoundsVo getBySpuId(Long spuId) {
        if (spuId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SPU ID不能为空");
        }
        SmsSpuBounds entity = smsSpuBoundsMapper.selectOne(
                Wrappers.<SmsSpuBounds>lambdaQuery().eq(SmsSpuBounds::getSpuId, spuId));
        if (entity == null) {
            return null;
        }
        Map<Long, String> spuNameMap = loadSpuNameMap(List.of(entity));
        return toVo(entity, spuNameMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SpuBoundsSaveBo bo) {
        validateSpuExists(bo.getSpuId());
        Long count = smsSpuBoundsMapper.selectCount(
                Wrappers.<SmsSpuBounds>lambdaQuery().eq(SmsSpuBounds::getSpuId, bo.getSpuId()));
        if (count != null && count > 0) {
            throw new BizException("该 SPU 已存在积分配置，请直接修改");
        }
        SmsSpuBounds entity = new SmsSpuBounds();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        smsSpuBoundsMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SpuBoundsSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改积分配置时 id 不能为空");
        }
        SmsSpuBounds existing = smsSpuBoundsMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("积分配置不存在");
        }
        validateSpuExists(bo.getSpuId());
        SmsSpuBounds duplicate = smsSpuBoundsMapper.selectOne(Wrappers.<SmsSpuBounds>lambdaQuery()
                .eq(SmsSpuBounds::getSpuId, bo.getSpuId())
                .ne(SmsSpuBounds::getId, bo.getId()));
        if (duplicate != null) {
            throw new BizException("该 SPU 已存在其他积分配置");
        }
        SmsSpuBounds entity = new SmsSpuBounds();
        BeanUtils.copyProperties(bo, entity);
        smsSpuBoundsMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsSpuBoundsMapper.delete(Wrappers.<SmsSpuBounds>lambdaQuery().in(SmsSpuBounds::getId, ids));
    }

    private void validateSpuExists(Long spuId) {
        if (spuId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SPU ID不能为空");
        }
        if (pmsSpuInfoMapper.selectById(spuId) == null) {
            throw new BizException("SPU不存在");
        }
    }

    private Map<Long, String> loadSpuNameMap(List<SmsSpuBounds> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> spuIds = records.stream()
                .map(SmsSpuBounds::getSpuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(spuIds)) {
            return Collections.emptyMap();
        }
        return pmsSpuInfoMapper.selectBatchIds(spuIds).stream()
                .collect(Collectors.toMap(PmsSpuInfo::getId, PmsSpuInfo::getSpuName, (a, b) -> a));
    }

    private SpuBoundsVo toVo(SmsSpuBounds entity, Map<Long, String> spuNameMap) {
        SpuBoundsVo vo = new SpuBoundsVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setSpuName(spuNameMap.get(entity.getSpuId()));
        return vo;
    }
}
