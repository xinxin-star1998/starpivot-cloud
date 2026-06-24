package cn.org.starpivot.mall.sms.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.sms.domain.bo.HomeAdvReqBo;
import cn.org.starpivot.mall.sms.domain.bo.HomeAdvSaveBo;
import cn.org.starpivot.mall.sms.domain.vo.HomeAdvVo;
import cn.org.starpivot.mall.sms.entity.SmsHomeAdv;
import cn.org.starpivot.mall.sms.mapper.SmsHomeAdvMapper;
import cn.org.starpivot.mall.sms.service.SmsHomeAdvService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class SmsHomeAdvServiceImpl implements SmsHomeAdvService {

    private final SmsHomeAdvMapper smsHomeAdvMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<HomeAdvVo> pageList(HomeAdvReqBo reqBo) {
        Page<SmsHomeAdv> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<SmsHomeAdv> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(reqBo.getName())) {
            wrapper.like(SmsHomeAdv::getName, reqBo.getName());
        }
        if (reqBo.getStatus() != null) {
            wrapper.eq(SmsHomeAdv::getStatus, reqBo.getStatus());
        }
        wrapper.orderByAsc(SmsHomeAdv::getSort).orderByDesc(SmsHomeAdv::getId);
        IPage<SmsHomeAdv> pageList = smsHomeAdvMapper.selectPage(page, wrapper);

        PageResponse<HomeAdvVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public HomeAdvVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "广告ID不能为空");
        }
        SmsHomeAdv entity = smsHomeAdvMapper.selectById(id);
        if (entity == null) {
            throw new BizException("广告不存在");
        }
        return toVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(HomeAdvSaveBo bo) {
        SmsHomeAdv entity = new SmsHomeAdv();
        BeanUtils.copyProperties(bo, entity);
        entity.setId(null);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        smsHomeAdvMapper.insert(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(HomeAdvSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "修改广告时 id 不能为空");
        }
        SmsHomeAdv existing = smsHomeAdvMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("广告不存在");
        }
        SmsHomeAdv entity = new SmsHomeAdv();
        BeanUtils.copyProperties(bo, entity);
        smsHomeAdvMapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "删除ID不能为空");
        }
        smsHomeAdvMapper.delete(Wrappers.<SmsHomeAdv>lambdaQuery().in(SmsHomeAdv::getId, ids));
    }

    private HomeAdvVo toVo(SmsHomeAdv entity) {
        HomeAdvVo vo = new HomeAdvVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
