package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.entity.PmsSkuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSkuInfoMapper;
import cn.org.starpivot.mall.sms.domain.bo.MemberPriceReqBo;
import cn.org.starpivot.mall.sms.domain.bo.MemberPriceSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.MemberPriceVo;
import cn.org.starpivot.mall.sms.entity.SmsMemberPrice;
import cn.org.starpivot.mall.sms.mapper.SmsMemberPriceMapper;
import cn.org.starpivot.mall.sms.service.SmsMemberPriceService;
import cn.org.starpivot.mall.ums.entity.UmsMemberLevel;
import cn.org.starpivot.mall.ums.mapper.UmsMemberLevelMapper;
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
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SmsMemberPriceServiceImpl implements SmsMemberPriceService {

    private final SmsMemberPriceMapper smsMemberPriceMapper;
    private final PmsSkuInfoMapper pmsSkuInfoMapper;
    private final UmsMemberLevelMapper umsMemberLevelMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberPriceVo> pageList(MemberPriceReqBo reqBo) {
        Page<SmsMemberPrice> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<SmsMemberPrice> wrapper = Wrappers.lambdaQuery();
        if (reqBo.getSkuId() != null) {
            wrapper.eq(SmsMemberPrice::getSkuId, reqBo.getSkuId());
        }
        if (reqBo.getMemberLevelId() != null) {
            wrapper.eq(SmsMemberPrice::getMemberLevelId, reqBo.getMemberLevelId());
        }
        wrapper.orderByDesc(SmsMemberPrice::getId);
        IPage<SmsMemberPrice> pageList = smsMemberPriceMapper.selectPage(page, wrapper);

        List<SmsMemberPrice> records = pageList.getRecords();
        Map<Long, String> skuNameMap = loadSkuNameMap(records);

        PageResponse<MemberPriceVo> pageResponse = new PageResponse<>();
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
    public MemberPriceVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "会员价ID不能为空");
        }
        SmsMemberPrice entity = smsMemberPriceMapper.selectById(id);
        if (entity == null) {
            throw new BizException("会员价不存在");
        }
        Map<Long, String> skuNameMap = loadSkuNameMap(List.of(entity));
        return toVo(entity, skuNameMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(MemberPriceSaveBo bo) {
        validateSkuExists(bo.getSkuId());
        fillMemberLevelName(bo);
        SmsMemberPrice entity = new SmsMemberPrice();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        if (entity.getAddOther() == null) {
            entity.setAddOther(0);
        }
        smsMemberPriceMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MemberPriceSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改会员价时 id 不能为空");
        }
        SmsMemberPrice existing = smsMemberPriceMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("会员价不存在");
        }
        validateSkuExists(bo.getSkuId());
        fillMemberLevelName(bo);
        SmsMemberPrice entity = new SmsMemberPrice();
        BeanUtils.copyProperties(bo, entity);
        smsMemberPriceMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsMemberPriceMapper.delete(Wrappers.<SmsMemberPrice>lambdaQuery().in(SmsMemberPrice::getId, ids));
    }

    private void validateSkuExists(Long skuId) {
        if (skuId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "SKU ID不能为空");
        }
        if (pmsSkuInfoMapper.selectById(skuId) == null) {
            throw new BizException("SKU不存在");
        }
    }

    private void fillMemberLevelName(MemberPriceSaveBo bo) {
        if (bo.getMemberLevelId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "会员等级不能为空");
        }
        UmsMemberLevel level = umsMemberLevelMapper.selectById(bo.getMemberLevelId());
        if (level == null) {
            throw new BizException("会员等级不存在");
        }
        if (!StringUtils.hasText(bo.getMemberLevelName())) {
            bo.setMemberLevelName(level.getName());
        }
    }

    private Map<Long, String> loadSkuNameMap(List<SmsMemberPrice> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> skuIds = records.stream()
                .map(SmsMemberPrice::getSkuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(skuIds)) {
            return Collections.emptyMap();
        }
        return pmsSkuInfoMapper.selectBatchIds(skuIds).stream()
                .collect(Collectors.toMap(PmsSkuInfo::getSkuId, PmsSkuInfo::getSkuName, (a, b) -> a));
    }

    private MemberPriceVo toVo(SmsMemberPrice entity, Map<Long, String> skuNameMap) {
        MemberPriceVo vo = new MemberPriceVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setSkuName(skuNameMap.get(entity.getSkuId()));
        return vo;
    }
}
