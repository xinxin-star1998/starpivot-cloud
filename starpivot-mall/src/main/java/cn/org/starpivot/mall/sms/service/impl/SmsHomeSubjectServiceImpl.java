package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.pms.entity.PmsSpuInfo;
import cn.org.starpivot.mall.pms.mapper.PmsSpuInfoMapper;
import cn.org.starpivot.mall.sms.domain.bo.HomeSubjectReqBo;
import cn.org.starpivot.mall.sms.domain.bo.HomeSubjectSaveBo;
import cn.org.starpivot.mall.sms.domain.bo.HomeSubjectSpuBo;
import cn.org.starpivot.mall.sms.domain.vo.HomeSubjectSpuVo;
import cn.org.starpivot.mall.sms.domain.vo.HomeSubjectVo;
import cn.org.starpivot.mall.sms.entity.SmsHomeSubject;
import cn.org.starpivot.mall.sms.entity.SmsHomeSubjectSpu;
import cn.org.starpivot.mall.sms.mapper.SmsHomeSubjectMapper;
import cn.org.starpivot.mall.sms.mapper.SmsHomeSubjectSpuMapper;
import cn.org.starpivot.mall.sms.service.SmsHomeSubjectService;
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
public class SmsHomeSubjectServiceImpl implements SmsHomeSubjectService {

    private final SmsHomeSubjectMapper smsHomeSubjectMapper;
    private final SmsHomeSubjectSpuMapper smsHomeSubjectSpuMapper;
    private final PmsSpuInfoMapper pmsSpuInfoMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HomeSubjectVo> pageList(HomeSubjectReqBo reqBo) {
        Page<SmsHomeSubject> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<SmsHomeSubject> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(reqBo.getName())) {
            wrapper.like(SmsHomeSubject::getName, reqBo.getName());
        }
        if (reqBo.getStatus() != null) {
            wrapper.eq(SmsHomeSubject::getStatus, reqBo.getStatus());
        }
        wrapper.orderByAsc(SmsHomeSubject::getSort).orderByDesc(SmsHomeSubject::getId);
        IPage<SmsHomeSubject> pageList = smsHomeSubjectMapper.selectPage(page, wrapper);

        PageResponse<HomeSubjectVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public HomeSubjectVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "专题ID不能为空");
        }
        SmsHomeSubject entity = smsHomeSubjectMapper.selectById(id);
        if (entity == null) {
            throw new BizException("专题不存在");
        }
        HomeSubjectVo vo = toVo(entity);
        vo.setSpuList(loadSpuList(id));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(HomeSubjectSaveBo bo) {
        SmsHomeSubject entity = new SmsHomeSubject();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        smsHomeSubjectMapper.insert(entity);
        saveSpuList(entity.getId(), bo.getSpuList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(HomeSubjectSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改专题时 id 不能为空");
        }
        SmsHomeSubject existing = smsHomeSubjectMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("专题不存在");
        }
        SmsHomeSubject entity = new SmsHomeSubject();
        BeanUtils.copyProperties(bo, entity);
        smsHomeSubjectMapper.updateById(entity);
        smsHomeSubjectSpuMapper.delete(
                Wrappers.<SmsHomeSubjectSpu>lambdaQuery().eq(SmsHomeSubjectSpu::getSubjectId, bo.getId()));
        saveSpuList(bo.getId(), bo.getSpuList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsHomeSubjectSpuMapper.delete(
                Wrappers.<SmsHomeSubjectSpu>lambdaQuery().in(SmsHomeSubjectSpu::getSubjectId, ids));
        smsHomeSubjectMapper.delete(Wrappers.<SmsHomeSubject>lambdaQuery().in(SmsHomeSubject::getId, ids));
    }

    private void saveSpuList(Long subjectId, List<HomeSubjectSpuBo> spuList) {
        if (CollectionUtils.isEmpty(spuList)) {
            return;
        }
        for (HomeSubjectSpuBo spuBo : spuList) {
            if (spuBo.getSpuId() == null) {
                continue;
            }
            SmsHomeSubjectSpu relation = new SmsHomeSubjectSpu();
            relation.setSubjectId(subjectId);
            relation.setSpuId(spuBo.getSpuId());
            relation.setName(spuBo.getName());
            relation.setSort(spuBo.getSort() != null ? spuBo.getSort() : 0);
            smsHomeSubjectSpuMapper.insert(relation);
        }
    }

    private List<HomeSubjectSpuVo> loadSpuList(Long subjectId) {
        List<SmsHomeSubjectSpu> relations = smsHomeSubjectSpuMapper.selectList(
                Wrappers.<SmsHomeSubjectSpu>lambdaQuery()
                        .eq(SmsHomeSubjectSpu::getSubjectId, subjectId)
                        .orderByAsc(SmsHomeSubjectSpu::getSort)
                        .orderByAsc(SmsHomeSubjectSpu::getId));
        if (CollectionUtils.isEmpty(relations)) {
            return Collections.emptyList();
        }
        List<Long> spuIds = relations.stream()
                .map(SmsHomeSubjectSpu::getSpuId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> spuNameMap = Collections.emptyMap();
        if (!CollectionUtils.isEmpty(spuIds)) {
            spuNameMap = pmsSpuInfoMapper.selectBatchIds(spuIds).stream()
                    .collect(Collectors.toMap(PmsSpuInfo::getId, PmsSpuInfo::getSpuName, (a, b) -> a));
        }
        Map<Long, String> finalSpuNameMap = spuNameMap;
        return relations.stream().map(relation -> {
            HomeSubjectSpuVo vo = new HomeSubjectSpuVo();
            BeanUtils.copyProperties(relation, vo);
            vo.setSpuName(finalSpuNameMap.get(relation.getSpuId()));
            return vo;
        }).collect(Collectors.toList());
    }

    private HomeSubjectVo toVo(SmsHomeSubject entity) {
        HomeSubjectVo vo = new HomeSubjectVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
