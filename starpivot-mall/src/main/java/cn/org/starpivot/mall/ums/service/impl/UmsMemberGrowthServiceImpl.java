package cn.org.starpivot.mall.ums.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberGrowthReqBo;
import cn.org.starpivot.mall.ums.domain.vo.GrowthChangeHistoryVo;
import cn.org.starpivot.mall.ums.domain.vo.IntegrationChangeHistoryVo;
import cn.org.starpivot.mall.ums.entity.UmsGrowthChangeHistory;
import cn.org.starpivot.mall.ums.entity.UmsIntegrationChangeHistory;
import cn.org.starpivot.mall.ums.mapper.UmsGrowthChangeHistoryMapper;
import cn.org.starpivot.mall.ums.mapper.UmsIntegrationChangeHistoryMapper;
import cn.org.starpivot.mall.ums.service.UmsMemberGrowthService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UmsMemberGrowthServiceImpl implements UmsMemberGrowthService {

    private final UmsIntegrationChangeHistoryMapper integrationChangeHistoryMapper;
    private final UmsGrowthChangeHistoryMapper growthChangeHistoryMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<IntegrationChangeHistoryVo> integrationPageList(MemberGrowthReqBo reqBo) {
        Page<UmsIntegrationChangeHistory> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<UmsIntegrationChangeHistory> wrapper = buildIntegrationWrapper(reqBo);
        IPage<UmsIntegrationChangeHistory> pageList = integrationChangeHistoryMapper.selectPage(page, wrapper);
        return toIntegrationPageResponse(pageList);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GrowthChangeHistoryVo> growthPageList(MemberGrowthReqBo reqBo) {
        Page<UmsGrowthChangeHistory> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<UmsGrowthChangeHistory> wrapper = buildGrowthWrapper(reqBo);
        IPage<UmsGrowthChangeHistory> pageList = growthChangeHistoryMapper.selectPage(page, wrapper);
        return toGrowthPageResponse(pageList);
    }

    private LambdaQueryWrapper<UmsIntegrationChangeHistory> buildIntegrationWrapper(MemberGrowthReqBo reqBo) {
        LambdaQueryWrapper<UmsIntegrationChangeHistory> wrapper = Wrappers.lambdaQuery();
        if (reqBo.getMemberId() != null) {
            wrapper.eq(UmsIntegrationChangeHistory::getMemberId, reqBo.getMemberId());
        }
        wrapper.orderByDesc(UmsIntegrationChangeHistory::getCreateTime);
        return wrapper;
    }

    private LambdaQueryWrapper<UmsGrowthChangeHistory> buildGrowthWrapper(MemberGrowthReqBo reqBo) {
        LambdaQueryWrapper<UmsGrowthChangeHistory> wrapper = Wrappers.lambdaQuery();
        if (reqBo.getMemberId() != null) {
            wrapper.eq(UmsGrowthChangeHistory::getMemberId, reqBo.getMemberId());
        }
        wrapper.orderByDesc(UmsGrowthChangeHistory::getCreateTime);
        return wrapper;
    }

    private PageResponse<IntegrationChangeHistoryVo> toIntegrationPageResponse(
            IPage<UmsIntegrationChangeHistory> pageList) {
        PageResponse<IntegrationChangeHistoryVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(
                pageList.getRecords().stream().map(this::toIntegrationVo).collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    private PageResponse<GrowthChangeHistoryVo> toGrowthPageResponse(IPage<UmsGrowthChangeHistory> pageList) {
        PageResponse<GrowthChangeHistoryVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords().stream().map(this::toGrowthVo).collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    private IntegrationChangeHistoryVo toIntegrationVo(UmsIntegrationChangeHistory entity) {
        IntegrationChangeHistoryVo vo = new IntegrationChangeHistoryVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }

    private GrowthChangeHistoryVo toGrowthVo(UmsGrowthChangeHistory entity) {
        GrowthChangeHistoryVo vo = new GrowthChangeHistoryVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
