package cn.org.starpivot.mall.ums.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberCollectReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberCollectSpuVo;
import cn.org.starpivot.mall.ums.domain.vo.MemberCollectSubjectVo;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.entity.UmsMemberCollectSpu;
import cn.org.starpivot.mall.ums.entity.UmsMemberCollectSubject;
import cn.org.starpivot.mall.ums.mapper.UmsMemberCollectSpuMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberCollectSubjectMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import cn.org.starpivot.mall.ums.service.UmsMemberCollectService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UmsMemberCollectServiceImpl implements UmsMemberCollectService {

    private final UmsMemberCollectSpuMapper collectSpuMapper;
    private final UmsMemberCollectSubjectMapper collectSubjectMapper;
    private final UmsMemberMapper umsMemberMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberCollectSpuVo> spuPageList(MemberCollectReqBo reqBo) {
        Page<UmsMemberCollectSpu> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<UmsMemberCollectSpu> wrapper = Wrappers.<UmsMemberCollectSpu>lambdaQuery()
                .eq(reqBo.getMemberId() != null, UmsMemberCollectSpu::getMemberId, reqBo.getMemberId())
                .like(StringUtils.hasText(reqBo.getSpuName()), UmsMemberCollectSpu::getSpuName, reqBo.getSpuName())
                .orderByDesc(UmsMemberCollectSpu::getCreateTime)
                .orderByDesc(UmsMemberCollectSpu::getId);
        IPage<UmsMemberCollectSpu> pageList = collectSpuMapper.selectPage(page, wrapper);
        Map<Long, UmsMember> memberMap = loadMemberMap(pageList.getRecords().stream()
                .map(UmsMemberCollectSpu::getMemberId)
                .toList());

        PageResponse<MemberCollectSpuVo> response = new PageResponse<>();
        response.setTotal(pageList.getTotal());
        response.setRows(pageList.getRecords().stream()
                .map(row -> toSpuVo(row, memberMap.get(row.getMemberId())))
                .toList());
        response.setPageNum(pageList.getCurrent());
        response.setPageSize(pageList.getSize());
        response.setPageCount(pageList.getPages());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberCollectSubjectVo> subjectPageList(MemberCollectReqBo reqBo) {
        Page<UmsMemberCollectSubject> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<UmsMemberCollectSubject> wrapper = Wrappers.<UmsMemberCollectSubject>lambdaQuery()
                .eq(reqBo.getMemberId() != null, UmsMemberCollectSubject::getMemberId, reqBo.getMemberId())
                .like(StringUtils.hasText(reqBo.getSubjectName()), UmsMemberCollectSubject::getSubjectName, reqBo.getSubjectName())
                .orderByDesc(UmsMemberCollectSubject::getId);
        IPage<UmsMemberCollectSubject> pageList = collectSubjectMapper.selectPage(page, wrapper);
        Map<Long, UmsMember> memberMap = loadMemberMap(pageList.getRecords().stream()
                .map(UmsMemberCollectSubject::getMemberId)
                .toList());

        PageResponse<MemberCollectSubjectVo> response = new PageResponse<>();
        response.setTotal(pageList.getTotal());
        response.setRows(pageList.getRecords().stream()
                .map(row -> toSubjectVo(row, memberMap.get(row.getMemberId())))
                .toList());
        response.setPageNum(pageList.getCurrent());
        response.setPageSize(pageList.getSize());
        response.setPageCount(pageList.getPages());
        return response;
    }

    private Map<Long, UmsMember> loadMemberMap(List<Long> memberIds) {
        List<Long> ids = memberIds.stream().filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return umsMemberMapper.selectBatchIds(ids).stream()
                .filter(m -> m.getId() != null)
                .collect(Collectors.toMap(UmsMember::getId, m -> m, (a, b) -> a));
    }

    private MemberCollectSpuVo toSpuVo(UmsMemberCollectSpu row, UmsMember member) {
        MemberCollectSpuVo vo = new MemberCollectSpuVo();
        BeanUtils.copyProperties(row, vo);
        if (member != null) {
            vo.setMemberUsername(member.getUsername());
        }
        return vo;
    }

    private MemberCollectSubjectVo toSubjectVo(UmsMemberCollectSubject row, UmsMember member) {
        MemberCollectSubjectVo vo = new MemberCollectSubjectVo();
        BeanUtils.copyProperties(row, vo);
        if (member != null) {
            vo.setMemberUsername(member.getUsername());
        }
        return vo;
    }
}
