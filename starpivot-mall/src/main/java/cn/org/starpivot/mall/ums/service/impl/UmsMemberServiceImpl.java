package cn.org.starpivot.mall.ums.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.ums.domain.bo.MemberReqBo;
import cn.org.starpivot.mall.ums.domain.bo.MemberSaveBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberVo;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import cn.org.starpivot.mall.ums.service.UmsMemberService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UmsMemberServiceImpl implements UmsMemberService {

    private final UmsMemberMapper umsMemberMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberVo> pageList(MemberReqBo reqBo) {
        Page<UmsMember> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<UmsMember> wrapper = Wrappers.lambdaQuery();
        if (StringUtils.hasText(reqBo.getUsername())) {
            wrapper.like(UmsMember::getUsername, reqBo.getUsername());
        }
        if (StringUtils.hasText(reqBo.getMobile())) {
            wrapper.like(UmsMember::getMobile, reqBo.getMobile());
        }
        if (reqBo.getStatus() != null) {
            wrapper.eq(UmsMember::getStatus, reqBo.getStatus());
        }
        wrapper.orderByDesc(UmsMember::getCreateTime);
        IPage<UmsMember> pageList = umsMemberMapper.selectPage(page, wrapper);

        PageResponse<MemberVo> pageResponse = new PageResponse<>();
        pageResponse.setTotal(pageList.getTotal());
        pageResponse.setRows(pageList.getRecords().stream().map(this::toVo).collect(Collectors.toList()));
        pageResponse.setPageNum(pageList.getCurrent());
        pageResponse.setPageSize(pageList.getSize());
        pageResponse.setPageCount(pageList.getPages());
        return pageResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public MemberVo getById(Long id) {
        if (id == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "会员ID不能为空");
        }
        UmsMember entity = umsMemberMapper.selectById(id);
        if (entity == null) {
            throw new BizException("会员不存在");
        }
        return toVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(MemberSaveBo bo) {
        if (bo.getId() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "会员ID不能为空");
        }
        UmsMember existing = umsMemberMapper.selectById(bo.getId());
        if (existing == null) {
            throw new BizException("会员不存在");
        }
        if (bo.getNickname() != null) {
            existing.setNickname(bo.getNickname());
        }
        if (bo.getStatus() != null) {
            existing.setStatus(bo.getStatus());
        }
        if (bo.getIntegration() != null) {
            existing.setIntegration(bo.getIntegration());
        }
        if (bo.getGrowth() != null) {
            existing.setGrowth(bo.getGrowth());
        }
        umsMemberMapper.updateById(existing);
    }

    private MemberVo toVo(UmsMember entity) {
        MemberVo vo = new MemberVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
