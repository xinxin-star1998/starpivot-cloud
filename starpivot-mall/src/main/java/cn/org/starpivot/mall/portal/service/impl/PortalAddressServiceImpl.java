package cn.org.starpivot.mall.portal.service.impl;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.mall.portal.domain.bo.PortalAddressSaveBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalAddressVo;
import cn.org.starpivot.mall.portal.service.PortalAddressService;
import cn.org.starpivot.mall.ums.entity.UmsMemberReceiveAddress;
import cn.org.starpivot.mall.ums.mapper.UmsMemberReceiveAddressMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PortalAddressServiceImpl implements PortalAddressService {

    private static final int DEFAULT_ADDRESS = 1;

    private final UmsMemberReceiveAddressMapper addressMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PortalAddressVo> listByMember(Long memberId) {
        return addressMapper.selectList(
                        Wrappers.<UmsMemberReceiveAddress>lambdaQuery()
                                .eq(UmsMemberReceiveAddress::getMemberId, memberId)
                                .orderByDesc(UmsMemberReceiveAddress::getDefaultStatus)
                                .orderByDesc(UmsMemberReceiveAddress::getId))
                .stream()
                .map(this::toVo)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PortalAddressVo getById(Long memberId, Long addressId) {
        return toVo(requireAddress(memberId, addressId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Long memberId, PortalAddressSaveBo bo) {
        if (bo.getId() == null) {
            UmsMemberReceiveAddress entity = new UmsMemberReceiveAddress();
            copyFields(bo, entity);
            entity.setMemberId(memberId);
            if (entity.getDefaultStatus() == null) {
                entity.setDefaultStatus(0);
            }
            if (Integer.valueOf(DEFAULT_ADDRESS).equals(entity.getDefaultStatus())) {
                clearDefault(memberId);
            }
            addressMapper.insert(entity);
            return;
        }

        UmsMemberReceiveAddress existing = requireAddress(memberId, bo.getId());
        copyFields(bo, existing);
        if (Integer.valueOf(DEFAULT_ADDRESS).equals(existing.getDefaultStatus())) {
            clearDefault(memberId);
            existing.setDefaultStatus(DEFAULT_ADDRESS);
        }
        addressMapper.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void remove(Long memberId, Long addressId) {
        UmsMemberReceiveAddress address = requireAddress(memberId, addressId);
        addressMapper.deleteById(address.getId());
    }

    private UmsMemberReceiveAddress requireAddress(Long memberId, Long addressId) {
        if (addressId == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "地址ID不能为空");
        }
        UmsMemberReceiveAddress address = addressMapper.selectById(addressId);
        if (address == null || !memberId.equals(address.getMemberId())) {
            throw new BizException("收货地址不存在");
        }
        return address;
    }

    private void clearDefault(Long memberId) {
        addressMapper.update(
                null,
                Wrappers.<UmsMemberReceiveAddress>lambdaUpdate()
                        .eq(UmsMemberReceiveAddress::getMemberId, memberId)
                        .set(UmsMemberReceiveAddress::getDefaultStatus, 0));
    }

    private void copyFields(PortalAddressSaveBo bo, UmsMemberReceiveAddress entity) {
        entity.setName(bo.getName());
        entity.setPhone(bo.getPhone());
        entity.setPostCode(bo.getPostCode());
        entity.setProvince(bo.getProvince());
        entity.setCity(bo.getCity());
        entity.setRegion(bo.getRegion());
        entity.setDetailAddress(bo.getDetailAddress());
        if (bo.getDefaultStatus() != null) {
            entity.setDefaultStatus(bo.getDefaultStatus());
        }
    }

    private PortalAddressVo toVo(UmsMemberReceiveAddress entity) {
        PortalAddressVo vo = new PortalAddressVo();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
