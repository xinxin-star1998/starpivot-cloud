package cn.org.starpivot.mall.ums.service.impl;

import cn.org.starpivot.common.entity.PageResponse;
import cn.org.starpivot.mall.ums.domain.bo.MemberReceiveAddressReqBo;
import cn.org.starpivot.mall.ums.domain.vo.MemberReceiveAddressVo;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.entity.UmsMemberReceiveAddress;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberReceiveAddressMapper;
import cn.org.starpivot.mall.ums.service.UmsMemberReceiveAddressService;
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
public class UmsMemberReceiveAddressServiceImpl implements UmsMemberReceiveAddressService {

    private final UmsMemberReceiveAddressMapper addressMapper;
    private final UmsMemberMapper umsMemberMapper;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<MemberReceiveAddressVo> pageList(MemberReceiveAddressReqBo reqBo) {
        Page<UmsMemberReceiveAddress> page = new Page<>(reqBo.getPageNum(), reqBo.getPageSize());
        LambdaQueryWrapper<UmsMemberReceiveAddress> wrapper = Wrappers.<UmsMemberReceiveAddress>lambdaQuery()
                .eq(reqBo.getMemberId() != null, UmsMemberReceiveAddress::getMemberId, reqBo.getMemberId())
                .like(StringUtils.hasText(reqBo.getName()), UmsMemberReceiveAddress::getName, reqBo.getName())
                .like(StringUtils.hasText(reqBo.getPhone()), UmsMemberReceiveAddress::getPhone, reqBo.getPhone())
                .like(StringUtils.hasText(reqBo.getProvince()), UmsMemberReceiveAddress::getProvince, reqBo.getProvince())
                .eq(reqBo.getDefaultStatus() != null, UmsMemberReceiveAddress::getDefaultStatus, reqBo.getDefaultStatus())
                .orderByDesc(UmsMemberReceiveAddress::getDefaultStatus)
                .orderByDesc(UmsMemberReceiveAddress::getId);
        IPage<UmsMemberReceiveAddress> pageList = addressMapper.selectPage(page, wrapper);

        List<Long> memberIds = pageList.getRecords().stream()
                .map(UmsMemberReceiveAddress::getMemberId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, UmsMember> memberMap = memberIds.isEmpty()
                ? Map.of()
                : umsMemberMapper.selectBatchIds(memberIds).stream()
                        .filter(m -> m.getId() != null)
                        .collect(Collectors.toMap(UmsMember::getId, m -> m, (a, b) -> a));

        PageResponse<MemberReceiveAddressVo> response = new PageResponse<>();
        response.setTotal(pageList.getTotal());
        response.setRows(pageList.getRecords().stream()
                .map(address -> toVo(address, memberMap.get(address.getMemberId())))
                .toList());
        response.setPageNum(pageList.getCurrent());
        response.setPageSize(pageList.getSize());
        response.setPageCount(pageList.getPages());
        return response;
    }

    private MemberReceiveAddressVo toVo(UmsMemberReceiveAddress address, UmsMember member) {
        MemberReceiveAddressVo vo = new MemberReceiveAddressVo();
        BeanUtils.copyProperties(address, vo);
        if (member != null) {
            vo.setMemberUsername(member.getUsername());
            vo.setMemberNickname(member.getNickname());
        }
        vo.setDefaultStatusLabel(address.getDefaultStatus() != null && address.getDefaultStatus() == 1 ? "默认" : "否");
        return vo;
    }
}
