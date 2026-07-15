package cn.org.starpivot.mall.member.internal;

import cn.org.starpivot.api.member.dto.MemberAddressDto;
import cn.org.starpivot.api.member.dto.MemberDto;
import cn.org.starpivot.api.member.dto.MemberLevelDto;
import cn.org.starpivot.mall.ums.entity.UmsMember;
import cn.org.starpivot.mall.ums.entity.UmsMemberLevel;
import cn.org.starpivot.mall.ums.entity.UmsMemberReceiveAddress;
import cn.org.starpivot.mall.ums.mapper.UmsMemberLevelMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberMapper;
import cn.org.starpivot.mall.ums.mapper.UmsMemberReceiveAddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 会员域内部查询业务（Feign /internal 目标）。
 */
@Service
@RequiredArgsConstructor
public class MemberInternalService {

    private final UmsMemberMapper umsMemberMapper;
    private final UmsMemberReceiveAddressMapper addressMapper;
    private final UmsMemberLevelMapper umsMemberLevelMapper;

    @Transactional(readOnly = true)
    public MemberDto getMember(Long memberId) {
        UmsMember member = umsMemberMapper.selectById(memberId);
        if (member == null) {
            return null;
        }
        return toMemberDto(member);
    }

    @Transactional(readOnly = true)
    public MemberAddressDto getAddress(Long memberId, Long addressId) {
        UmsMemberReceiveAddress address = addressMapper.selectById(addressId);
        if (address == null || !memberId.equals(address.getMemberId())) {
            return null;
        }
        return toAddressDto(address);
    }

    @Transactional(readOnly = true)
    public MemberLevelDto getMemberLevel(Long levelId) {
        UmsMemberLevel level = umsMemberLevelMapper.selectById(levelId);
        if (level == null) {
            return null;
        }
        return toLevelDto(level);
    }

    private MemberDto toMemberDto(UmsMember member) {
        MemberDto dto = new MemberDto();
        dto.setId(member.getId());
        dto.setUsername(member.getUsername());
        dto.setNickname(member.getNickname());
        dto.setMobile(member.getMobile());
        dto.setHeader(member.getHeader());
        dto.setStatus(member.getStatus());
        dto.setLevelId(member.getLevelId());
        dto.setIntegration(member.getIntegration());
        dto.setGrowth(member.getGrowth());
        return dto;
    }

    private MemberAddressDto toAddressDto(UmsMemberReceiveAddress address) {
        MemberAddressDto dto = new MemberAddressDto();
        BeanUtils.copyProperties(address, dto);
        return dto;
    }

    private MemberLevelDto toLevelDto(UmsMemberLevel level) {
        MemberLevelDto dto = new MemberLevelDto();
        dto.setId(level.getId());
        dto.setName(level.getName());
        dto.setGrowthPoint(level.getGrowthPoint());
        dto.setFreeFreightPoint(level.getFreeFreightPoint());
        dto.setPriviledgeFreeFreight(level.getPrivilegeFreeFreight());
        dto.setPriviledgeMemberPrice(level.getPrivilegeMemberPrice());
        dto.setPriviledgeBirthday(level.getPrivilegeBirthday());
        return dto;
    }
}
