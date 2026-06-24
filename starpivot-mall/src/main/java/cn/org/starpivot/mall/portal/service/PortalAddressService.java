package cn.org.starpivot.mall.portal.service;

import cn.org.starpivot.mall.portal.domain.bo.PortalAddressSaveBo;
import cn.org.starpivot.mall.portal.domain.vo.PortalAddressVo;
import java.util.List;

public interface PortalAddressService {

    List<PortalAddressVo> listByMember(Long memberId);

    PortalAddressVo getById(Long memberId, Long addressId);

    void save(Long memberId, PortalAddressSaveBo bo);

    void remove(Long memberId, Long addressId);
}
